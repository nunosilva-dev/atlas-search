package com.nsdev.atlassearch.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Address implements Serializable {
    private String street;
    private String city;
    private String country;
}