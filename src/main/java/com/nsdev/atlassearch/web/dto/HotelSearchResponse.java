package com.nsdev.atlassearch.web.dto;

import com.nsdev.atlassearch.domain.Hotel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Data Transfer Object (DTO) wrapping the search results.
 * <p>
 * <b>Architecture Note:</b> This class implements {@link Serializable}.
 * This is strictly required because this object is the return type of a {@code @Cacheable}
 * method. When the L2 Cache (Redis) is used, this object must be serialized into
 * bytes to be stored across the network.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelSearchResponse implements Serializable {
    private List<Hotel> hotels;
}