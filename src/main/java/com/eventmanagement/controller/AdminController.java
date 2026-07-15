package com.eventmanagement.controller;

import com.eventmanagement.service.EventService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private EventService eventService;

    private static final String ADMIN_PASSWORD = "admin123";

    // ──── Auth ─────────────────────────────────────────────────────────────────
    @GetMapping("/login")
    public String loginPage() { return "admin/login"; }

    @PostMapping("/login")
    public String login(@RequestParam String password, HttpSession session, RedirectAttributes ra) {
        if (ADMIN_PASSWORD.equals(password)) {
            session.setAttribute("adminLoggedIn", true);
            return "redirect:/admin/dashboard";
        }
        ra.addFlashAttribute("error", "Invalid admin password!");
        return "redirect:/admin/login";
    }

    // ──── Dashboard ────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn")))
            return "redirect:/admin/login";
        model.addAttribute("stats", eventService.getAdminStats());
        model.addAttribute("pendingEvents", eventService.getPendingEvents());
        model.addAttribute("allEvents", eventService.getAllEvents());
        return "admin/dashboard";
    }

    // ──── Events ───────────────────────────────────────────────────────────────
    @GetMapping("/events")
    public String allEvents(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn")))
            return "redirect:/admin/login";
        model.addAttribute("events", eventService.getAllEvents());
        return "admin/events";
    }

    @GetMapping("/events/pending")
    public String pendingEvents(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn")))
            return "redirect:/admin/login";
        model.addAttribute("pendingEvents", eventService.getPendingEvents());
        return "admin/pending-events";
    }

    @PostMapping("/events/{id}/approve")
    public String approveEvent(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn")))
            return "redirect:/admin/login";
        try {
            eventService.approveEvent(id);
            ra.addFlashAttribute("success", "Event approved successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/events/pending";
    }

    @PostMapping("/events/{id}/reject")
    public String rejectEvent(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn")))
            return "redirect:/admin/login";
        try {
            eventService.rejectEvent(id);
            ra.addFlashAttribute("success", "Event rejected.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/events/pending";
    }

    // ──── Registrations ────────────────────────────────────────────────────────
    @GetMapping("/registrations")
    public String allRegistrations(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn")))
            return "redirect:/admin/login";
        // Show all events with their registrations
        model.addAttribute("events", eventService.getAllEvents());
        return "admin/registrations";
    }

    // ──── Financial Summary ────────────────────────────────────────────────────
    @GetMapping("/financials")
    public String financials(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn")))
            return "redirect:/admin/login";
        model.addAttribute("stats", eventService.getAdminStats());
        model.addAttribute("events", eventService.getAllEvents());
        return "admin/financials";
    }
}
