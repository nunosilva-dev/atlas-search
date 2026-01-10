package com.nsdev.atlassearch.repository;

import com.nsdev.atlassearch.domain.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long>, QuerydslPredicateExecutor<Hotel> {

}