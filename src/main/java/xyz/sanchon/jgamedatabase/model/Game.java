package xyz.sanchon.jgamedatabase.model;

import jakarta.persistence.*;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Integer releaseYear;

    @ManyToOne
    @JoinColumn(name = "platform_id")
    private Platform platform;

    @ManyToOne
    @JoinColumn(name = "genre_id")
    private Genre genre;

    private Long igdbId;
    private String coverUrl;

    /** Legacy field: status text before migration to game_statuses. Kept for compatibility with
     *  old schemas and cleared (null) after the automatic migration. */
    private String status;

    /** Normalised status as FK. Takes priority over the legacy field {@code status}. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id")
    private GameStatus gameStatus;

    private Double rating;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    private String igdbSlug;

    /** Steam App ID (for prices via GG.deals). Optional. */
    private Long steamAppId;

    // New field to differentiate between possessed and wishlist games
    // true = wishlist, false/null = possessed
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean wishlist = false;

    public Game() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public Long getIgdbId() {
        return igdbId;
    }

    public void setIgdbId(Long igdbId) {
        this.igdbId = igdbId;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    /** Returns the status name. Prioritises the normalised FK; falls back to legacy text if not yet migrated. */
    public String getStatus() {
        return gameStatus != null ? gameStatus.getName() : status;
    }

    /** Writes the legacy field. Used by {@code @ModelAttribute} and during migration. */
    public void setStatus(String status) {
        this.status = status;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getIgdbSlug() {
        return igdbSlug;
    }

    public void setIgdbSlug(String igdbSlug) {
        this.igdbSlug = igdbSlug;
    }

    public Long getSteamAppId() {
        return steamAppId;
    }

    public void setSteamAppId(Long steamAppId) {
        this.steamAppId = steamAppId;
    }

    public boolean isWishlist() {
        return wishlist;
    }

    public void setWishlist(boolean wishlist) {
        this.wishlist = wishlist;
    }

    /**
     * Generates a Metacritic-compatible slug following their rules:
     * - All lowercase.
     * - Punctuation and spaces replaced with hyphens.
     * - Special characters removed.
     */
    public String getMetacriticSlug() {
        String base = (igdbSlug != null && !igdbSlug.isEmpty()) ? igdbSlug : title;
        if (base == null) return "";

        return base.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "-") // Punctuation and special chars to hyphens
                .replaceAll("\\s+", "-")         // Spaces to hyphens
                .replaceAll("-+", "-")          // Collapse multiple hyphens
                .replaceAll("^-|-$", "");       // Strip leading or trailing hyphens
    }
}
