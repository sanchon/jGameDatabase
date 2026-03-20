package xyz.sanchon.jgamedatabase.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import xyz.sanchon.jgamedatabase.model.Game;
import xyz.sanchon.jgamedatabase.model.Genre;
import xyz.sanchon.jgamedatabase.model.Platform;
import xyz.sanchon.jgamedatabase.repository.GameRepository;
import xyz.sanchon.jgamedatabase.repository.GenreRepository;
import xyz.sanchon.jgamedatabase.repository.PlatformRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final GameRepository gameRepository;
    private final PlatformRepository platformRepository;
    private final GenreRepository genreRepository;

    public DataInitializer(GameRepository gameRepository, PlatformRepository platformRepository, GenreRepository genreRepository) {
        this.gameRepository = gameRepository;
        this.platformRepository = platformRepository;
        this.genreRepository = genreRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (gameRepository.count() == 0) {
            loadData();
        }
    }

    private void loadData() {
        Platform ps5 = new Platform("PlayStation 5");
        Platform xbox = new Platform("Xbox Series X");
        Platform switchPlat = new Platform("Nintendo Switch");
        Platform pc = new Platform("PC");

        platformRepository.save(ps5);
        platformRepository.save(xbox);
        platformRepository.save(switchPlat);
        platformRepository.save(pc);

        Genre rpg = new Genre("RPG");
        Genre action = new Genre("Action");
        Genre adventure = new Genre("Adventure");
        Genre platformer = new Genre("Platformer");

        genreRepository.save(rpg);
        genreRepository.save(action);
        genreRepository.save(adventure);
        genreRepository.save(platformer);

        Game game1 = new Game();
        game1.setTitle("The Legend of Zelda: Breath of the Wild");
        game1.setReleaseYear(2017);
        game1.setPlatform(switchPlat);
        game1.setGenre(adventure);
        game1.setStatus("Completed");
        game1.setRating(10.0);
        game1.setNotes("Masterpiece");
        gameRepository.save(game1);

        Game game2 = new Game();
        game2.setTitle("Elden Ring");
        game2.setReleaseYear(2022);
        game2.setPlatform(ps5);
        game2.setGenre(rpg);
        game2.setStatus("Playing");
        game2.setRating(9.5);
        gameRepository.save(game2);
        
        Game game3 = new Game();
        game3.setTitle("Hollow Knight");
        game3.setReleaseYear(2017);
        game3.setPlatform(pc);
        game3.setGenre(platformer);
        game3.setStatus("Backlog");
        gameRepository.save(game3);

        System.out.println("Sample data loaded...");
    }
}
