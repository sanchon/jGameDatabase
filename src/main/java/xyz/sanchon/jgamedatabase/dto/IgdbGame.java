package xyz.sanchon.jgamedatabase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class IgdbGame {
    private Long id;
    private String name;
    
    @JsonProperty("first_release_date")
    private Long firstReleaseDate;
    
    private Double rating;
    private String slug;
    
    // We will extract cover url manually or map it if it comes as an object
    @JsonProperty("cover")
    private IgdbCover cover;
    
    @JsonProperty("platforms")
    private List<IgdbPlatform> platforms;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getFirstReleaseDate() {
        return firstReleaseDate;
    }

    public void setFirstReleaseDate(Long firstReleaseDate) {
        this.firstReleaseDate = firstReleaseDate;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public IgdbCover getCover() {
        return cover;
    }

    public void setCover(IgdbCover cover) {
        this.cover = cover;
    }

    public List<IgdbPlatform> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<IgdbPlatform> platforms) {
        this.platforms = platforms;
    }

    public static class IgdbCover {
        private Long id;
        private String url;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
    
    public static class IgdbPlatform {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
