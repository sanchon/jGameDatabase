package xyz.sanchon.jgamedatabase.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.sanchon.jgamedatabase.dto.WishlistStats;
import xyz.sanchon.jgamedatabase.service.WishlistStatsService;

/**
 * JSON endpoints consumed by external dashboards (e.g. gethomepage).
 */
@RestController
public class ApiController {

    private final WishlistStatsService wishlistStatsService;

    public ApiController(WishlistStatsService wishlistStatsService) {
        this.wishlistStatsService = wishlistStatsService;
    }

    @GetMapping("/api/widgets/wishlist")
    public WishlistStats wishlistWidget() {
        return wishlistStatsService.getStats();
    }
}
