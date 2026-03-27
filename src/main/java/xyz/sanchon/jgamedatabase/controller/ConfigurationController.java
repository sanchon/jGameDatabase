package xyz.sanchon.jgamedatabase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.sanchon.jgamedatabase.repository.GameRepository;
import xyz.sanchon.jgamedatabase.repository.GenreRepository;
import xyz.sanchon.jgamedatabase.repository.PlatformRepository;
import xyz.sanchon.jgamedatabase.service.AppConfigurationService;

import java.util.List;

@Controller
@RequestMapping("/configuration")
public class ConfigurationController {

    private static final List<String> PLATFORMS_TO_KEEP = List.of("PC", "Xbox 360", "Nintendo Switch");

    private final AppConfigurationService configService;
    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;

    public ConfigurationController(AppConfigurationService configService,
                                   GameRepository gameRepository,
                                   GenreRepository genreRepository,
                                   PlatformRepository platformRepository) {
        this.configService = configService;
        this.gameRepository = gameRepository;
        this.genreRepository = genreRepository;
        this.platformRepository = platformRepository;
    }

    @GetMapping
    public String showConfiguration(Model model) {
        model.addAttribute("igdbClientId", configService.getIgdbClientId());
        model.addAttribute("igdbClientSecret", configService.getIgdbClientSecret());
        model.addAttribute("ggDealsApiKey", configService.getGgDealsApiKey());
        model.addAttribute("ggDealsRegion", configService.getGgDealsRegion());
        return "configuration";
    }

    @PostMapping
    public String saveConfiguration(
            @RequestParam("igdbClientId") String igdbClientId,
            @RequestParam("igdbClientSecret") String igdbClientSecret,
            @RequestParam("ggDealsApiKey") String ggDealsApiKey,
            @RequestParam("ggDealsRegion") String ggDealsRegion,
            Model model) {

        configService.setIgdbClientId(igdbClientId);
        configService.setIgdbClientSecret(igdbClientSecret);
        configService.setGgDealsApiKey(ggDealsApiKey);
        configService.setGgDealsRegion(ggDealsRegion);

        model.addAttribute("message", "Configuration saved successfully!");
        model.addAttribute("igdbClientId", igdbClientId);
        model.addAttribute("igdbClientSecret", igdbClientSecret);
        model.addAttribute("ggDealsApiKey", ggDealsApiKey);
        model.addAttribute("ggDealsRegion", ggDealsRegion);

        return "configuration";
    }

    @PostMapping("/reset")
    public String resetDatabase(RedirectAttributes redirectAttributes) {
        gameRepository.deleteAll();
        genreRepository.deleteAll();
        platformRepository.findAll().stream()
                .filter(p -> !PLATFORMS_TO_KEEP.contains(p.getName()))
                .forEach(platformRepository::delete);

        redirectAttributes.addFlashAttribute("message", "Base de datos reiniciada. Se han eliminado todos los juegos y géneros. Las plataformas PC, Xbox 360 y Nintendo Switch se han conservado.");
        return "redirect:/configuration";
    }
}
