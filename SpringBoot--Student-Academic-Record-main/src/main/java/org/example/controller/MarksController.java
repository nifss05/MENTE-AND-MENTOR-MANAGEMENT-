package org.example.controller;

import jakarta.servlet.http.HttpSession;
import org.example.dto.MarksRequest;
import org.example.model.Marks;
import org.example.model.Student;
import org.example.model.User;
import org.example.repository.MarksRepository;
import org.example.repository.StudentRepository;
import org.example.service.MarksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/marks")
public class MarksController {

    @Autowired
    private MarksService marksService;

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping
    public ResponseEntity<?> getMarks(
            @RequestParam Integer sem,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Student student = studentRepository.findByUserId(user.getUserId());

        return ResponseEntity.ok(
                marksService.getMarksForSemester(student.getUsn(), sem));
    }

    @PostMapping
    public ResponseEntity<?> addMarks(
            @RequestBody MarksRequest request,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Student student = studentRepository.findByUserId(user.getUserId());

        try {
            marksService.saveOrUpdateMarks(
                    student.getUsn(),
                    request);
            return ResponseEntity.ok(Map.of(
                    "message", "Marks saved successfully",
                    "status", "success"));
        } catch (IllegalArgumentException e) {
            // Return validation error with 400 Bad Request
            return ResponseEntity.status(400).body(Map.of(
                    "message", e.getMessage(),
                    "status", "error",
                    "errorType", "VALIDATION_ERROR"));
        } catch (Exception e) {
            // Return general error with 500 Internal Server Error
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Error saving marks: " + e.getMessage(),
                    "status", "error",
                    "errorType", "SERVER_ERROR"));
        }
    }
}
