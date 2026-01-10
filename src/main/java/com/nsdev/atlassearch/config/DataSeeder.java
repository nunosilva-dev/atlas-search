package com.nsdev.atlassearch.config;

import com.nsdev.atlassearch.domain.Address;
import com.nsdev.atlassearch.domain.Hotel;
import com.nsdev.atlassearch.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final HotelRepository hotelRepository;

    @Bean
    @Profile("!test")
    public CommandLineRunner initDatabase() {
        return args -> {
            if (hotelRepository.count() > 0) return;

            var h1 = Hotel.builder()
                    .name("Ritz Lisbon")
                    .description("Luxury hotel in the center.")
                    .stars(5)
                    .address(new Address("Rua Rodrigo da Fonseca", "Lisbon", "Portugal"))
                    .hasPool(true).hasGym(true)
                    .build();

            var h2 = Hotel.builder()
                    .name("LowCost Porto")
                    .description("Best budget option.")
                    .stars(2)
                    .address(new Address("Rua de Santa Catarina", "Porto", "Portugal"))
                    .hasPool(false).hasGym(false)
                    .build();

            var h3 = Hotel.builder()
                    .name("Algarve Sun")
                    .description("Beach resort.")
                    .stars(4)
                    .address(new Address("Marina", "Vilamoura", "Portugal"))
                    .hasPool(true).hasGym(false)
                    .build();

            hotelRepository.saveAll(List.of(h1, h2, h3));
            log.info("🌍 Atlas Search: Database Seeded!");
        };
    }
}