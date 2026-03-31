package xyz.sanchon.jgamedatabase.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.sanchon.jgamedatabase.dto.BatchGameEntry;
import xyz.sanchon.jgamedatabase.model.Platform;
import xyz.sanchon.jgamedatabase.model.Store;
import xyz.sanchon.jgamedatabase.repository.PlatformRepository;
import xyz.sanchon.jgamedatabase.repository.StoreRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class BatchImportService {

    private final PlatformRepository platformRepository;
    private final StoreRepository storeRepository;

    public BatchImportService(PlatformRepository platformRepository, StoreRepository storeRepository) {
        this.platformRepository = platformRepository;
        this.storeRepository = storeRepository;
    }

    /**
     * Parses a CSV with columns Nombre, Géneros, Fecha de Lanzamiento, Plataformas, Fuentes
     * and returns one BatchGameEntry per row.
     */
    public List<BatchGameEntry> parseCsv(MultipartFile file) throws IOException {
        List<BatchGameEntry> entries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(reader,
                     CSVFormat.DEFAULT.builder()
                             .setHeader()
                             .setSkipHeaderRecord(true)
                             .setIgnoreHeaderCase(true)
                             .setTrim(true)
                             .build())) {

            for (CSVRecord record : parser) {
                String name = getColumn(record, "nombre", "name");
                if (name == null || name.isBlank()) continue;

                String platformHint = getColumn(record, "plataformas", "platforms", "platform");
                String storeHint    = getColumn(record, "fuentes", "store", "tienda", "fuente");

                entries.add(new BatchGameEntry(name, platformHint, storeHint));
            }
        }
        return entries;
    }

    /**
     * Resolves a platform hint (e.g. "PC (Windows)") to a Platform entity.
     * Uses bidirectional substring matching, case-insensitive.
     */
    public Platform resolvePlatform(String hint) {
        if (hint == null || hint.isBlank()) return null;
        String h = hint.trim().toLowerCase();

        // Exact match first
        for (Platform p : platformRepository.findAll()) {
            if (p.getName().equalsIgnoreCase(hint.trim())) return p;
        }
        // Bidirectional substring: "PC (Windows)" contains "PC", or "PC" contains "pc"
        for (Platform p : platformRepository.findAll()) {
            String pn = p.getName().toLowerCase();
            if (h.contains(pn) || pn.contains(h)) return p;
        }
        return null;
    }

    /**
     * Resolves a store hint (e.g. "EA app", "Ubisoft Connect", "Epic") to a Store entity.
     * Uses bidirectional substring matching, case-insensitive.
     */
    public Store resolveStore(String hint) {
        if (hint == null || hint.isBlank()) return null;
        String h = hint.trim().toLowerCase();

        // Exact match first
        for (Store s : storeRepository.findAll()) {
            if (s.getName().equalsIgnoreCase(hint.trim())) return s;
        }
        // Bidirectional substring: "EA app" contains "EA", "Ubisoft Connect" contains "Ubisoft"
        for (Store s : storeRepository.findAll()) {
            String sn = s.getName().toLowerCase();
            if (h.contains(sn) || sn.contains(h)) return s;
        }
        return null;
    }

    // -------------------------------------------------------------------------

    private String getColumn(CSVRecord record, String... candidates) {
        for (String col : candidates) {
            if (record.isMapped(col)) {
                String val = record.get(col);
                if (val != null && !val.isBlank()) return val.trim();
            }
        }
        return null;
    }
}
