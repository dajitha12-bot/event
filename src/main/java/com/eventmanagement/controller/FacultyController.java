package com.eventmanagement.controller;

import com.eventmanagement.service.EventService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/faculty")
public class FacultyController {

    @Autowired
    private EventService eventService;

    @GetMapping("/login")
    public String loginPage() { return "faculty/login"; }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpSession session, RedirectAttributes ra) {
        try {
            var faculty = eventService.loginFaculty(email, password);
            session.setAttribute("facultyId", faculty.getId());
            session.setAttribute("facultyName", faculty.getName());
            session.setAttribute("facultyDept", faculty.getDepartment());
            return "redirect:/faculty/dashboard";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/faculty/login";
        }
    }

    @GetMapping("/register")
    public String registerPage() { return "faculty/register"; }

    @PostMapping("/register")
    public String register(@RequestParam String name, @RequestParam String email,
                           @RequestParam String password, @RequestParam String department,
                           RedirectAttributes ra) {
        try {
            eventService.registerFaculty(name, email, password, department);
            ra.addFlashAttribute("success", "Faculty account created! Please login.");
            return "redirect:/faculty/login";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/faculty/register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long facultyId = (Long) session.getAttribute("facultyId");
        if (facultyId == null) return "redirect:/faculty/login";
        model.addAttribute("facultyName", session.getAttribute("facultyName"));
        model.addAttribute("facultyDept", session.getAttribute("facultyDept"));
        model.addAttribute("stats", eventService.getFacultyStats(facultyId));
        model.addAttribute("myEvents", eventService.getFacultyEventsById(facultyId));
        return "faculty/dashboard";
    }

    @GetMapping("/events")
    public String events(HttpSession session, Model model) {
        Long facultyId = (Long) session.getAttribute("facultyId");
        if (facultyId == null) return "redirect:/faculty/login";
        model.addAttribute("facultyName", session.getAttribute("facultyName"));
        model.addAttribute("myEvents", eventService.getFacultyEventsById(facultyId));
        return "faculty/events";
    }

    @GetMapping("/events/create")
    public String createEventPage(HttpSession session, Model model) {
        if (session.getAttribute("facultyId") == null) return "redirect:/faculty/login";
        model.addAttribute("facultyName", session.getAttribute("facultyName"));
        model.addAttribute("venues", eventService.getVenues());
        return "faculty/create-event";
    }

    @PostMapping("/events/create")
    public String createEvent(@RequestParam String name, @RequestParam String time,
                              @RequestParam String venue, @RequestParam int seats,
                              @RequestParam double fee,
                              @RequestParam(required = false, defaultValue = "") String description,
                              HttpSession session, RedirectAttributes ra) {
        Long facultyId = (Long) session.getAttribute("facultyId");
        if (facultyId == null) return "redirect:/faculty/login";
        try {
            eventService.createEvent(name, time, venue, seats, fee, description, facultyId);
            ra.addFlashAttribute("success", "Event created! Awaiting admin approval.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/faculty/events";
    }

    @GetMapping("/events/{id}/students")
    public String viewStudents(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("facultyId") == null) return "redirect:/faculty/login";
        model.addAttribute("facultyName", session.getAttribute("facultyName"));
        model.addAttribute("event", eventService.getEventById(id));
        model.addAttribute("registrations", eventService.getEventRegistrations(id));
        return "faculty/event-students";
    }

    @GetMapping("/earnings")
    public String earnings(HttpSession session, Model model) {
        Long facultyId = (Long) session.getAttribute("facultyId");
        if (facultyId == null) return "redirect:/faculty/login";
        model.addAttribute("facultyName", session.getAttribute("facultyName"));
        model.addAttribute("stats", eventService.getFacultyStats(facultyId));
        model.addAttribute("myEvents", eventService.getFacultyEventsById(facultyId));
        return "faculty/earnings";
    }
}
