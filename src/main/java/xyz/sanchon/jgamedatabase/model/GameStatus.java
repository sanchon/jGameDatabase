package xyz.sanchon.jgamedatabase.model;

import jakarta.persistence.*;

@Entity
@Table(name = "game_statuses")
public class GameStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    public GameStatus() {}

    public GameStatus(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
