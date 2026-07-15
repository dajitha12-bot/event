package com.eventmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Event name cannot be empty")
    private String name;

    @NotBlank(message = "Event time cannot be empty")
    private String time;

    @NotBlank(message = "Venue cannot be empty")
    private String venue;

    @Min(value = 1, message = "Total seats must be at least 1")
    private Integer totalSeats;

    private Integer availableSeats;

    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @DecimalMin(value = "0.0", message = "Registration fee cannot be negative")
    @DecimalMax(value = "5000.0", message = "Registration fee cannot exceed 5000")
    private Double registrationFee = 0.0;

    private String approvedBy;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Registration> registrations = new ArrayList<>();

    public Event() {}

    public Event(String name, String time, String venue, Integer totalSeats,
                 Faculty faculty, Double registrationFee, String description) {
        this.name = name;
        this.time = time;
        this.venue = venue;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.faculty = faculty;
        this.registrationFee = registrationFee;
        this.description = description;
        this.status = "PENDING";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getRegistrationFee() { return registrationFee; }
    public void setRegistrationFee(Double registrationFee) { this.registrationFee = registrationFee; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Faculty getFaculty() { return faculty; }
    public void setFaculty(Faculty faculty) { this.faculty = faculty; }
    public List<Registration> getRegistrations() { return registrations; }
    public void setRegistrations(List<Registration> registrations) { this.registrations = registrations; }

    public int getBookedSeats() {
        return totalSeats != null && availableSeats != null ? totalSeats - availableSeats : 0;
    }

    public double getTotalRevenue() {
        return getBookedSeats() * (registrationFee != null ? registrationFee : 0.0);
    }
}
