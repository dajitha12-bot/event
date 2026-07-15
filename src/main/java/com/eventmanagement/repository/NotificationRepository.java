package com.eventmanagement.repository;

import com.eventmanagement.model.Notification;
import com.eventmanagement.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStudentOrderByCreatedAtDesc(Student student);
}
