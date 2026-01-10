package com.nsdev.atlassearch.web.dto;

import com.nsdev.atlassearch.domain.Hotel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelSearchResponse implements Serializable {
    private List<Hotel> hotels;
}