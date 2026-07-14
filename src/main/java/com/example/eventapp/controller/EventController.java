package com.example.eventapp.controller;

import com.example.eventapp.model.Event;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.EventRepository;
import com.example.eventapp.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class EventController {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventController(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        List<Event> userEvents = eventRepository.findByOwner(user);
        
        long totalEvents = userEvents.size();
        long upcomingEvents = userEvents.stream()
                .filter(e -> e.getEventDate().isAfter(LocalDate.now().minusDays(1)))
                .count();

        model.addAttribute("totalEvents", totalEvents);
        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("events", userEvents);

        return "dashboard";
    }

    @GetMapping("/events")
    public String listEvents(Principal principal, Model model) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        List<Event> userEvents = eventRepository.findByOwnerOrderByEventDateAsc(user);
        model.addAttribute("events", userEvents);
        return "events";
    }

    @GetMapping("/events/create")
    public String showCreateForm(Model model) {
        model.addAttribute("event", new Event());
        return "create-event";
    }

    @PostMapping("/events/create")
    public String createEvent(@Valid @ModelAttribute("event") Event event,
                              BindingResult result,
                              Principal principal,
                              Model model) {
        if (result.hasErrors()) {
            return "create-event";
        }

        // Time Conflict Check
        if (hasTimeConflict(event, null)) {
            result.rejectValue("eventTime", "conflict", 
                               "Time conflict: Another event is scheduled at this venue during this time window.");
            return "create-event";
        }

        User user = getCurrentUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        event.setOwner(user);
        eventRepository.save(event);

        return "redirect:/events?created=true";
    }

    @GetMapping("/events/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Principal principal, Model model) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        Event event = eventRepository.findById(id).orElse(null);
        if (event == null || !event.getOwner().getId().equals(user.getId())) {
            return "redirect:/events?error=unauthorized";
        }

        model.addAttribute("event", event);
        return "edit-event";
    }

    @PostMapping("/events/edit/{id}")
    public String updateEvent(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("event") Event event,
                              BindingResult result,
                              Principal principal,
                              Model model) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        Event existingEvent = eventRepository.findById(id).orElse(null);
        if (existingEvent == null || !existingEvent.getOwner().getId().equals(user.getId())) {
            return "redirect:/events?error=unauthorized";
        }

        if (result.hasErrors()) {
            return "edit-event";
        }

        // Time Conflict Check (ignoring current event)
        if (hasTimeConflict(event, id)) {
            result.rejectValue("eventTime", "conflict", 
                               "Time conflict: Another event is scheduled at this venue during this time window.");
            return "edit-event";
        }

        existingEvent.setTitle(event.getTitle());
        existingEvent.setDescription(event.getDescription());
        existingEvent.setVenue(event.getVenue());
        existingEvent.setEventDate(event.getEventDate());
        existingEvent.setOrganizerName(event.getOrganizerName());
        existingEvent.setOrganizerEmail(event.getOrganizerEmail());
        existingEvent.setEventTime(event.getEventTime());
        existingEvent.setDuration(event.getDuration());

        eventRepository.save(existingEvent);

        return "redirect:/events?updated=true";
    }

    @GetMapping("/events/view/{id}")
    public String viewEventDetails(@PathVariable("id") Long id, Principal principal, Model model) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        Event event = eventRepository.findById(id).orElse(null);
        if (event == null || !event.getOwner().getId().equals(user.getId())) {
            return "redirect:/events?error=unauthorized";
        }

        model.addAttribute("event", event);
        return "view-event";
    }

    @PostMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable("id") Long id, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        Event event = eventRepository.findById(id).orElse(null);
        if (event == null || !event.getOwner().getId().equals(user.getId())) {
            return "redirect:/events?error=unauthorized";
        }

        eventRepository.delete(event);
        return "redirect:/events?deleted=true";
    }

    // Helper method to detect overlaps
    private boolean hasTimeConflict(Event newEvent, Long ignoreEventId) {
        if (newEvent.getEventDate() == null || newEvent.getVenue() == null || 
            newEvent.getEventTime() == null || newEvent.getDuration() == null) {
            return false;
        }
        
        List<Event> existingEvents = eventRepository.findByEventDateAndVenue(newEvent.getEventDate(), newEvent.getVenue());
        
        LocalDateTime newStart = newEvent.getEventDate().atTime(newEvent.getEventTime());
        LocalDateTime newEnd = newStart.plusMinutes(newEvent.getDuration());

        for (Event existing : existingEvents) {
            if (ignoreEventId != null && existing.getId().equals(ignoreEventId)) {
                continue;
            }
            
            LocalDateTime extStart = existing.getEventDate().atTime(existing.getEventTime());
            LocalDateTime extEnd = extStart.plusMinutes(existing.getDuration());
            
            // Interval overlap logic: StartA < EndB AND StartB < EndA
            if (newStart.isBefore(extEnd) && extStart.isBefore(newEnd)) {
                return true;
            }
        }
        return false;
    }
}
