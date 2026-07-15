package com.eventmanagement.service;

import com.eventmanagement.model.*;
import com.eventmanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class EventService {

    @Autowired private EventRepository eventRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private FacultyRepository facultyRepository;
    @Autowired private RegistrationRepository registrationRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private NotificationRepository notificationRepository;

    private static final List<String> AVAILABLE_VENUES = Arrays.asList(
        "Main Auditorium", "Seminar Hall", "Conference Room",
        "Open Ground", "Lecture Hall", "Lab Complex"
    );
    private static final List<String> PAYMENT_METHODS = Arrays.asList(
        "Credit Card", "Debit Card", "UPI", "Net Banking", "Cash"
    );
    private static int regCounter = 100;
    private static int payCounter = 1000;

    // ──── Student Auth ─────────────────────────────────────────────────────────
    public Student registerStudent(String name, String email, String password, String department) {
        if (email != null && email.toLowerCase().endsWith("@organizer.com")) {
            throw new RuntimeException("Students cannot register with an @organizer.com email address!");
        }
        if (studentRepository.existsByEmail(email))
            throw new RuntimeException("Email already registered!");
        return studentRepository.save(new Student(name, email, password, department));
    }

    public Student loginStudent(String email, String password) {
        if (email != null && email.toLowerCase().endsWith("@organizer.com")) {
            throw new RuntimeException("Organizer accounts cannot log in as students!");
        }
        Student s = studentRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Student not found!"));
        if (!s.getPassword().equals(password))
            throw new RuntimeException("Invalid password!");
        return s;
    }

    // ──── Faculty Auth ─────────────────────────────────────────────────────────
    public Faculty registerFaculty(String name, String email, String password, String department) {
        if (email == null || !email.toLowerCase().endsWith("@organizer.com")) {
            throw new RuntimeException("Only email addresses ending with @organizer.com are allowed for organizers!");
        }
        if (facultyRepository.existsByEmail(email))
            throw new RuntimeException("Email already registered!");
        return facultyRepository.save(new Faculty(name, email, password, department));
    }

    public Faculty loginFaculty(String email, String password) {
        if (email == null) throw new RuntimeException("Email required");
        if (!email.toLowerCase().endsWith("@organizer.com")) {
            throw new RuntimeException("Organizer login requires an @organizer.com email address!");
        }
        Faculty f = facultyRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Faculty not found!"));
        if (!f.getPassword().equals(password))
            throw new RuntimeException("Invalid password!");
        return f;
    }

    public List<Event> getFacultyEventsById(Long facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
            .orElseThrow(() -> new RuntimeException("Faculty not found!"));
        return eventRepository.findByFaculty(faculty);
    }

    // ──── Events ───────────────────────────────────────────────────────────────
    public List<Event> getAvailableEvents() {
        return eventRepository.findByStatusAndAvailableSeatsGreaterThan("APPROVED", 0);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getPendingEvents() {
        return eventRepository.findByStatus("PENDING");
    }

    public List<Event> getFacultyEvents(Faculty faculty) {
        return eventRepository.findByFaculty(faculty);
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found!"));
    }

    @Transactional
    public Event createEvent(String name, String time, String venueName,
                             int seats, double fee, String description, Long facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
            .orElseThrow(() -> new RuntimeException("Faculty not found!"));

        // Conflict check
        if (eventRepository.existsByVenueAndTime(venueName, time)) {
            throw new RuntimeException("Venue conflict! '" + venueName + "' is already booked at this time.");
        }

        Event event = new Event(name, time, venueName, seats, faculty, fee, description);
        return eventRepository.save(event);
    }

    @Transactional
    public void approveEvent(Long eventId) {
        Event event = getEventById(eventId);
        if (!"PENDING".equals(event.getStatus()))
            throw new RuntimeException("Event is already " + event.getStatus());
        event.setStatus("APPROVED");
        event.setApprovedBy("Admin");
        eventRepository.save(event);
    }

    @Transactional
    public void rejectEvent(Long eventId) {
        Event event = getEventById(eventId);
        event.setStatus("REJECTED");
        eventRepository.save(event);
    }

    // ──── Registration + Payment ───────────────────────────────────────────────
    @Transactional
    public Map<String, String> registerForEvent(Long studentId, Long eventId, String paymentMethod) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found!"));
        Event event = getEventById(eventId);

        if (!"APPROVED".equals(event.getStatus()))
            throw new RuntimeException("Event is not approved yet!");
        if (event.getAvailableSeats() <= 0)
            throw new RuntimeException("No seats available!");
        if (registrationRepository.existsByStudentAndEvent(student, event))
            throw new RuntimeException("You are already registered for this event!");

        // Create payment
        payCounter++;
        String payId = "PAY" + payCounter;
        Payment payment = new Payment(payId, student, event, event.getRegistrationFee(), paymentMethod);
        paymentRepository.save(payment);

        // Create registration
        regCounter++;
        String regId = "REG" + regCounter;
        Registration reg = new Registration(regId, student, event, event.getRegistrationFee(), paymentMethod);
        registrationRepository.save(reg);

        // Update seats and faculty earnings
        event.setAvailableSeats(event.getAvailableSeats() - 1);
        eventRepository.save(event);

        Faculty faculty = event.getFaculty();
        if (faculty != null) {
            faculty.setTotalEarnings(faculty.getTotalEarnings() + event.getRegistrationFee());
            facultyRepository.save(faculty);
        }

        // Save notification
        try {
            Notification notification = new Notification(
                "You registered successfully for '" + event.getName() + "' at '" + event.getVenue() + "'.",
                student
            );
            notificationRepository.save(notification);
        } catch (Exception e) {
            // Ignore notification errors
        }

        Map<String, String> result = new HashMap<>();
        result.put("registrationId", regId);
        result.put("paymentId", payId);
        result.put("transactionId", payment.getTransactionId());
        result.put("amount", String.format("%.2f", event.getRegistrationFee()));
        result.put("eventName", event.getName());
        result.put("venue", event.getVenue());
        result.put("time", event.getTime());
        return result;
    }

    // ──── Feedback ─────────────────────────────────────────────────────────────
    @Transactional
    public void submitFeedback(Long studentId, Long eventId, int rating, String comments) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found!"));
        Event event = getEventById(eventId);

        Registration reg = registrationRepository.findByStudentAndEvent(student, event)
            .orElseThrow(() -> new RuntimeException("You are not registered for this event!"));

        if (reg.getFeedback() != null)
            throw new RuntimeException("You have already submitted feedback for this event!");

        reg.setRating(rating);
        reg.setFeedback(comments);
        registrationRepository.save(reg);
    }

    // ──── Data helpers ─────────────────────────────────────────────────────────
    public List<Registration> getStudentRegistrations(Long studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found!"));
        return registrationRepository.findByStudent(student);
    }

    public List<Payment> getStudentPayments(Long studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found!"));
        return paymentRepository.findByStudent(student);
    }

    public List<Registration> getEventRegistrations(Long eventId) {
        Event event = getEventById(eventId);
        return registrationRepository.findByEvent(event);
    }

    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvents", eventRepository.count());
        stats.put("totalStudents", studentRepository.count());
        stats.put("totalFaculties", facultyRepository.count());
        stats.put("totalRegistrations", registrationRepository.count());
        stats.put("pendingEvents", eventRepository.findByStatus("PENDING").size());
        stats.put("approvedEvents", eventRepository.findByStatus("APPROVED").size());

        Double revenue = eventRepository.getTotalRevenue();
        stats.put("totalRevenue", revenue != null ? revenue : 0.0);
        return stats;
    }

    public Map<String, Object> getFacultyStats(Long facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
            .orElseThrow(() -> new RuntimeException("Faculty not found!"));
        List<Event> myEvents = eventRepository.findByFaculty(faculty);

        int totalReg = 0;
        double totalRev = 0;
        int totalFeedback = 0;
        double totalRating = 0;

        for (Event e : myEvents) {
            List<Registration> regs = registrationRepository.findByEvent(e);
            totalReg += regs.size();
            totalRev += e.getTotalRevenue();
            for (Registration r : regs) {
                if (r.getFeedback() != null) {
                    totalFeedback++;
                    totalRating += r.getRating();
                }
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvents", myEvents.size());
        stats.put("totalRegistrations", totalReg);
        stats.put("totalRevenue", totalRev);
        stats.put("avgRating", totalFeedback > 0 ? String.format("%.1f", totalRating / totalFeedback) : "N/A");
        stats.put("pendingEvents", myEvents.stream().filter(e -> "PENDING".equals(e.getStatus())).count());
        stats.put("approvedEvents", myEvents.stream().filter(e -> "APPROVED".equals(e.getStatus())).count());
        return stats;
    }

    public List<String> getVenues() { return AVAILABLE_VENUES; }
    public List<String> getPaymentMethods() { return PAYMENT_METHODS; }

    // ──── Sample data initializer ──────────────────────────────────────────────
    @Transactional
    public void initSampleData() {
        if (facultyRepository.count() > 0) return; // Already initialized

        Faculty f1 = facultyRepository.save(new Faculty("Dr. Sharma", "sharma@organizer.com", "faculty123", "Computer Science"));
        Faculty f2 = facultyRepository.save(new Faculty("Dr. Patel", "patel@organizer.com", "faculty123", "Electronics"));

        Student s1 = studentRepository.save(new Student("Alice Johnson", "alice@student.edu", "student123", "Computer Science"));
        Student s2 = studentRepository.save(new Student("Bob Kumar", "bob@student.edu", "student123", "Electronics"));

        notificationRepository.save(new Notification("Welcome to the Event Management System! Browse upcoming events in the catalog and register.", s1));
        notificationRepository.save(new Notification("Welcome to the Event Management System! Browse upcoming events in the catalog and register.", s2));

        Event e1 = new Event("Tech Fest 2025", "10:00 AM - 5:00 PM, Dec 15", "Main Auditorium", 200, f1, 299.0, "Annual technology festival with workshops, competitions and keynote speakers.");
        e1.setStatus("APPROVED"); e1.setApprovedBy("Admin");
        eventRepository.save(e1);

        Event e2 = new Event("Robotics Workshop", "9:00 AM - 1:00 PM, Dec 20", "Lab Complex", 50, f2, 499.0, "Hands-on robotics workshop for beginners and intermediate learners.");
        e2.setStatus("APPROVED"); e2.setApprovedBy("Admin");
        eventRepository.save(e2);

        Event e3 = new Event("Hackathon 2025", "8:00 AM - 8:00 PM, Jan 5", "Open Ground", 300, f1, 0.0, "24-hour coding marathon. Build innovative solutions to real-world problems.");
        eventRepository.save(e3); // PENDING
    }
}
