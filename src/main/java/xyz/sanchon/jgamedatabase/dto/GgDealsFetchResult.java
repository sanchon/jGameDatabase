package xyz.sanchon.jgamedatabase.dto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GgDealsFetchResult {

    private final Map<Long, GgDealsPriceEntry> prices;
    private final List<GgDealsApiCallLog> apiCalls;

    public GgDealsFetchResult(Map<Long, GgDealsPriceEntry> prices, List<GgDealsApiCallLog> apiCalls) {
        this.prices = prices != null ? prices : Collections.emptyMap();
        this.apiCalls = apiCalls != null ? apiCalls : Collections.emptyList();
    }

    public Map<Long, GgDealsPriceEntry> getPrices() {
        return prices;
    }

    public List<GgDealsApiCallLog> getApiCalls() {
        return apiCalls;
    }
}
