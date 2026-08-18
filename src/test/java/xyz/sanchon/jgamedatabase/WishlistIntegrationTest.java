package xyz.sanchon.jgamedatabase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import xyz.sanchon.jgamedatabase.model.Game;
import xyz.sanchon.jgamedatabase.repository.GameRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WishlistIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @BeforeEach
    void cleanDatabase() {
        gameRepository.deleteAll();
    }

    @Test
    void wishlistPageLoads() throws Exception {
        mockMvc.perform(get("/games/wishlist").header("Accept-Language", "en"))
                .andExpect(status().isOk())
                .andExpect(view().name("games/wishlist"))
                .andExpect(model().attributeExists("games"))
                .andExpect(content().string(containsString("Wishlist")));
    }

    @Test
    void canAddGameToWishlist() throws Exception {
        mockMvc.perform(post("/games/create")
                        .param("title", "Test Game")
                        .param("wishlist", "true")
                        .param("targetPrice", "19.99")
                        .header("Accept-Language", "en"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/games/wishlist"));

        // The game must be persisted as a wishlist item, keeping its target price.
        List<Game> wishlist = gameRepository.findAll().stream()
                .filter(Game::isWishlist)
                .toList();
        assertThat(wishlist).hasSize(1);
        assertThat(wishlist.get(0).getTitle()).isEqualTo("Test Game");
        assertThat(wishlist.get(0).getTargetPrice()).isEqualTo(19.99);

        // And it must be rendered on the wishlist page with its target price.
        mockMvc.perform(get("/games/wishlist").header("Accept-Language", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Test Game")))
                .andExpect(content().string(containsString("19.99")));
    }
}
