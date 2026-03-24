package xyz.sanchon.jgamedatabase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xyz.sanchon.jgamedatabase.service.AppConfigurationService;

@Controller
@RequestMapping("/configuration")
public class ConfigurationController {

    private final AppConfigurationService configService;

    public ConfigurationController(AppConfigurationService configService) {
        this.configService = configService;
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
}
