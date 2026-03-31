package xyz.sanchon.jgamedatabase.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import xyz.sanchon.jgamedatabase.model.Store;
import xyz.sanchon.jgamedatabase.repository.StoreRepository;

import java.util.List;

/**
 * Ensures all canonical stores/formats exist in the database on every startup.
 * Idempotent — only inserts entries that are not already present.
 */
@Component
@Order(3)
public class StoreSeedService implements CommandLineRunner {

    static final String PHYSICAL = "Formato físico";
    static final List<String> DIGITAL_STORES = List.of(
            "Steam", "Rockstar", "EA", "Epic", "GOG", "Ubisoft"
    );

    private final StoreRepository storeRepository;

    public StoreSeedService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    public void run(String... args) {
        ensureStore(PHYSICAL);
        for (String name : DIGITAL_STORES) {
            ensureStore(name);
        }
    }

    private void ensureStore(String name) {
        if (storeRepository.findByName(name).isEmpty()) {
            storeRepository.save(new Store(name));
        }
    }
}
