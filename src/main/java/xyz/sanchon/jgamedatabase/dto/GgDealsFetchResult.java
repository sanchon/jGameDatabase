package xyz.sanchon.jgamedatabase.dto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GgDealsFetchResult {

    private final Map<Long, GgDealsPriceEntry> prices;
    private final List<GgDealsApiCallLog> apiCalls;
    private final boolean cached;

    public GgDealsFetchResult(Map<Long, GgDealsPriceEntry> prices, List<GgDealsApiCallLog> apiCalls) {
        this(prices, apiCalls, false);
    }

    public GgDealsFetchResult(Map<Long, GgDealsPriceEntry> prices, List<GgDealsApiCallLog> apiCalls, boolean cached) {
        this.prices = prices != null ? prices : Collections.emptyMap();
        this.apiCalls = apiCalls != null ? apiCalls : Collections.emptyList();
        this.cached = cached;
    }

    public Map<Long, GgDealsPriceEntry> getPrices() {
        return prices;
    }

    public List<GgDealsApiCallLog> getApiCalls() {
        return apiCalls;
    }

    /** True when the result was served from the local cache instead of calling the GG.deals API. */
    public boolean isCached() {
        return cached;
    }
}
