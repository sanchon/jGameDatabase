package xyz.sanchon.jgamedatabase.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import xyz.sanchon.jgamedatabase.dto.GgDealsApiCallLog;
import xyz.sanchon.jgamedatabase.dto.GgDealsFetchResult;
import xyz.sanchon.jgamedatabase.dto.GgDealsPriceEntry;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GgDealsService {

    private static final Logger log = LoggerFactory.getLogger(GgDealsService.class);
    private static final int MAX_IDS_PER_REQUEST = 100;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ggdeals.api-key}")
    private String apiKey;

    @Value("${ggdeals.region:es}")
    private String region;

    public GgDealsService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * Obtiene precios por Steam App ID e incluye trazas de petición/respuesta para depuración.
     */
    public GgDealsFetchResult fetchPricesBySteamAppIdsWithDebug(List<Long> steamAppIds) {
        Map<Long, GgDealsPriceEntry> out = new HashMap<>();
        List<GgDealsApiCallLog> logs = new ArrayList<>();

        if (steamAppIds == null || steamAppIds.isEmpty()) {
            return new GgDealsFetchResult(out, logs);
        }
        List<Long> distinct = steamAppIds.stream().filter(id -> id != null && id > 0).distinct().collect(Collectors.toList());
        if (distinct.isEmpty()) {
            return new GgDealsFetchResult(out, logs);
        }

        for (int i = 0; i < distinct.size(); i += MAX_IDS_PER_REQUEST) {
            List<Long> chunk = distinct.subList(i, Math.min(i + MAX_IDS_PER_REQUEST, distinct.size()));
            String idsParam = chunk.stream().map(String::valueOf).collect(Collectors.joining(","));

            URI uri = UriComponentsBuilder.fromUriString("https://api.gg.deals/v1/prices/by-steam-app-id/")
                    .queryParam("key", apiKey)
                    .queryParam("ids", idsParam)
                    .queryParam("region", region)
                    .build()
                    .encode()
                    .toUri();

            String maskedUrl = maskKeyInUrl(uri.toString());
            try {
                HttpExchange http = webClient.get()
                        .uri(uri)
                        .exchangeToMono(response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new HttpExchange(response.statusCode().value(), body)))
                        .block();

                if (http == null) {
                    logs.add(new GgDealsApiCallLog("GET", maskedUrl, null, null, "Respuesta vacía"));
                    continue;
                }

                logs.add(new GgDealsApiCallLog("GET", maskedUrl, http.status, prettyJsonIfPossible(http.body), null));

                JsonNode root = objectMapper.readTree(http.body);
                boolean success = root.path("success").asBoolean(false);
                JsonNode dataNode = root.get("data");
                if (!success || dataNode == null || !dataNode.isObject()) {
                    log.warn("GG.deals API success=false o data no es objeto para ids={}", idsParam);
                    continue;
                }
                Iterator<String> names = dataNode.fieldNames();
                while (names.hasNext()) {
                    String key = names.next();
                    JsonNode val = dataNode.get(key);
                    if (val == null || val.isNull()) {
                        continue;
                    }
                    try {
                        long steamId = Long.parseLong(key.trim());
                        GgDealsPriceEntry entry = objectMapper.treeToValue(val, GgDealsPriceEntry.class);
                        out.put(steamId, entry);
                    } catch (Exception ex) {
                        log.debug("Entrada GG.deals omitida para clave {}: {}", key, ex.getMessage());
                    }
                }
            } catch (Exception ex) {
                log.warn("Error al consultar GG.deals para ids={}: {}", idsParam, ex.getMessage());
                logs.add(new GgDealsApiCallLog("GET", maskedUrl, null, null, ex.getMessage()));
            }
        }
        return new GgDealsFetchResult(out, logs);
    }

    private static String maskKeyInUrl(String url) {
        if (url == null) {
            return "";
        }
        return url.replaceFirst("(key=)[^&]*", "$1<oculto>");
    }

    private String prettyJsonIfPossible(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        try {
            JsonNode node = objectMapper.readTree(raw);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception e) {
            return raw;
        }
    }

    private static final class HttpExchange {
        final int status;
        final String body;

        HttpExchange(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }
}
