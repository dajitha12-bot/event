package com.eventmanagement.controller;

import com.eventmanagement.model.*;
import com.eventmanagement.service.EventService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private EventService eventService;

    @Autowired
    private com.eventmanagement.repository.NotificationRepository notificationRepository;

    @Autowired
    private com.eventmanagement.repository.StudentRepository studentRepository;

    // ──── Auth ─────────────────────────────────────────────────────────────────
    @GetMapping("/login")
    public String loginPage() { return "student/login"; }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpSession session, RedirectAttributes ra) {
        try {
            Student student = eventService.loginStudent(email, password);
            session.setAttribute("studentId", student.getId());
            session.setAttribute("studentName", student.getName());
            return "redirect:/student/dashboard";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/student/login";
        }
    }

    @GetMapping("/register")
    public String registerPage() { return "student/register"; }

    @PostMapping("/register")
    public String register(@RequestParam String name, @RequestParam String email,
                           @RequestParam String password, @RequestParam String department,
                           RedirectAttributes ra) {
        try {
            eventService.registerStudent(name, email, password, department);
            ra.addFlashAttribute("success", "Account created! Please login.");
            return "redirect:/student/login";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/student/register";
        }
    }

    // ──── Dashboard ────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId == null) return "redirect:/student/login";
        model.addAttribute("studentName", session.getAttribute("studentName"));
        model.addAttribute("availableEvents", eventService.getAvailableEvents());
        model.addAttribute("myRegistrations", eventService.getStudentRegistrations(studentId));
        return "student/dashboard";
    }

    // ──── Events ───────────────────────────────────────────────────────────────
    @GetMapping("/events")
    public String events(HttpSession session, Model model) {
        if (session.getAttribute("studentId") == null) return "redirect:/student/login";
        model.addAttribute("studentName", session.getAttribute("studentName"));
        model.addAttribute("events", eventService.getAvailableEvents());
        model.addAttribute("paymentMethods", eventService.getPaymentMethods());
        return "student/events";
    }

    @GetMapping("/events/{id}/register")
    public String registerPage(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("studentId") == null) return "redirect:/student/login";
        model.addAttribute("event", eventService.getEventById(id));
        model.addAttribute("paymentMethods", eventService.getPaymentMethods());
        return "student/register-event";
    }

    @PostMapping("/events/{id}/register")
    public String registerForEvent(@PathVariable Long id,
                                   @RequestParam String paymentMethod,
                                   HttpSession session,
                                   RedirectAttributes ra) {
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId == null) return "redirect:/student/login";
        try {
            Map<String, String> result = eventService.registerForEvent(studentId, id, paymentMethod);
            ra.addFlashAttribute("receipt", result);
            return "redirect:/student/receipt";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/student/events";
        }
    }

    @GetMapping("/receipt")
    public String receipt(HttpSession session, Model model) {
        if (session.getAttribute("studentId") == null) return "redirect:/student/login";
        model.addAttribute("studentName", session.getAttribute("studentName"));
        return "student/receipt";
    }

    // ──── My Registrations ─────────────────────────────────────────────────────
    @GetMapping("/registrations")
    public String myRegistrations(HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId == null) return "redirect:/student/login";
        model.addAttribute("studentName", session.getAttribute("studentName"));
        model.addAttribute("registrations", eventService.getStudentRegistrations(studentId));
        return "student/registrations";
    }

    // ──── Payments ─────────────────────────────────────────────────────────────
    @GetMapping("/payments")
    public String payments(HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId == null) return "redirect:/student/login";
        model.addAttribute("studentName", session.getAttribute("studentName"));
        model.addAttribute("payments", eventService.getStudentPayments(studentId));
        return "student/payments";
    }

    // ──── Notifications ────────────────────────────────────────────────────────
    @GetMapping("/notifications")
    public String notifications(HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId == null) return "redirect:/student/login";
        
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found!"));
            
        model.addAttribute("studentName", session.getAttribute("studentName"));
        model.addAttribute("notifications", notificationRepository.findByStudentOrderByCreatedAtDesc(student));
        return "student/notifications";
    }

    // ──── Feedback ─────────────────────────────────────────────────────────────
    @PostMapping("/feedback")
    public String submitFeedback(@RequestParam Long eventId, @RequestParam int rating,
                                 @RequestParam String comments,
                                 HttpSession session, RedirectAttributes ra) {
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId == null) return "redirect:/student/login";
        try {
            eventService.submitFeedback(studentId, eventId, rating, comments);
            ra.addFlashAttribute("success", "Feedback submitted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/student/registrations";
    }
}
