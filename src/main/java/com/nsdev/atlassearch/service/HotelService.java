package com.nsdev.atlassearch.service;

import com.nsdev.atlassearch.domain.Hotel;
import com.nsdev.atlassearch.domain.QHotel;
import com.nsdev.atlassearch.repository.HotelRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;

    @Transactional(readOnly = true)
    public Iterable<Hotel> searchHotels(String city, Integer minStars, Boolean hasPool) {
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
        return hotelRepository.findAll(builder);
    }
}