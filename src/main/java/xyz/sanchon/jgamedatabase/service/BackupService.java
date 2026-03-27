package xyz.sanchon.jgamedatabase.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class BackupService {

    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public record BackupInfo(String filename, String displayDate, long sizeKb) {}

    @Value("${app.backup.dir}")
    private String backupDir;

    private final CsvService csvService;

    public BackupService(CsvService csvService) {
        this.csvService = csvService;
    }

    public String backup() throws IOException {
        Path dir = Paths.get(backupDir);
        Files.createDirectories(dir);

        String filename = "backup_" + LocalDateTime.now().format(FILE_TIMESTAMP) + ".csv";
        Path dest = dir.resolve(filename);

        try (InputStream in = csvService.exportCsv()) {
            Files.copy(in, dest);
        }

        return dest.toAbsolutePath().toString();
    }

    public List<BackupInfo> listBackups() {
        Path dir = Paths.get(backupDir);
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (var stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.getFileName().toString().endsWith(".csv"))
                    .sorted(Comparator.reverseOrder())
                    .map(p -> {
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
                            LocalDateTime created = attrs.lastModifiedTime()
                                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                            long sizeKb = Math.max(1, attrs.size() / 1024);
                            return new BackupInfo(p.getFileName().toString(), created.format(DISPLAY_FORMAT), sizeKb);
                        } catch (IOException e) {
                            return new BackupInfo(p.getFileName().toString(), "-", 0L);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    public String getBackupDir() {
        return Paths.get(backupDir).toAbsolutePath().toString();
    }
}