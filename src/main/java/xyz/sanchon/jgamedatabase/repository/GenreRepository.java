package xyz.sanchon.jgamedatabase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.sanchon.jgamedatabase.model.Genre;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByName(String name);
}
