package com.eventmanagement.repository;

import com.eventmanagement.model.Event;
import com.eventmanagement.model.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatus(String status);
    List<Event> findByFaculty(Faculty faculty);
    List<Event> findByStatusAndAvailableSeatsGreaterThan(String status, int seats);

    @Query("SELECT COALESCE(SUM(r.amountPaid), 0) FROM Registration r")
    Double getTotalRevenue();

    @Query("SELECT COUNT(r) FROM Registration r")
    Long getTotalRegistrations();

    boolean existsByVenueAndTime(String venue, String time);
}
