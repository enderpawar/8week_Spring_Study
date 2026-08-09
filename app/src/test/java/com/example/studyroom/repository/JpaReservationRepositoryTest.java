package com.example.studyroom.repository;

import com.example.studyroom.domain.Reservation;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class JpaReservationRepositoryTest {

    @Autowired
    private ReservationRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndFindsReservationThroughJpaAdapter() {
        Reservation reservation = new Reservation("A101", "jinwoo");
        reservation.confirm();

        Reservation saved = repository.save(reservation);
        entityManager.flush();
        entityManager.clear();

        Reservation found = repository.findById(saved.getId()).orElseThrow();

        assertNotNull(saved.getId());
        assertEquals("A101", found.getRoomName());
        assertEquals("jinwoo", found.getRequesterName());
        assertTrue(found.isConfirmed());
    }

    @Test
    void savingExistingReservationUpdatesWithoutAddingDuplicate() {
        int countBeforeSave = repository.findAll().size();
        Reservation reservation = new Reservation("B202", "minji");
        reservation.confirm();
        Reservation saved = repository.save(reservation);
        entityManager.flush();

        saved.cancel();
        repository.save(saved);
        entityManager.flush();
        entityManager.clear();

        List<Reservation> reservations = repository.findAll();
        List<Reservation> savedIdRows = reservations.stream()
                .filter(candidate -> candidate.getId().equals(saved.getId()))
                .toList();

        assertEquals(countBeforeSave + 1, reservations.size());
        assertEquals(1, savedIdRows.size());
        assertFalse(savedIdRows.get(0).isConfirmed());
    }
}
