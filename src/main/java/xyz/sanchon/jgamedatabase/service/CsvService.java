package xyz.sanchon.jgamedatabase.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.sanchon.jgamedatabase.model.Game;
import xyz.sanchon.jgamedatabase.model.Genre;
import xyz.sanchon.jgamedatabase.model.Platform;
import xyz.sanchon.jgamedatabase.model.GameStatus;
import xyz.sanchon.jgamedatabase.model.Store;
import xyz.sanchon.jgamedatabase.repository.GameRepository;
import xyz.sanchon.jgamedatabase.repository.GameStatusRepository;
import xyz.sanchon.jgamedatabase.repository.GenreRepository;
import xyz.sanchon.jgamedatabase.repository.PlatformRepository;
import xyz.sanchon.jgamedatabase.repository.StoreRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CsvService {

    private final GameRepository gameRepository;
    private final PlatformRepository platformRepository;
    private final GenreRepository genreRepository;
    private final GameStatusRepository gameStatusRepository;
    private final StoreRepository storeRepository;

    public CsvService(GameRepository gameRepository, PlatformRepository platformRepository,
                      GenreRepository genreRepository, GameStatusRepository gameStatusRepository,
                      StoreRepository storeRepository) {
        this.gameRepository = gameRepository;
        this.platformRepository = platformRepository;
        this.genreRepository = genreRepository;
        this.gameStatusRepository = gameStatusRepository;
        this.storeRepository = storeRepository;
    }

    public void importCsv(MultipartFile file) throws IOException {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, 
                     CSVFormat.DEFAULT.builder()
                             .setHeader()
                             .setSkipHeaderRecord(true)
                             .setIgnoreHeaderCase(true)
                             .setTrim(true)
                             .build())) {

            List<Game> gamesToSave = new ArrayList<>();

            for (CSVRecord csvRecord : csvParser) {
                Game game = new Game();
                
                String idStr = getRecordValue(csvRecord, "id");
                if (idStr != null && !idStr.isEmpty()) {
                    try {
                        Long id = Long.parseLong(idStr);
                        Optional<Game> existing = gameRepository.findById(id);
                        if (existing.isPresent()) {
                            game = existing.get();
                        } else {
                            game.setId(id);
                        }
                    } catch (NumberFormatException e) {
                        // ignore id parsing error
                    }
                }

                game.setTitle(getRecordValue(csvRecord, "title"));

                String yearStr = getRecordValue(csvRecord, "year");
                if (yearStr != null && !yearStr.isEmpty()) {
                    try {
                        game.setReleaseYear(Integer.parseInt(yearStr));
                    } catch (NumberFormatException e) {
                        game.setReleaseYear(null);
                    }
                }

                String platformName = getRecordValue(csvRecord, "platform");
                if (platformName != null && !platformName.isEmpty()) {
                    Optional<Platform> platformOpt = platformRepository.findByName(platformName);
                    Platform platform;
                    if (platformOpt.isEmpty()) {
                        platform = new Platform();
                        platform.setName(platformName);
                        platform = platformRepository.save(platform);
                    } else {
                        platform = platformOpt.get();
                    }
                    game.setPlatform(platform);
                }

                String genreName = getRecordValue(csvRecord, "genre");
                if (genreName != null && !genreName.isEmpty()) {
                    Optional<Genre> genreOpt = genreRepository.findByName(genreName);
                    Genre genre;
                    if (genreOpt.isEmpty()) {
                        genre = new Genre();
                        genre.setName(genreName);
                        genre = genreRepository.save(genre);
                    } else {
                        genre = genreOpt.get();
                    }
                    game.setGenre(genre);
                }

                String status = getRecordValue(csvRecord, "status");
                if (status != null && !status.isEmpty()) {
                    GameStatus gs = gameStatusRepository.findByName(status).orElse(null);
                    if (gs != null) {
                        game.setGameStatus(gs);
                        game.setStatus(null);
                    } else {
                        // Unknown status: save as text and let the migration resolve it
                        game.setStatus(status);
                    }
                }

                String ratingStr = getRecordValue(csvRecord, "rating");
                if (ratingStr != null && !ratingStr.isEmpty()) {
                    try {
                        game.setRating(Double.parseDouble(ratingStr));
                    } catch (NumberFormatException e) {
                        game.setRating(null);
                    }
                }

                String igdbIdStr = getRecordValue(csvRecord, "igdb_id");
                if (igdbIdStr != null && !igdbIdStr.isEmpty()) {
                    try {
                        game.setIgdbId(Long.parseLong(igdbIdStr));
                    } catch (NumberFormatException e) {
                        game.setIgdbId(null);
                    }
                }

                String steamIdStr = getRecordValue(csvRecord, "steam_app_id");
                if (steamIdStr != null && !steamIdStr.isEmpty()) {
                    try {
                        game.setSteamAppId(Long.parseLong(steamIdStr));
                    } catch (NumberFormatException e) {
                        game.setSteamAppId(null);
                    }
                }

                game.setIgdbSlug(getRecordValue(csvRecord, "igdb_slug"));
                game.setCoverUrl(getRecordValue(csvRecord, "cover_url"));
                game.setNotes(getRecordValue(csvRecord, "notes"));

                String wishlistStr = getRecordValue(csvRecord, "wishlist");
                if (wishlistStr != null && !wishlistStr.isEmpty()) {
                    game.setWishlist(Boolean.parseBoolean(wishlistStr));
                }

                String storeName = getRecordValue(csvRecord, "store");
                if (storeName != null && !storeName.isEmpty()) {
                    Store store = storeRepository.findByName(storeName).orElse(null);
                    game.setStore(store);
                }

                gamesToSave.add(game);
            }

            gameRepository.saveAll(gamesToSave);
        }
    }

    public ByteArrayInputStream exportCsv() {
        List<Game> games = gameRepository.findAll();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)),
                     CSVFormat.DEFAULT.builder()
                             .setHeader("id", "title", "year", "platform", "genre", "status", "rating", "igdb_id", "steam_app_id", "igdb_slug", "cover_url", "notes", "wishlist", "store")
                             .build())) {

            for (Game game : games) {
                csvPrinter.printRecord(
                        game.getId(),
                        game.getTitle(),
                        game.getReleaseYear(),
                        game.getPlatform() != null ? game.getPlatform().getName() : "",
                        game.getGenre() != null ? game.getGenre().getName() : "",
                        game.getGameStatus() != null ? game.getGameStatus().getName() : "",
                        game.getRating(),
                        game.getIgdbId(),
                        game.getSteamAppId(),
                        game.getIgdbSlug(),
                        game.getCoverUrl(),
                        game.getNotes(),
                        game.isWishlist(),
                        game.getStore() != null ? game.getStore().getName() : ""
                );
            }

            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("fail to export data to CSV file: " + e.getMessage());
        }
    }
    
    private String getRecordValue(CSVRecord record, String column) {
        if (record.isMapped(column)) {
            return record.get(column);
        }
        return null;
    }
}
