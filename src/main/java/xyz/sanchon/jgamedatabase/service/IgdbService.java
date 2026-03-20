package xyz.sanchon.jgamedatabase.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import xyz.sanchon.jgamedatabase.dto.IgdbAuthResponse;
import xyz.sanchon.jgamedatabase.dto.IgdbGame;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class IgdbService {

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
        String body = "fields name, cover.url, first_release_date, rating, slug, platforms.name;" +
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
}
