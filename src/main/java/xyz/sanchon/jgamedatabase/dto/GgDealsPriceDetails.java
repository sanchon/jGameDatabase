package xyz.sanchon.jgamedatabase.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GgDealsPriceDetails {

    @JsonProperty("currentRetail")
    private String currentRetail;

    @JsonProperty("currentKeyshops")
    private String currentKeyshops;

    @JsonProperty("historicalRetail")
    private String historicalRetail;

    @JsonProperty("historicalKeyshops")
    private String historicalKeyshops;

    private String currency;

    public String getCurrentRetail() {
        return currentRetail;
    }

    public void setCurrentRetail(String currentRetail) {
        this.currentRetail = currentRetail;
    }

    public String getCurrentKeyshops() {
        return currentKeyshops;
    }

    public void setCurrentKeyshops(String currentKeyshops) {
        this.currentKeyshops = currentKeyshops;
    }

    public String getHistoricalRetail() {
        return historicalRetail;
    }

    public void setHistoricalRetail(String historicalRetail) {
        this.historicalRetail = historicalRetail;
    }

    public String getHistoricalKeyshops() {
        return historicalKeyshops;
    }

    public void setHistoricalKeyshops(String historicalKeyshops) {
        this.historicalKeyshops = historicalKeyshops;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
