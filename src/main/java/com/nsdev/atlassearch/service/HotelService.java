package com.nsdev.atlassearch.service;

import com.nsdev.atlassearch.domain.Hotel;
import com.nsdev.atlassearch.domain.QHotel;
import com.nsdev.atlassearch.repository.HotelRepository;
import com.nsdev.atlassearch.web.dto.HotelSearchResponse;
import com.querydsl.core.BooleanBuilder;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Service layer responsible for handling Hotel business logic.
 * <p>
 * This service acts as the orchestrator between the REST Controller, the Data Repository,
 * the Caching mechanism, and the Observability stack.
 * </p>
 */
@Service
@Observed(name = "hotel.service") // Creates a Span in Jaeger for every method call
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;

    /**
     * Searches for hotels based on dynamic criteria.
     * <p>
     * <b>Caching Strategy:</b>
     * This method is annotated with {@code @Cacheable}. The result will be stored in the
     * "hotels-search" cache. The key is a composite of the method arguments.
     * <ul>
     * <li><b>L1 Cache (Caffeine):</b> Checked first. Fast access.</li>
     * <li><b>L2 Cache (Redis):</b> Checked second. If found, L1 is backfilled.</li>
     * <li><b>DB:</b> Queried only if both caches miss.</li>
     * </ul>
     * </p>
     *
     * @param city     The city name to filter by (optional).
     * @param minStars The minimum number of stars (optional).
     * @param hasPool  Whether the hotel must have a pool (optional).
     * @return A response DTO containing the list of matching hotels.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "hotels-search", key = "{#city, #minStars, #hasPool}")
    public HotelSearchResponse searchHotels(String city, Integer minStars, Boolean hasPool) {
        QHotel qHotel = QHotel.hotel;
        BooleanBuilder builder = new BooleanBuilder();

        if (city != null) {
            builder.and(qHotel.address.city.equalsIgnoreCase(city));
        }
        if (minStars != null) {
            builder.and(qHotel.stars.goe(minStars));
        }
        if (hasPool != null) {
            builder.and(qHotel.hasPool.eq(hasPool));
        }

        Iterable<Hotel> result = hotelRepository.findAll(builder);
        List<Hotel> hotelList = StreamSupport.stream(result.spliterator(), false).toList();
        return new HotelSearchResponse(hotelList);
    }
}