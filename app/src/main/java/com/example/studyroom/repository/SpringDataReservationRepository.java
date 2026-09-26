package com.example.studyroom.repository;


import com.example.studyroom.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;

public interface SpringDataReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("select r from Reservation r join  fetch r.member")
    List<Reservation> findAllWithMember();
}

