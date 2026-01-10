package com.nsdev.atlassearch.web;

import com.nsdev.atlassearch.domain.Address;
import com.nsdev.atlassearch.domain.Hotel;
import com.nsdev.atlassearch.repository.HotelRepository;
import com.nsdev.atlassearch.web.dto.HotelSearchResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class HotelControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection(name = "redis")
    @SuppressWarnings("resource")
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
            .withExposedPorts(6379);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    @Qualifier("caffeineCacheManager")
    private CacheManager l1CacheManager;

    @Autowired
    @Qualifier("redisCacheManager")
    private CacheManager l2CacheManager;

    @BeforeEach
    void setUp() {
        hotelRepository.deleteAll();
        Objects.requireNonNull(l1CacheManager.getCache("hotels-search")).clear();
        Objects.requireNonNull(l2CacheManager.getCache("hotels-search")).clear();

        var hotel = Hotel.builder()
                .name("Original Name")
                .description("Best testing hotel")
                .stars(5)
                .address(new Address("Rua Teste", "Lisbon", "Portugal"))
                .hasPool(true)
                .hasGym(true)
                .build();

        hotelRepository.save(hotel);
    }

    @Test
    @DisplayName("Should verify L1, L2 and DB tiers")
    void shouldVerifyCacheTiers() {
        String url = "/hotels/search?city=Lisbon";

        ResponseEntity<HotelSearchResponse> response1 = restTemplate.getForEntity(url, HotelSearchResponse.class);
        Assertions.assertNotNull(response1.getBody());
        assertThat(response1.getBody().getHotels().getFirst().getName()).isEqualTo("Original Name");

        Hotel hotel = hotelRepository.findAll().getFirst();
        hotel.setName("DB HACKED");
        hotelRepository.saveAndFlush(hotel);


        ResponseEntity<HotelSearchResponse> responseL1 = restTemplate.getForEntity(url, HotelSearchResponse.class);
        Assertions.assertNotNull(responseL1.getBody());
        assertThat(responseL1.getBody().getHotels().getFirst().getName())
                .as("Should come from L1 (Original)")
                .isEqualTo("Original Name");

        Objects.requireNonNull(l1CacheManager.getCache("hotels-search")).clear();

        ResponseEntity<HotelSearchResponse> responseL2 = restTemplate.getForEntity(url, HotelSearchResponse.class);
        Assertions.assertNotNull(responseL2.getBody());
        assertThat(responseL2.getBody().getHotels().getFirst().getName())
                .as("Should come from L2/Redis (Original)")
                .isEqualTo("Original Name");


        Objects.requireNonNull(l2CacheManager.getCache("hotels-search")).clear();
        Objects.requireNonNull(l1CacheManager.getCache("hotels-search")).clear();

        ResponseEntity<HotelSearchResponse> responseDB = restTemplate.getForEntity(url, HotelSearchResponse.class);
        Assertions.assertNotNull(responseDB.getBody());
        assertThat(responseDB.getBody().getHotels().getFirst().getName())
                .as("Should come from DB (Hacked)")
                .isEqualTo("DB HACKED");
    }
}