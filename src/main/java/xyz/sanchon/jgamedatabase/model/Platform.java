package xyz.sanchon.jgamedatabase.model;

import jakarta.persistence.*;

@Entity
@Table(name = "platforms")
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String metacriticSlug;

    public Platform() {
    }

    public Platform(String name, String metacriticSlug) {
        this.name = name;
        this.metacriticSlug = metacriticSlug;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMetacriticSlug() {
        return metacriticSlug;
    }

    public void setMetacriticSlug(String metacriticSlug) {
        this.metacriticSlug = metacriticSlug;
    }
}
