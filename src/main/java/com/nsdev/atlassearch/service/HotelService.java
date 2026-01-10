package com.nsdev.atlassearch.service;

import com.nsdev.atlassearch.domain.Hotel;
import com.nsdev.atlassearch.domain.QHotel;
import com.nsdev.atlassearch.repository.HotelRepository;
import com.nsdev.atlassearch.web.dto.HotelSearchResponse;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "hotels-search", key = "{#city, #minStars, #hasPool}")
    public HotelSearchResponse searchHotels(String city, Integer minStars, Boolean hasPool) {

        QHotel qHotel = QHotel.hotel;
        BooleanBuilder builder = new BooleanBuilder();

        if (city != null) builder.and(qHotel.address.city.equalsIgnoreCase(city));
        if (minStars != null) builder.and(qHotel.stars.goe(minStars));
        if (hasPool != null) builder.and(qHotel.hasPool.eq(hasPool));

        Iterable<Hotel> result = hotelRepository.findAll(builder);
        List<Hotel> hotelList = StreamSupport.stream(result.spliterator(), false).toList();
        return new HotelSearchResponse(hotelList);
    }
}