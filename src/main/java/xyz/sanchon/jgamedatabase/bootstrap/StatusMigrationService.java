package xyz.sanchon.jgamedatabase.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import xyz.sanchon.jgamedatabase.model.GameStatus;
import xyz.sanchon.jgamedatabase.repository.GameStatusRepository;

import java.util.List;
import java.util.Map;

/**
 * Runs at application startup:
 * 1. Ensures that the canonical statuses exist in game_statuses.
 * 2. Migrates games that still have their status as text (legacy column)
 *    without a status_id assigned.
 */
@Component
@Order(2)
public class StatusMigrationService implements CommandLineRunner {

    static final List<String> CANONICAL = List.of(
            "Not started", "Playing", "Completed", "Abandoned"
    );

    /** Map of legacy text (lowercase) → canonical name */
    private static final Map<String, String> LEGACY = Map.ofEntries(
            Map.entry("not started", "Not started"),
            Map.entry("sin empezar", "Not started"),
            Map.entry("backlog",     "Not started"),
            Map.entry("playing",     "Playing"),
            Map.entry("jugando",     "Playing"),
            Map.entry("completed",   "Completed"),
            Map.entry("terminado",   "Completed"),
            Map.entry("completado",  "Completed"),
            Map.entry("done",        "Completed"),
            Map.entry("abandoned",   "Abandoned"),
            Map.entry("abandonado",  "Abandoned"),
            Map.entry("dropped",     "Abandoned")
    );

    private final GameStatusRepository statusRepository;
    private final JdbcTemplate jdbc;

    public StatusMigrationService(GameStatusRepository statusRepository, JdbcTemplate jdbc) {
        this.statusRepository = statusRepository;
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        seedCanonicalStatuses();
        migrateStatusIdFromLegacyNames();
        int migrated = migrateTextStatus();
        if (migrated > 0) {
            System.out.println("[StatusMigration] " + migrated + " game(s) migrated to the new status system.");
        }
        clearLegacyStatusField();
    }

    /**
     * Updates the status_id FK of games that already have a status_id pointing to a legacy
     * (non-canonical) GameStatus name (e.g. "Sin empezar" → "Not started").
     * This handles the case where canonical names changed between versions.
     */
    private void migrateStatusIdFromLegacyNames() {
        List<GameStatus> allStatuses = statusRepository.findAll();
        for (GameStatus gs : allStatuses) {
            String canonical = LEGACY.get(gs.getName().toLowerCase().trim());
            if (canonical != null && !canonical.equals(gs.getName())) {
                statusRepository.findByName(canonical).ifPresent(canonicalGs ->
                    jdbc.update(
                        "UPDATE games SET status_id = ? WHERE status_id = ?",
                        canonicalGs.getId(), gs.getId()
                    )
                );
            }
        }
    }

    private void seedCanonicalStatuses() {
        for (String name : CANONICAL) {
            if (statusRepository.findByName(name).isEmpty()) {
                statusRepository.save(new GameStatus(name));
            }
        }
    }

    private int migrateTextStatus() {
        // Find games with a text status but no status_id assigned yet.
        // The query fails gracefully if a column does not exist (very old schema).
        List<Map<String, Object>> rows;
        try {
            rows = jdbc.queryForList(
                    "SELECT id, status FROM games WHERE status_id IS NULL AND status IS NOT NULL"
            );
        } catch (Exception e) {
            return 0; // status_id column does not exist yet — will retry on next startup
        }

        int count = 0;
        for (Map<String, Object> row : rows) {
            Long gameId = ((Number) row.get("id")).longValue();
            String oldStatus = (String) row.get("status");
            String canonical = LEGACY.getOrDefault(
                    oldStatus.toLowerCase().trim(), "Not started"
            );
            GameStatus gs = statusRepository.findByName(canonical)
                    .orElseGet(() -> statusRepository.findByName("Not started").orElseThrow());
            jdbc.update("UPDATE games SET status_id = ?, status = NULL WHERE id = ?", gs.getId(), gameId);
            count++;
        }
        return count;
    }

    /** Sets the legacy field to NULL for all games that already have a status_id assigned. */
    private void clearLegacyStatusField() {
        try {
            jdbc.update("UPDATE games SET status = NULL WHERE status_id IS NOT NULL AND status IS NOT NULL");
        } catch (Exception ignored) {
            // If the status column does not exist in this schema it is simply ignored
        }
    }
}
