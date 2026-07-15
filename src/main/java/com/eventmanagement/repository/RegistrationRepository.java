package com.eventmanagement.repository;

import com.eventmanagement.model.Event;
import com.eventmanagement.model.Registration;
import com.eventmanagement.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByStudent(Student student);
    List<Registration> findByEvent(Event event);
    Optional<Registration> findByStudentAndEvent(Student student, Event event);
    boolean existsByStudentAndEvent(Student student, Event event);
}
