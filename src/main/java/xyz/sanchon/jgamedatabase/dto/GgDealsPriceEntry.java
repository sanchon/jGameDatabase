package xyz.sanchon.jgamedatabase.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GgDealsPriceEntry {

    private String title;
    private String url;
    private GgDealsPriceDetails prices;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public GgDealsPriceDetails getPrices() {
        return prices;
    }

    public void setPrices(GgDealsPriceDetails prices) {
        this.prices = prices;
    }
}
