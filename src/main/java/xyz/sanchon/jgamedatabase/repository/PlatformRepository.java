package xyz.sanchon.jgamedatabase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.sanchon.jgamedatabase.model.Platform;

import java.util.Optional;

public interface PlatformRepository extends JpaRepository<Platform, Long> {
    Optional<Platform> findByName(String name);
}
