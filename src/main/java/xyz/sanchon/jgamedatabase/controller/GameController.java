package xyz.sanchon.jgamedatabase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import xyz.sanchon.jgamedatabase.model.Game;
import xyz.sanchon.jgamedatabase.repository.GameRepository;

import java.util.List;

@Controller
@RequestMapping("/games")
public class GameController {

    private final GameRepository gameRepository;

    public GameController(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
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

        // Update fields
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
}
