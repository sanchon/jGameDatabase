package xyz.sanchon.jgamedatabase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.sanchon.jgamedatabase.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByName(String name);

    @Query("SELECT g FROM Genre g WHERE g.id NOT IN (SELECT DISTINCT gm.genre.id FROM Game gm WHERE gm.genre IS NOT NULL)")
    List<Genre> findUnused();
}
