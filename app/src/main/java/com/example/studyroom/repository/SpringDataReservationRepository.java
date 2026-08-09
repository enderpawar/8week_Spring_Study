package com.example.studyroom.repository;


import com.example.studyroom.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataReservationRepository extends JpaRepository<Reservation, Long> {
}