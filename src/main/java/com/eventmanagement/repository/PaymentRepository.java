package com.eventmanagement.repository;

import com.eventmanagement.model.Payment;
import com.eventmanagement.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStudent(Student student);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.student = :student")
    Double getTotalSpentByStudent(Student student);
}
