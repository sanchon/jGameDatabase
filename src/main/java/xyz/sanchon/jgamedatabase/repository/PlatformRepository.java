package xyz.sanchon.jgamedatabase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.sanchon.jgamedatabase.model.Platform;

import java.util.List;
import java.util.Optional;

public interface PlatformRepository extends JpaRepository<Platform, Long> {
    Optional<Platform> findByName(String name);

    @Query("SELECT p FROM Platform p WHERE p.id NOT IN (SELECT DISTINCT g.platform.id FROM Game g WHERE g.platform IS NOT NULL)")
    List<Platform> findUnused();
}
