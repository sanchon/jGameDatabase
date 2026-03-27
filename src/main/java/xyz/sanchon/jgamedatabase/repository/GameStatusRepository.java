package xyz.sanchon.jgamedatabase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.sanchon.jgamedatabase.model.GameStatus;

import java.util.Optional;

public interface GameStatusRepository extends JpaRepository<GameStatus, Long> {
    Optional<GameStatus> findByName(String name);
}
