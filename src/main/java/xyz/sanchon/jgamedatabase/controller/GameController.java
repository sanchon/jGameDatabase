package xyz.sanchon.jgamedatabase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import xyz.sanchon.jgamedatabase.dto.IgdbGame;
import xyz.sanchon.jgamedatabase.model.Game;
import xyz.sanchon.jgamedatabase.model.Genre;
import xyz.sanchon.jgamedatabase.model.Platform;
import xyz.sanchon.jgamedatabase.repository.GameRepository;
import xyz.sanchon.jgamedatabase.repository.GenreRepository;
import xyz.sanchon.jgamedatabase.repository.PlatformRepository;
import xyz.sanchon.jgamedatabase.service.IgdbService;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/games")
public class GameController {

    private final GameRepository gameRepository;
    private final PlatformRepository platformRepository;
    private final GenreRepository genreRepository;
    private final IgdbService igdbService;

    public GameController(GameRepository gameRepository, PlatformRepository platformRepository, GenreRepository genreRepository, IgdbService igdbService) {
        this.gameRepository = gameRepository;
        this.platformRepository = platformRepository;
        this.genreRepository = genreRepository;
        this.igdbService = igdbService;
    }

    @GetMapping
    public String listGames(Model model) {
        List<Game> games = gameRepository.findAll();
        model.addAttribute("games", games);
        return "games/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Game game = gameRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid game Id:" + id));
        model.addAttribute("game", game);
        return "games/edit";
    }

    @PostMapping("/update/{id}")
    public String updateGame(@PathVariable("id") Long id, @ModelAttribute("game") Game game, Model model) {
        Game existingGame = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid game Id:" + id));

        // Update fields available in edit form
        existingGame.setStatus(game.getStatus());
        existingGame.setNotes(game.getNotes());
        
        // Save changes
        gameRepository.save(existingGame);
        return "redirect:/games";
    }

    @GetMapping("/delete/{id}")
    public String deleteGame(@PathVariable("id") Long id) {
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
}
