package xyz.sanchon.jgamedatabase.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IgdbExternalGame {

    /** Steam store id as string (IGDB external_games.uid for category Steam). */
    private String uid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
