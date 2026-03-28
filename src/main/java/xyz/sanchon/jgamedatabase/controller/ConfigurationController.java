package xyz.sanchon.jgamedatabase.controller;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.sanchon.jgamedatabase.model.Genre;
import xyz.sanchon.jgamedatabase.model.Platform;
import xyz.sanchon.jgamedatabase.repository.GameRepository;
import xyz.sanchon.jgamedatabase.repository.GenreRepository;
import xyz.sanchon.jgamedatabase.repository.PlatformRepository;
import xyz.sanchon.jgamedatabase.service.AppConfigurationService;
import xyz.sanchon.jgamedatabase.service.BackupService;

import java.util.List;

@Controller
@RequestMapping("/configuration")
public class ConfigurationController {

    private static final List<String> PLATFORMS_TO_KEEP = List.of("PC", "Xbox 360", "Nintendo Switch");

    private final AppConfigurationService configService;
    private final BackupService backupService;
    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;
    private final MessageSource messageSource;

    public ConfigurationController(AppConfigurationService configService,
                                   BackupService backupService,
                                   GameRepository gameRepository,
                                   GenreRepository genreRepository,
                                   PlatformRepository platformRepository,
                                   MessageSource messageSource) {
        this.configService = configService;
        this.backupService = backupService;
        this.gameRepository = gameRepository;
        this.genreRepository = genreRepository;
        this.platformRepository = platformRepository;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String showConfiguration(Model model) {
        model.addAttribute("igdbClientId", configService.getIgdbClientId());
        model.addAttribute("igdbClientSecret", configService.getIgdbClientSecret());
        model.addAttribute("ggDealsApiKey", configService.getGgDealsApiKey());
        model.addAttribute("ggDealsRegion", configService.getGgDealsRegion());
        model.addAttribute("h2ConsoleEnabled", configService.isH2ConsoleEnabled());
        model.addAttribute("backupDir", backupService.getBackupDir());
        model.addAttribute("backups", backupService.listBackups());
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

        model.addAttribute("message", messageSource.getMessage("flash.config.saved", null, LocaleContextHolder.getLocale()));
        model.addAttribute("igdbClientId", igdbClientId);
        model.addAttribute("igdbClientSecret", igdbClientSecret);
        model.addAttribute("ggDealsApiKey", ggDealsApiKey);
        model.addAttribute("ggDealsRegion", ggDealsRegion);
        model.addAttribute("h2ConsoleEnabled", configService.isH2ConsoleEnabled());
        model.addAttribute("backupDir", backupService.getBackupDir());
        model.addAttribute("backups", backupService.listBackups());

        return "configuration";
    }

    @PostMapping("/backup")
    public String backup(RedirectAttributes redirectAttributes) {
        try {
            String path = backupService.backup();
            redirectAttributes.addFlashAttribute("message",
                    messageSource.getMessage("flash.backup.created", new Object[]{path}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("flash.backup.error", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
        }
        return "redirect:/configuration";
    }

    @PostMapping("/h2-console/toggle")
    public String toggleH2Console(RedirectAttributes redirectAttributes) {
        boolean current = configService.isH2ConsoleEnabled();
        configService.setH2ConsoleEnabled(!current);
        String key = !current ? "flash.h2.enabled" : "flash.h2.disabled";
        redirectAttributes.addFlashAttribute("message",
                messageSource.getMessage(key, null, LocaleContextHolder.getLocale()));
        return "redirect:/configuration";
    }

    @PostMapping("/cleanup")
    public String cleanupOrphans(RedirectAttributes redirectAttributes) {
        List<Platform> unusedPlatforms = platformRepository.findUnused();
        List<Genre> unusedGenres = genreRepository.findUnused();
        platformRepository.deleteAll(unusedPlatforms);
        genreRepository.deleteAll(unusedGenres);

        redirectAttributes.addFlashAttribute("message",
                messageSource.getMessage("flash.cleanup.done",
                        new Object[]{unusedPlatforms.size(), unusedGenres.size()},
                        LocaleContextHolder.getLocale()));
        return "redirect:/configuration";
    }

    @PostMapping("/reset")
    public String resetDatabase(RedirectAttributes redirectAttributes) {
        gameRepository.deleteAll();
        genreRepository.deleteAll();
        platformRepository.findAll().stream()
                .filter(p -> !PLATFORMS_TO_KEEP.contains(p.getName()))
                .forEach(platformRepository::delete);

        redirectAttributes.addFlashAttribute("message",
                messageSource.getMessage("flash.reset.done", null, LocaleContextHolder.getLocale()));
        return "redirect:/configuration";
    }
}
