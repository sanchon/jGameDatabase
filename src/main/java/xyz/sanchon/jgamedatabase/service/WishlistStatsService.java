package xyz.sanchon.jgamedatabase.service;

import org.springframework.stereotype.Service;
import xyz.sanchon.jgamedatabase.dto.GgDealsFetchResult;
import xyz.sanchon.jgamedatabase.dto.GgDealsPriceEntry;
import xyz.sanchon.jgamedatabase.dto.WishlistStats;
import xyz.sanchon.jgamedatabase.model.Game;
import xyz.sanchon.jgamedatabase.repository.GameRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistStatsService {

    private final GameRepository gameRepository;
    private final GgDealsService ggDealsService;

    public WishlistStatsService(GameRepository gameRepository, GgDealsService ggDealsService) {
        this.gameRepository = gameRepository;
        this.ggDealsService = ggDealsService;
    }

    public WishlistStats getStats() {
        List<Game> wishlist = gameRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("wishlist"), true));

        long total = wishlist.size();
        long withTarget = wishlist.stream().filter(g -> g.getTargetPrice() != null).count();
        long belowTarget = countBelowTarget(wishlist);

        return new WishlistStats(total, withTarget, belowTarget);
    }

    private long countBelowTarget(List<Game> games) {
        List<Game> eligible = games.stream()
                .filter(g -> g.getTargetPrice() != null && g.getSteamAppId() != null)
                .toList();
        if (eligible.isEmpty()) {
            return 0L;
        }

        List<Long> steamIds = eligible.stream()
                .map(Game::getSteamAppId)
                .distinct()
                .collect(Collectors.toList());

        // GgDealsService already rate-limits the real API calls internally.
        GgDealsFetchResult result = ggDealsService.fetchPricesBySteamAppIdsWithDebug(steamIds);

        long count = 0L;
        for (Game g : eligible) {
            GgDealsPriceEntry entry = result.getPrices().get(g.getSteamAppId());
            if (entry == null || entry.getPrices() == null) {
                continue;
            }
            Double best = GgDealsPriceUtils.bestCurrent(entry.getPrices());
            if (best != null && best <= g.getTargetPrice()) {
                count++;
            }
        }
        return count;
    }
}
