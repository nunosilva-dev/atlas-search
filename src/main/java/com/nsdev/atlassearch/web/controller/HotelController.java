package com.nsdev.atlassearch.web.controller;

import com.nsdev.atlassearch.service.HotelService;
import com.nsdev.atlassearch.web.dto.HotelSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping("/search")
    public HotelSearchResponse search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer stars,
            @RequestParam(required = false) Boolean hasPool
    ) {
        return hotelService.searchHotels(city, stars, hasPool);
    }
}