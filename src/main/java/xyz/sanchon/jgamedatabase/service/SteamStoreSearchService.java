package xyz.sanchon.jgamedatabase.service;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import xyz.sanchon.jgamedatabase.dto.SteamSearchHitDto;
import xyz.sanchon.jgamedatabase.dto.SteamStoreSearchApiResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Search via the public Steam store API ({@code /api/storesearch/}).
 */
@Service
public class SteamStoreSearchService {

    private static final String USER_AGENT =
            "Mozilla/5.0 (compatible; jGameDatabase/1.0; +https://github.com/)";

    private final WebClient webClient;

    public SteamStoreSearchService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build();
    }

    public List<SteamSearchHitDto> search(String term) {
        if (term == null || term.isBlank()) {
            return Collections.emptyList();
        }
        String t = term.trim();
        if (t.length() < 2) {
            return Collections.emptyList();
        }

        SteamStoreSearchApiResponse body = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("store.steampowered.com")
                        .path("/api/storesearch/")
                        .queryParam("term", t)
                        .queryParam("cc", "ES")
                        .queryParam("l", "spanish")
                        .build())
                .retrieve()
                .bodyToMono(SteamStoreSearchApiResponse.class)
                .block();

        if (body == null || body.getItems() == null || body.getItems().isEmpty()) {
            return Collections.emptyList();
        }

        return body.getItems().stream()
                .filter(item -> item.getType() == null || "app".equals(item.getType()))
                .map(item -> new SteamSearchHitDto(
                        item.getId(),
                        item.getName() != null ? item.getName() : "",
                        item.getTinyImage()))
                .collect(Collectors.toList());
    }
}
