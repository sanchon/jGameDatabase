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
 * Se ejecuta al arrancar la aplicación:
 * 1. Garantiza que los estados canónicos existen en game_statuses.
 * 2. Migra los juegos que todavía tienen el estado en texto (columna legacy)
 *    sin tener status_id asignado.
 */
@Component
@Order(2)
public class StatusMigrationService implements CommandLineRunner {

    static final List<String> CANONICAL = List.of(
            "Sin empezar", "Jugando", "Terminado", "Abandonado"
    );

    /** Mapa de texto legacy (en minúsculas) → nombre canónico */
    private static final Map<String, String> LEGACY = Map.ofEntries(
            Map.entry("sin empezar", "Sin empezar"),
            Map.entry("backlog",     "Sin empezar"),
            Map.entry("jugando",     "Jugando"),
            Map.entry("playing",     "Jugando"),
            Map.entry("terminado",   "Terminado"),
            Map.entry("completado",  "Terminado"),
            Map.entry("completed",   "Terminado"),
            Map.entry("done",        "Terminado"),
            Map.entry("abandonado",  "Abandonado"),
            Map.entry("abandoned",   "Abandonado"),
            Map.entry("dropped",     "Abandonado")
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
        int migrated = migrateTextStatus();
        if (migrated > 0) {
            System.out.println("[StatusMigration] " + migrated + " juego(s) migrados al nuevo sistema de estados.");
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
        // Busca juegos con texto de estado pero sin status_id asignado aún.
        // La consulta falla con gracia si alguna columna no existe (esquema muy antiguo).
        List<Map<String, Object>> rows;
        try {
            rows = jdbc.queryForList(
                    "SELECT id, status FROM games WHERE status_id IS NULL AND status IS NOT NULL"
            );
        } catch (Exception e) {
            return 0; // columna status_id aún no existe — se reintentará en el próximo arranque
        }

        int count = 0;
        for (Map<String, Object> row : rows) {
            Long gameId = ((Number) row.get("id")).longValue();
            String oldStatus = (String) row.get("status");
            String canonical = LEGACY.getOrDefault(
                    oldStatus.toLowerCase().trim(), "Sin empezar"
            );
            GameStatus gs = statusRepository.findByName(canonical)
                    .orElseGet(() -> statusRepository.findByName("Sin empezar").orElseThrow());
            jdbc.update("UPDATE games SET status_id = ? WHERE id = ?", gs.getId(), gameId);
            count++;
        }
        return count;
    }
}
