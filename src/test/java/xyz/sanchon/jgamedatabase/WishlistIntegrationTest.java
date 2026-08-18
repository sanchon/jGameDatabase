package xyz.sanchon.jgamedatabase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import xyz.sanchon.jgamedatabase.dto.GgDealsFetchResult;
import xyz.sanchon.jgamedatabase.dto.GgDealsPriceDetails;
import xyz.sanchon.jgamedatabase.dto.GgDealsPriceEntry;
import xyz.sanchon.jgamedatabase.model.Game;
import xyz.sanchon.jgamedatabase.repository.GameRepository;
import xyz.sanchon.jgamedatabase.service.GgDealsService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @MockBean
    private GgDealsService ggDealsService;

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

    @Test
    void highlightsGameBelowTargetPrice() throws Exception {
        Game game = new Game();
        game.setTitle("Below Target Game");
        game.setWishlist(true);
        game.setTargetPrice(20.0);
        game.setSteamAppId(123L);
        gameRepository.save(game);

        GgDealsPriceDetails details = new GgDealsPriceDetails();
        details.setCurrentRetail("24.99");
        details.setCurrentKeyshops("14.99");
        details.setCurrency("EUR");

        GgDealsPriceEntry entry = new GgDealsPriceEntry();
        entry.setPrices(details);

        when(ggDealsService.fetchPricesBySteamAppIdsWithDebug(anyList()))
                .thenReturn(new GgDealsFetchResult(Map.of(123L, entry), List.of()));

        mockMvc.perform(get("/games/wishlist")
                        .param("fetchPrices", "true")
                        .header("Accept-Language", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("price-drop")))
                .andExpect(content().string(containsString("Target reached!")))
                .andExpect(content().string(containsString("1 game(s) at or below their target price!")));
    }

    @Test
    void wishlistStatsEndpointReturnsCounts() throws Exception {
        Game below = new Game();
        below.setTitle("Below");
        below.setWishlist(true);
        below.setTargetPrice(20.0);
        below.setSteamAppId(111L);
        gameRepository.save(below);

        Game noTarget = new Game();
        noTarget.setTitle("NoTarget");
        noTarget.setWishlist(true);
        gameRepository.save(noTarget);

        Game collection = new Game();
        collection.setTitle("Collection");
        collection.setWishlist(false);
        gameRepository.save(collection);

        GgDealsPriceDetails details = new GgDealsPriceDetails();
        details.setCurrentRetail("24.99");
        details.setCurrentKeyshops("14.99");
        GgDealsPriceEntry entry = new GgDealsPriceEntry();
        entry.setPrices(details);
        when(ggDealsService.fetchPricesBySteamAppIdsWithDebug(anyList()))
                .thenReturn(new GgDealsFetchResult(Map.of(111L, entry), List.of()));

        mockMvc.perform(get("/api/widgets/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wishlistTotal").value(2))
                .andExpect(jsonPath("$.wishlistWithTarget").value(1))
                .andExpect(jsonPath("$.wishlistBelowTarget").value(1));
    }
}
