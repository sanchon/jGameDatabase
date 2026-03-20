package xyz.sanchon.jgamedatabase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.sanchon.jgamedatabase.model.Genre;

public interface GenreRepository extends JpaRepository<Genre, Long> {
}
