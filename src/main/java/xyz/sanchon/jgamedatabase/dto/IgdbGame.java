package xyz.sanchon.jgamedatabase.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IgdbGame {
    private Long id;
    private String name;
    
    @JsonProperty("first_release_date")
    private Long firstReleaseDate;
    
    private Double rating;
    private String slug;
    private String summary;
    
    @JsonProperty("cover")
    private IgdbCover cover;
    
    @JsonProperty("platforms")
    private List<IgdbPlatform> platforms;

    @JsonProperty("genres")
    private List<IgdbGenre> genres;
    
    @JsonProperty("total_rating")
    private Double totalRating;

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

    public String getFirstReleaseDate() {
        if (firstReleaseDate == null) return null;
        // Convert timestamp to year string or full date
        Date date = new Date(firstReleaseDate * 1000);
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public IgdbCover getCover() {
        return cover;
    }

    public void setCover(IgdbCover cover) {
        this.cover = cover;
    }
    
    // Helper method for Thymeleaf
    public String getCoverUrl() {
        if (cover != null && cover.getUrl() != null) {
            // IGDB returns urls like "//images.igdb.com/..."
            String url = cover.getUrl();
            if (url.startsWith("//")) {
                url = "https:" + url;
            }
            // Replace thumb with cover_big for better quality
            return url.replace("t_thumb", "t_cover_big");
        }
        return null;
    }

    public List<IgdbPlatform> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<IgdbPlatform> platforms) {
        this.platforms = platforms;
    }

    public List<IgdbGenre> getGenres() {
        return genres;
    }

    public void setGenres(List<IgdbGenre> genres) {
        this.genres = genres;
    }

    public Double getTotalRating() {
        return totalRating != null ? totalRating : rating;
    }

    public void setTotalRating(Double totalRating) {
        this.totalRating = totalRating;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
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
    
    @JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IgdbGenre {
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
