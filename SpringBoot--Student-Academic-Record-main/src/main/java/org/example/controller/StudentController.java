package org.example.controller;

import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.model.Certificate;
import org.example.model.Student;
import org.example.model.User;
import org.example.repository.CertificateRepository;
import org.example.repository.StudentRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    // ===============================
    // VALIDATE MENTOR
    // ===============================
    @GetMapping("/validate-mentor/{mentorId}")
    public ResponseEntity<?> validateMentor(@PathVariable String mentorId) {

        if (mentorId == null || mentorId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Mentor ID is required"));
        }

        User mentor = userRepository.findByUsn(mentorId);

        if (mentor == null) {
            return ResponseEntity.ok(Map.of("valid", false, "message", "Mentor not found"));
        }

        if (!"MENTOR".equalsIgnoreCase(mentor.getRole())) {
            return ResponseEntity.ok(Map.of("valid", false, "message", "User is not a mentor"));
        }

        return ResponseEntity.ok(Map.of(
                "valid", true,
                "message", "Mentor found: " + mentor.getName(),
                "mentorName", mentor.getName()));
    }

    // ===============================
    // GET STUDENT PROFILE
    // ===============================
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Student student = studentRepository.findByUserId(user.getUserId());

        if (student == null) {
            // For first-time students on setup page, return user info
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getUserId());
            response.put("name", user.getName());
            response.put("usn", user.getUsn());
            response.put("email", user.getEmail());
            return ResponseEntity.ok(response);
        }

        // For existing students, include email from user object
        Map<String, Object> response = new HashMap<>();
        response.put("userId", student.getUserId());
        response.put("studentId", student.getStudentId());
        response.put("name", student.getName());
        response.put("usn", student.getUsn());
        response.put("email", user.getEmail()); // Email from User table
        response.put("department", student.getDepartment());
        response.put("academicYear", student.getAcademicYear());
        response.put("dob", student.getDob());
        response.put("mentorId", student.getMentorId());
        response.put("mentorName", student.getMentorName());

        return ResponseEntity.ok(response);
    }

    // ===============================
    // CREATE / UPDATE PROFILE
    // ===============================
    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody Student updated,
            HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Student student = studentRepository.findByUserId(user.getUserId());

        if (student == null) {
            return ResponseEntity.status(404).body("Student not found");
        }

        // Update only allowed fields
        if (updated.getName() != null)
            student.setName(updated.getName());

        if (updated.getDepartment() != null)
            student.setDepartment(updated.getDepartment());

        if (updated.getAcademicYear() != null)
            student.setAcademicYear(updated.getAcademicYear());

        if (updated.getDob() != null)
            student.setDob(updated.getDob());

        studentRepository.save(student);

        return ResponseEntity.ok("Profile updated successfully");
    }

    @PostMapping("/setup")
    public ResponseEntity<?> setupProfile(
            @RequestBody Student studentData,
            HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        // Validate mentor exists using mentorId
        String mentorId = studentData.getMentorId();
        if (mentorId == null || mentorId.isBlank()) {
            return ResponseEntity.badRequest().body("Mentor ID is required");
        }

        User mentor = userRepository.findByUsn(mentorId);
        if (mentor == null || !"MENTOR".equalsIgnoreCase(mentor.getRole())) {
            return ResponseEntity.badRequest().body("Invalid mentor ID. Mentor not found or not authorized");
        }

        Student student = studentRepository.findByUserId(user.getUserId());

        // Update User object if name changed
        String newName = studentData.getName();
        if (newName != null && !newName.isBlank() && !newName.equals(user.getName())) {
            user.setName(newName);
            userRepository.save(user);
            session.setAttribute("user", user);
        }

        if (student != null) {
            // Update existing
            if (studentData.getName() != null && !studentData.getName().isBlank()) {
                student.setName(studentData.getName());
            } else {
                student.setName(user.getName());
            }
            student.setUsn(user.getUsn());
            student.setDepartment(studentData.getDepartment());
            student.setAcademicYear(studentData.getAcademicYear());
            student.setMentorId(mentorId);
            student.setMentorName(mentor.getName());
            student.setDob(studentData.getDob());

            return ResponseEntity.ok(studentRepository.save(student));
        }

        // Create new
        student = new Student();
        student.setUserId(user.getUserId());
        if (studentData.getName() != null && !studentData.getName().isBlank()) {
            student.setName(studentData.getName());
        } else {
            student.setName(user.getName());
        }
        student.setUsn(user.getUsn());
        student.setDepartment(studentData.getDepartment());
        student.setAcademicYear(studentData.getAcademicYear());
        student.setMentorId(mentorId);
        student.setMentorName(mentor.getName());
        student.setDob(studentData.getDob());

        return ResponseEntity.ok(studentRepository.save(student));
    }

    @Autowired
    private CertificateRepository certificateRepository;

    @PostMapping(value = "/certificate/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadCertificate(
            @RequestParam("certificateType") String type,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {

        try {
            User user = (User) session.getAttribute("user");

            if (user == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            Student student = studentRepository.findByUserId(user.getUserId());

            if (student == null) {
                return ResponseEntity.badRequest()
                        .body("Please complete student setup before uploading certificates");
            }

            if (student.getMentorId() == null || student.getMentorId().isBlank()) {
                return ResponseEntity.badRequest()
                        .body("Mentor not assigned. Complete student setup.");
            }

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("No file selected");
            }

            // ===== FILE SAVE =====
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            new File(uploadDir).mkdirs();

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File dest = new File(uploadDir + fileName);
            file.transferTo(dest);

            // ===== SAVE CERTIFICATE =====
            Certificate cert = new Certificate();
            cert.setStudentUsn(student.getUsn());
            cert.setMentorId(student.getMentorId());
            cert.setCertificateType(type);
            cert.setFilePath("uploads/" + fileName);
            cert.setActivityPoints(calculatePoints(type));
            cert.setStatus("PENDING");

            certificateRepository.save(cert);

            return ResponseEntity.ok("Certificate uploaded successfully");

        } catch (Exception e) {
            e.printStackTrace(); // 👈 IMPORTANT FOR DEBUG
            return ResponseEntity.status(500).body("Server error during upload");
        }

    }

    private int calculatePoints(String type) {
        return switch (type) {
            case "NSS" -> 10;
            case "Sports" -> 15;
            case "Workshop" -> 5;
            case "Internship" -> 20;
            default -> 0;
        };
    }

    // ===============================
    // ACTIVITY POINTS
    // ===============================
    @GetMapping("/activity-points")
    public ResponseEntity<?> getActivityPoints(HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Student student = studentRepository.findByUserId(user.getUserId());

        if (student == null) {
            return ResponseEntity.ok(Map.of("totalPoints", 0));
        }

        // Get all APPROVED certificates for this student
        List<Certificate> approvedCerts = certificateRepository.findByStudentUsnAndStatus(student.getUsn(), "APPROVED");

        int totalPoints = approvedCerts.stream()
                .mapToInt(c -> c.getActivityPoints() != null ? c.getActivityPoints() : 0)
                .sum();

        return ResponseEntity.ok(Map.of("totalPoints", totalPoints));
    }

    // ===============================
    // CERTIFICATE REJECTIONS
    // ===============================
    @GetMapping("/certificate-rejections")
    public ResponseEntity<?> getCertificateRejections(HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Student student = studentRepository.findByUserId(user.getUserId());

        if (student == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // Get all REJECTED certificates for this student
        List<Certificate> rejectedCerts = certificateRepository.findByStudentUsnAndStatus(student.getUsn(), "REJECTED");

        // Map to DTOs for response
        List<Map<String, Object>> rejections = rejectedCerts.stream()
                .map(cert -> Map.of(
                        "certificateType",
                        (Object) (cert.getCertificateType() != null ? cert.getCertificateType() : "Unknown"),
                        "rejectedDate", cert.getRejectedDate() != null ? cert.getRejectedDate().toString() : "N/A",
                        "rejectionNote",
                        cert.getRejectionNote() != null ? cert.getRejectionNote() : "No note provided"))
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(rejections);
    }

    // ===============================
    // APPROVED CERTIFICATES
    // ===============================
    @GetMapping("/certificates")
    public ResponseEntity<?> getApprovedCertificates(
            @RequestParam(required = false) String status,
            HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Student student = studentRepository.findByUserId(user.getUserId());

        if (student == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        String filterStatus = status != null ? status : "APPROVED";
        List<Certificate> certs = certificateRepository.findByStudentUsnAndStatus(student.getUsn(), filterStatus);

        // Map to response objects
        List<Map<String, Object>> response = certs.stream()
                .map(cert -> Map.of(
                        "certificateId", (Object) cert.getCertificateId(),
                        "certificateType",
                        (Object) (cert.getCertificateType() != null ? cert.getCertificateType() : "Unknown"),
                        "activityPoints", (Object) (cert.getActivityPoints() != null ? cert.getActivityPoints() : 0),
                        "status", (Object) cert.getStatus(),
                        "validatedAt",
                        (Object) (cert.getValidatedAt() != null ? cert.getValidatedAt().toString() : "N/A")))
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }

}
