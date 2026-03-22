package xyz.sanchon.jgamedatabase.controller;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import xyz.sanchon.jgamedatabase.dto.IgdbGame;
import xyz.sanchon.jgamedatabase.model.Game;
import xyz.sanchon.jgamedatabase.repository.GameRepository;
import xyz.sanchon.jgamedatabase.repository.GenreRepository;
import xyz.sanchon.jgamedatabase.repository.PlatformRepository;
import xyz.sanchon.jgamedatabase.service.CsvService;
import xyz.sanchon.jgamedatabase.service.IgdbService;

import java.util.List;

@Controller
@RequestMapping("/games")
public class GameController {

    private final GameRepository gameRepository;
    private final PlatformRepository platformRepository;
    private final GenreRepository genreRepository;
    private final IgdbService igdbService;
    private final CsvService csvService;

    public GameController(GameRepository gameRepository, PlatformRepository platformRepository, GenreRepository genreRepository, IgdbService igdbService, CsvService csvService) {
        this.gameRepository = gameRepository;
        this.platformRepository = platformRepository;
        this.genreRepository = genreRepository;
        this.igdbService = igdbService;
        this.csvService = csvService;
    }

    @GetMapping
    public String listGames(@RequestParam(required = false) String status,
                            @RequestParam(required = false) Long genreId,
                            @RequestParam(required = false) Long platformId,
                            Model model) {
        
        Specification<Game> spec = Specification.where(null);
        
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        
        if (genreId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("genre").get("id"), genreId));
        }
        
        if (platformId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("platform").get("id"), platformId));
        }

        List<Game> games = gameRepository.findAll(spec);
        model.addAttribute("games", games);
        model.addAttribute("genres", genreRepository.findAll());
        model.addAttribute("platforms", platformRepository.findAll());
        
        // Pass back current filters to the model
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedGenre", genreId);
        model.addAttribute("selectedPlatform", platformId);
        
        return "games/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Game game = gameRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid game Id:" + id));
        model.addAttribute("game", game);
        return "games/edit";
    }

    @PostMapping("/update/{id}")
    public String updateGame(@PathVariable Long id, @ModelAttribute("game") Game game) {
        Game existingGame = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid game Id:" + id));

        // Update fields available in the edit form
        existingGame.setStatus(game.getStatus());
        existingGame.setNotes(game.getNotes());
        
        // Save changes
        gameRepository.save(existingGame);
        return "redirect:/games";
    }

    @GetMapping("/delete/{id}")
    public String deleteGame(@PathVariable Long id) {
        Game game = gameRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid game Id:" + id));
        gameRepository.delete(game);
        return "redirect:/games";
    }

    @GetMapping("/new")
    public String searchForm() {
        return "games/search";
    }

    @GetMapping("/search")
    public String searchGames(@RequestParam("query") String query, Model model) {
        List<IgdbGame> results = igdbService.searchGames(query);
        model.addAttribute("results", results);
        model.addAttribute("query", query);
        return "games/search";
    }

    @GetMapping("/create")
    public String createForm(@RequestParam(value = "igdbId", required = false) Long igdbId,
                             @RequestParam(value = "title", required = false) String title,
                             @RequestParam(value = "year", required = false) Integer year,
                             @RequestParam(value = "cover", required = false) String cover,
                             Model model) {
        
        Game game = new Game();
        game.setIgdbId(igdbId);
        game.setTitle(title);
        game.setReleaseYear(year);
        game.setCoverUrl(cover);
        // Default status
        game.setStatus("Backlog");
        
        model.addAttribute("game", game);
        model.addAttribute("platforms", platformRepository.findAll());
        model.addAttribute("genres", genreRepository.findAll());
        
        return "games/create";
    }

    @PostMapping("/create")
    public String createGame(@ModelAttribute("game") Game game) {
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
}
