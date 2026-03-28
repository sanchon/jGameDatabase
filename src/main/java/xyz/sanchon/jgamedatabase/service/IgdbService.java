package xyz.sanchon.jgamedatabase.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import xyz.sanchon.jgamedatabase.dto.IgdbAuthResponse;
import xyz.sanchon.jgamedatabase.dto.IgdbGame;

import xyz.sanchon.jgamedatabase.dto.IgdbExternalGame;
import xyz.sanchon.jgamedatabase.dto.IgdbWebsite;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IgdbService {

    private static final Pattern STEAM_STORE_APP_ID = Pattern.compile(
            "store\\.steampowered\\.com/app/(\\d+)", Pattern.CASE_INSENSITIVE);

    private final WebClient webClient;
    
    @Value("${igdb.client-id}")
    private String clientId;

    @Value("${igdb.client-secret}")
    private String clientSecret;

    private String accessToken;
    private Instant tokenExpiration;

    public IgdbService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    private synchronized String getAccessToken() {
        if (accessToken != null && tokenExpiration != null && Instant.now().isBefore(tokenExpiration)) {
            return accessToken;
        }

        // Twitch ID endpoint for token
        String tokenUrl = "https://id.twitch.tv/oauth2/token?client_id=" + clientId + 
                          "&client_secret=" + clientSecret + 
                          "&grant_type=client_credentials";

        IgdbAuthResponse response = webClient.post()
                .uri(tokenUrl)
                .retrieve()
                .bodyToMono(IgdbAuthResponse.class)
                .block();

        if (response != null && response.getAccessToken() != null) {
            this.accessToken = response.getAccessToken();
            // Expire 1 minute before actual expiration just in case
            this.tokenExpiration = Instant.now().plusSeconds(response.getExpiresIn() - 60);
            return accessToken;
        }
        
        throw new RuntimeException("Failed to obtain IGDB access token");
    }

    public List<IgdbGame> searchGames(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String token = getAccessToken();

        // IGDB Apicalypse query
        String body = "fields name, cover.url, first_release_date, rating, slug, platforms.name, genres.name;" +
                      "search \"" + query + "\";" +
                      "limit 20;";

        return webClient.post()
                .uri("https://api.igdb.com/v4/games")
                .header("Client-ID", clientId)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "text/plain") // Important for raw body
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(IgdbGame.class)
                .collectList()
                .block();
    }

    /**
     * Fetches the Steam App ID from IGDB: first via {@code external_games} (Steam), otherwise via Steam store URL in {@code websites}.
     */
    public Optional<Long> findSteamAppIdForIgdbGame(Long igdbGameId) {
        if (igdbGameId == null) {
            return Optional.empty();
        }
        Optional<Long> fromExternal = findSteamFromExternalGames(igdbGameId);
        if (fromExternal.isPresent()) {
            return fromExternal;
        }
        return findSteamAppIdFromWebsites(igdbGameId);
    }

    private Optional<Long> findSteamFromExternalGames(Long igdbGameId) {
        String token = getAccessToken();
        String body = "fields uid; where game = " + igdbGameId + " & category = 1; limit 5;";

        List<IgdbExternalGame> rows = webClient.post()
                .uri("https://api.igdb.com/v4/external_games")
                .header("Client-ID", clientId)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "text/plain")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(IgdbExternalGame.class)
                .collectList()
                .block();

        if (rows == null) {
            return Optional.empty();
        }
        for (IgdbExternalGame row : rows) {
            if (row.getUid() == null || row.getUid().isBlank()) {
                continue;
            }
            try {
                return Optional.of(Long.parseLong(row.getUid().trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return Optional.empty();
    }

    /** Steam website category in IGDB = 13. */
    private Optional<Long> findSteamAppIdFromWebsites(Long igdbGameId) {
        String token = getAccessToken();
        String body = "fields url; where game = " + igdbGameId + " & category = 13; limit 10;";

        List<IgdbWebsite> rows = webClient.post()
                .uri("https://api.igdb.com/v4/websites")
                .header("Client-ID", clientId)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "text/plain")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(IgdbWebsite.class)
                .collectList()
                .block();

        if (rows == null) {
            return Optional.empty();
        }
        for (IgdbWebsite row : rows) {
            if (row.getUrl() == null || row.getUrl().isBlank()) {
                continue;
            }
            Matcher m = STEAM_STORE_APP_ID.matcher(row.getUrl());
            if (m.find()) {
                try {
                    return Optional.of(Long.parseLong(m.group(1)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return Optional.empty();
    }
}
