package com.example.eventapp.repository;

import com.example.eventapp.model.Event;
import com.example.eventapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOwner(User owner);
    List<Event> findByOwnerOrderByEventDateAsc(User owner);
    List<Event> findByEventDateAndVenue(LocalDate eventDate, String venue);
}
