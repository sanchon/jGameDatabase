package xyz.sanchon.jgamedatabase.controller;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import xyz.sanchon.jgamedatabase.dto.GgDealsFetchResult;
import xyz.sanchon.jgamedatabase.dto.GgDealsPriceEntry;
import xyz.sanchon.jgamedatabase.dto.IgdbGame;
import xyz.sanchon.jgamedatabase.dto.SteamSearchHitDto;
import xyz.sanchon.jgamedatabase.model.Game;
import xyz.sanchon.jgamedatabase.repository.GameRepository;
import xyz.sanchon.jgamedatabase.repository.GameStatusRepository;
import xyz.sanchon.jgamedatabase.repository.GenreRepository;
import xyz.sanchon.jgamedatabase.repository.PlatformRepository;
import xyz.sanchon.jgamedatabase.service.CsvService;
import xyz.sanchon.jgamedatabase.service.GgDealsService;
import xyz.sanchon.jgamedatabase.service.IgdbService;
import xyz.sanchon.jgamedatabase.service.MarkdownService;
import xyz.sanchon.jgamedatabase.service.SteamStoreSearchService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/games")
public class GameController {

    private final GameRepository gameRepository;
    private final GameStatusRepository gameStatusRepository;
    private final PlatformRepository platformRepository;
    private final GenreRepository genreRepository;
    private final IgdbService igdbService;
    private final GgDealsService ggDealsService;
    private final SteamStoreSearchService steamStoreSearchService;
    private final MarkdownService markdownService;
    private final CsvService csvService;

    public GameController(GameRepository gameRepository, GameStatusRepository gameStatusRepository, PlatformRepository platformRepository, GenreRepository genreRepository, IgdbService igdbService, GgDealsService ggDealsService, SteamStoreSearchService steamStoreSearchService, MarkdownService markdownService, CsvService csvService) {
        this.gameRepository = gameRepository;
        this.gameStatusRepository = gameStatusRepository;
        this.platformRepository = platformRepository;
        this.genreRepository = genreRepository;
        this.igdbService = igdbService;
        this.ggDealsService = ggDealsService;
        this.steamStoreSearchService = steamStoreSearchService;
        this.markdownService = markdownService;
        this.csvService = csvService;
    }

    private void applyStatus(Game game, String statusName) {
        if (statusName == null || statusName.isBlank()) {
            game.setGameStatus(null);
        } else {
            gameStatusRepository.findByName(statusName).ifPresent(gs -> {
                game.setGameStatus(gs);
                game.setStatus(null);
            });
        }
    }

    /**
     * Búsqueda en la tienda Steam (solo usada desde el formulario de deseados).
     */
    @GetMapping(value = "/api/steam-search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<SteamSearchHitDto> steamSearch(@RequestParam("q") String q) {
        return steamStoreSearchService.search(q);
    }

    @GetMapping
    public String listGames(@RequestParam(required = false) String status,
                            @RequestParam(required = false) Long genreId,
                            @RequestParam(required = false) Long platformId,
                            @RequestParam(defaultValue = "title") String sortBy,
                            @RequestParam(defaultValue = "asc") String sortDir,
                            Model model) {
        
        // Always filter by wishlist = false for the main list
        Specification<Game> spec = Specification.where((root, query, cb) -> cb.equal(root.get("wishlist"), false));
        
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        
        if (genreId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("genre").get("id"), genreId));
        }
        
        if (platformId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("platform").get("id"), platformId));
        }

        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase("asc") ? 
            org.springframework.data.domain.Sort.by(sortBy).ascending() : 
            org.springframework.data.domain.Sort.by(sortBy).descending();

        List<Game> games = gameRepository.findAll(spec, sort);
        model.addAttribute("games", games);
        attachGgDealsPrices(model, games);
        model.addAttribute("genres", genreRepository.findAll());
        model.addAttribute("platforms", platformRepository.findAll());
        
        // Pass back current filters and sorting to the model
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedGenre", genreId);
        model.addAttribute("selectedPlatform", platformId);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        
        return "games/list";
    }

    @GetMapping("/wishlist")
    public String listWishlist(@RequestParam(defaultValue = "title") String sortBy,
                               @RequestParam(defaultValue = "asc") String sortDir,
                               @RequestParam(defaultValue = "false") boolean fetchPrices,
                               Model model) {
        
        // Filter by wishlist = true
        Specification<Game> spec = Specification.where((root, query, cb) -> cb.equal(root.get("wishlist"), true));
        
        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase("asc") ? 
            org.springframework.data.domain.Sort.by(sortBy).ascending() : 
            org.springframework.data.domain.Sort.by(sortBy).descending();

        List<Game> games = gameRepository.findAll(spec, sort);
        model.addAttribute("games", games);
        if (fetchPrices) {
            attachGgDealsPrices(model, games);
        } else {
            model.addAttribute("ggDealsPrices", Map.<Long, GgDealsPriceEntry>of());
            model.addAttribute("ggDealsApiCalls", Collections.emptyList());
            model.addAttribute("ggDealsSteamIdCount", 0);
        }
        model.addAttribute("fetchPrices", fetchPrices);
        
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        
        return "games/wishlist";
    }

    @GetMapping("/detail/{id}")
    public String gameDetail(@PathVariable Long id,
                             @RequestParam(defaultValue = "false") boolean editNotes,
                             Model model) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("game", game);
        model.addAttribute("editNotes", editNotes);
        model.addAttribute("notesHtml", markdownService.toSafeHtml(game.getNotes()));
        return "games/detail";
    }

    @PostMapping("/detail/{id}/notes")
    public String updateGameNotes(@PathVariable Long id, @RequestParam(value = "notes", required = false) String notes) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        game.setNotes(notes != null ? notes : "");
        gameRepository.save(game);
        return "redirect:/games/detail/" + id;
    }

    @PostMapping("/detail/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        applyStatus(game, status);
        gameRepository.save(game);
        return "redirect:/games/detail/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteGame(@PathVariable Long id) {
        Game game = gameRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean isWishlist = game.isWishlist();
        gameRepository.delete(game);
        return isWishlist ? "redirect:/games/wishlist" : "redirect:/games";
    }

    @GetMapping("/new")
    public String searchForm(@RequestParam(required = false, defaultValue = "false") boolean wishlist, Model model) {
        model.addAttribute("wishlist", wishlist);
        return "games/search";
    }

    @GetMapping("/search")
    public String searchGames(@RequestParam("query") String query, 
                              @RequestParam(required = false, defaultValue = "false") boolean wishlist,
                              Model model) {
        List<IgdbGame> results = igdbService.searchGames(query);
        model.addAttribute("results", results);
        model.addAttribute("query", query);
        model.addAttribute("wishlist", wishlist);
        return "games/search";
    }

    @GetMapping("/create")
    public String createForm(@RequestParam(value = "igdbId", required = false) Long igdbId,
                             @RequestParam(value = "title", required = false) String title,
                             @RequestParam(value = "year", required = false) Integer year,
                             @RequestParam(value = "cover", required = false) String cover,
                             @RequestParam(value = "slug", required = false) String slug,
                             @RequestParam(value = "rating", required = false) Double rating,
                             @RequestParam(value = "genre", required = false) String genreName,
                             @RequestParam(value = "platforms", required = false) String platformsParam,
                             @RequestParam(value = "wishlist", required = false, defaultValue = "false") boolean wishlist,
                             Model model) {
        
        Game game = new Game();
        game.setIgdbId(igdbId);
        game.setTitle(title);
        game.setReleaseYear(year);
        game.setCoverUrl(cover);
        game.setIgdbSlug(slug);
        game.setWishlist(wishlist);
        
        // Use rating as-is (0-100 scale) but round to 1 decimal
        if (rating != null) {
             game.setRating(Math.round(rating * 10.0) / 10.0);
        }

        if (genreName != null && !genreName.isEmpty()) {
            xyz.sanchon.jgamedatabase.model.Genre genre = genreRepository.findByName(genreName)
                    .orElseGet(() -> {
                        xyz.sanchon.jgamedatabase.model.Genre newGenre = new xyz.sanchon.jgamedatabase.model.Genre();
                        newGenre.setName(genreName);
                        return genreRepository.save(newGenre);
                    });
            game.setGenre(genre);
        }
        
        if (!wishlist) {
            applyStatus(game, "Sin empezar");
        } else {
            game.setGameStatus(null);
            game.setStatus(null);
        }

        // Steam App ID: en colección se intenta vía IGDB; en deseados no (poco fiable) — se usa búsqueda en Steam en el formulario.
        if (igdbId != null && !wishlist) {
            igdbService.findSteamAppIdForIgdbGame(igdbId).ifPresent(game::setSteamAppId);
        }
        
        // Plataformas: usar las que vienen de IGDB (creándolas en BD si no existen),
        // o todas las de BD como fallback si no hay info de IGDB.
        List<xyz.sanchon.jgamedatabase.model.Platform> platforms;
        if (platformsParam != null && !platformsParam.isBlank()) {
            platforms = Arrays.stream(platformsParam.split(","))
                    .map(String::trim)
                    .filter(name -> !name.isEmpty())
                    .map(name -> platformRepository.findByName(name)
                            .orElseGet(() -> {
                                xyz.sanchon.jgamedatabase.model.Platform p = new xyz.sanchon.jgamedatabase.model.Platform();
                                p.setName(name);
                                return platformRepository.save(p);
                            }))
                    .collect(Collectors.toList());
        } else {
            platforms = platformRepository.findAll();
        }

        model.addAttribute("game", game);
        model.addAttribute("platforms", platforms);
        model.addAttribute("genres", genreRepository.findAll());
        model.addAttribute("wishlist", wishlist);

        return "games/create";
    }

    @PostMapping("/create")
    public String createGame(@ModelAttribute("game") Game game) {
        if (game.isWishlist()) {
            game.setStatus(null);
            game.setGameStatus(null);
        } else {
            // game.getStatus() devuelve el texto del campo legacy ligado por @ModelAttribute
            applyStatus(game, game.getStatus());
        }
        gameRepository.save(game);
        return game.isWishlist() ? "redirect:/games/wishlist" : "redirect:/games";
    }

    @GetMapping("/move-to-collection/{id}")
    public String moveToCollection(@PathVariable Long id) {
        Game game = gameRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid game Id:" + id));
        game.setWishlist(false);
        applyStatus(game, "Sin empezar");
        gameRepository.save(game);
        return "redirect:/games";
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportCsv() {
        String filename = "games.csv";
        InputStreamResource file = new InputStreamResource(csvService.exportCsv());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file);
    }

    @PostMapping("/import")
    public String importCsv(@RequestParam("file") MultipartFile file, Model model) {
        try {
            csvService.importCsv(file);
        } catch (Exception e) {
            model.addAttribute("error", "Error importing CSV: " + e.getMessage());
            // Need to reload the list with an error
            return "redirect:/games?error=true";
        }
        return "redirect:/games";
    }

    private void attachGgDealsPrices(Model model, List<Game> games) {
        List<Long> steamIds = games.stream()
                .map(Game::getSteamAppId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        GgDealsFetchResult result = ggDealsService.fetchPricesBySteamAppIdsWithDebug(steamIds);
        model.addAttribute("ggDealsPrices", result.getPrices());
        model.addAttribute("ggDealsApiCalls", result.getApiCalls());
        model.addAttribute("ggDealsSteamIdCount", steamIds.size());
    }
}
