package org.example.controller;

import jakarta.servlet.http.HttpSession;
import org.example.dto.MessageRequest;
import org.example.model.Message;
import org.example.model.User;
import org.example.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestBody MessageRequest request,
            HttpSession session) {

        User mentor = (User) session.getAttribute("user");

        if (mentor == null || !"MENTOR".equalsIgnoreCase(mentor.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        System.out.println("📤 ===== MESSAGE SEND REQUEST =====");
        System.out.println("Mentor USN: " + mentor.getUsn());
        System.out.println("Student USN: " + request.getStudentUsn());
        System.out.println("Message: " + request.getMessage());

        messageService.sendMessage(
                mentor.getUsn(),
                request.getStudentUsn(),
                request.getMessage());

        System.out.println("✅ Message saved to database");
        return ResponseEntity.ok("Message sent");
    }

    @GetMapping("/student")
    public ResponseEntity<?> getStudentMessages(HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        System.out.println("📨 ===== GET STUDENT MESSAGES =====");
        System.out.println("Student USN: " + user.getUsn());

        List<Message> messages = messageService.getStudentMessages(user.getUsn());

        System.out.println("Messages found: " + messages.size());
        messages.forEach(m -> {
            System.out.println("  - Message ID: " + m.getId() + ", From: " + m.getMentorId() + ", Read: " + m.isRead());
        });

        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {

        messageService.markAsRead(id);

        return ResponseEntity.ok("Marked as read");
    }
}