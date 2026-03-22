package xyz.sanchon.jgamedatabase.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamStoreSearchApiResponse {

    private List<SteamStoreSearchItem> items = Collections.emptyList();

    public List<SteamStoreSearchItem> getItems() {
        return items;
    }

    public void setItems(List<SteamStoreSearchItem> items) {
        this.items = items != null ? items : Collections.emptyList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SteamStoreSearchItem {
        private String type;
        private long id;
        private String name;

        @JsonProperty("tiny_image")
        private String tinyImage;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTinyImage() {
            return tinyImage;
        }

        public void setTinyImage(String tinyImage) {
            this.tinyImage = tinyImage;
        }
    }
}
