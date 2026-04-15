package org.example.controller;

import jakarta.annotation.Resource;
import org.example.dto.MentorPendingResponse;
import org.example.dto.SubjectMarks;
import org.example.model.*;
import jakarta.servlet.http.HttpSession;
import org.example.constants.CertificateStatus;

import org.example.model.Certificate;
import org.example.repository.MarksRepository;
import org.example.repository.CertificateRepository;
import org.example.repository.StudentRepository;
import org.example.repository.UserRepository;
import org.example.service.MarksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mentor")
public class MentorController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MarksService marksService;

    @Autowired
    private MarksRepository marksRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private StudentRepository studentRepository;

    // ===============================
    // DASHBOARD
    // ===============================
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"MENTOR".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return ResponseEntity.ok("Mentor dashboard access granted");
    }

    // ===============================
    // GET PENDING (CERTS + MARKS)
    // ===============================
    @GetMapping("/pending")
    public ResponseEntity<?> pendingAll(HttpSession session) {

        User mentor = (User) session.getAttribute("user");
        if (mentor == null || !"MENTOR".equalsIgnoreCase(mentor.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        List<Certificate> certs = certificateRepository.findByMentorIdAndStatus(
                mentor.getUsn(),
                CertificateStatus.PENDING);

        List<Marks> marks = marksRepository.findByValidatedFalseAndMentorId(
                mentor.getUsn());

        System.out.println("Mentor USN: " + mentor.getUsn());
        System.out.println("Certificates: " + certs.size());
        System.out.println("Marks: " + marks.size());
        return ResponseEntity.ok(
                new MentorPendingResponse(certs, marks));

    }

    @GetMapping("/me")
    public ResponseEntity<?> getMentor(HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null || !"MENTOR".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping("/mentees")
    public ResponseEntity<?> getMentees(HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Not logged in");
        }

        if (!"MENTOR".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Not a mentor");
        }

        if (user.getUsn() == null || user.getUsn().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Mentor USN missing");
        }

        List<Student> students = studentRepository.findByMentorId(user.getUsn());

        return ResponseEntity.ok(students);
    }

    @GetMapping("/students")
    public ResponseEntity<?> getFilteredStudents(
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            HttpSession session) {

        User mentor = (User) session.getAttribute("user");

        if (mentor == null || !"MENTOR".equalsIgnoreCase(mentor.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        List<Student> students = studentRepository.findByMentorId(mentor.getUsn());

        List<Map<String, Object>> result = students.stream()

                .filter(s -> academicYear == null || academicYear.equals(s.getAcademicYear()))
                .filter(s -> department == null || department.isEmpty()
                        || department.equalsIgnoreCase(s.getDepartment()))

                .map(s -> {

                    Map<String, Object> map = new HashMap<>();

                    map.put("name", s.getName());
                    map.put("usn", s.getUsn());
                    map.put("department", s.getDepartment());
                    map.put("academicYear", s.getAcademicYear());

                    // compute submission status
                    boolean hasMarks = !marksRepository.findByStudentUsn(s.getUsn()).isEmpty();
                    boolean hasCerts = !certificateRepository.findByStudentUsn(s.getUsn()).isEmpty();

                    String computedStatus;

                    if (hasMarks || hasCerts) {
                        computedStatus = "APPROVED";
                    } else {
                        computedStatus = "PENDING";
                    }

                    map.put("status", computedStatus);

                    return map;

                })

                .filter(m -> status == null || status.isEmpty() || status.equalsIgnoreCase((String) m.get("status")))

                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/certificate/download")
    public ResponseEntity<Resource> downloadCertificate(@RequestParam String path) throws Exception {

        File file = new File(path);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        UrlResource resource = new UrlResource(file.toURI());

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=" + file.getName())
                .body((Resource) resource);
    }

    @GetMapping("/student/{id}")
    public Map<String, Object> getStudentProfile(@PathVariable Long id, HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null || !"MENTOR".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Unauthorized");
        }

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        String usn = student.getUsn();

        List<Marks> marks = marksRepository.findByStudentUsn(usn);

        List<Certificate> certs = certificateRepository.findByStudentUsn(usn);

        Map<String, Object> response = new HashMap<>();

        response.put("student", student);
        response.put("marks", marks);
        response.put("certificates", certs);

        return response;
    }

    @GetMapping("/monitor")
    public ResponseEntity<?> monitorStats(HttpSession session) {

        User mentor = (User) session.getAttribute("user");

        if (mentor == null || !"MENTOR".equalsIgnoreCase(mentor.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String mentorId = mentor.getUsn();

        List<Student> students = studentRepository.findByMentorId(mentorId);

        int totalStudents = students.size();

        int setupCompleted = 0;

        for (Student s : students) {
            if (s.getDob() != null && s.getDepartment() != null) {
                setupCompleted++;
            }
        }

        int setupPending = totalStudents - setupCompleted;

        int pendingCertificates = certificateRepository.findByMentorIdAndStatus(
                mentorId, "PENDING").size();

        int pendingMarks = marksRepository.findByValidatedFalseAndMentorId(
                mentorId).size();

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalStudents", totalStudents);
        stats.put("setupCompleted", setupCompleted);
        stats.put("setupPending", setupPending);
        stats.put("pendingCertificates", pendingCertificates);
        stats.put("pendingMarks", pendingMarks);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/analytics")
    public Map<String, Object> getAnalytics(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester,
            HttpSession session) {
        User mentor = (User) session.getAttribute("user");
        Map<String, Object> result = new HashMap<>();

        // ============================
        // SUBJECT AVERAGES
        // ============================

        List<SubjectMarks> subjectData;

        if (semester != null)
            subjectData = marksRepository.getSubjectAveragesBySemester(semester);
        else
            subjectData = marksRepository.getSubjectAveragesAll();

        List<String> subjects = new ArrayList<>();
        List<Double> averages = new ArrayList<>();

        for (SubjectMarks row : subjectData) {
            subjects.add(row.getName());
            averages.add(row.getTotal().doubleValue());
        }

        result.put("subjects", subjects);
        result.put("marks", averages);

        // ============================
        // PERFORMANCE (FIX ADDED HERE)
        // ============================

        if (semester != null) {
            Map<String, Integer> perf = marksService.getPerformance(semester, mentor.getUsn());

            result.put("high", perf.get("high"));
            result.put("average", perf.get("average"));
            result.put("low", perf.get("low"));
        } else {
            // fallback if no semester selected
            result.put("high", 0);
            result.put("average", 0);
            result.put("low", 0);
        }

        // ============================
        // CERTIFICATES
        // ============================

        result.put("certApproved", certificateRepository.countByStatus("APPROVED"));
        result.put("certPending", certificateRepository.countByStatus("PENDING"));
        result.put("certRejected", certificateRepository.countByStatus("REJECTED"));

        System.out.println("FINAL RESPONSE: " + result);
        return result;
    }

    @PostMapping("/setup")
    public ResponseEntity<?> setupProfile(@RequestBody User updatedUser,
            HttpSession session) {

        User mentor = (User) session.getAttribute("user");

        if (mentor == null || !"MENTOR".equalsIgnoreCase(mentor.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        // update only editable fields
        if (updatedUser.getName() != null)
            mentor.setName(updatedUser.getName());

        if (updatedUser.getEmail() != null)
            mentor.setEmail(updatedUser.getEmail());

        if (updatedUser.getDepartment() != null)
            mentor.setDepartment(updatedUser.getDepartment());

        userRepository.save(mentor);

        // update session also
        session.setAttribute("user", mentor);

        return ResponseEntity.ok("Profile updated");
    }

    @PostMapping("/marks/{id}/approve")
    public ResponseEntity<?> approveMarks(@PathVariable Long id, HttpSession session) {

        User mentor = (User) session.getAttribute("user");

        if (mentor == null || !"MENTOR".equalsIgnoreCase(mentor.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Marks marks = marksRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marks not found"));

        marks.setValidated(true);

        marksRepository.save(marks);

        return ResponseEntity.ok("Marks approved");
    }

    @PostMapping("/marks/{id}/reject")
    public ResponseEntity<?> rejectMarks(@PathVariable Long id, HttpSession session) {

        User mentor = (User) session.getAttribute("user");

        if (mentor == null || !"MENTOR".equalsIgnoreCase(mentor.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        marksRepository.deleteById(id);

        return ResponseEntity.ok("Marks rejected");
    }

    @PostMapping("/certificate/{id}/approve")
    public ResponseEntity<?> approveCertificate(@PathVariable Long id, HttpSession session) {

        User mentor = (User) session.getAttribute("user");

        if (mentor == null || !"MENTOR".equalsIgnoreCase(mentor.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Certificate cert = certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        cert.setStatus(CertificateStatus.APPROVED);

        certificateRepository.save(cert);

        return ResponseEntity.ok("Certificate approved");
    }

    @PostMapping("/certificate/{id}/reject")
    public ResponseEntity<?> rejectCertificate(@PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            HttpSession session) {

        User mentor = (User) session.getAttribute("user");

        if (mentor == null || !"MENTOR".equalsIgnoreCase(mentor.getRole())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Certificate cert = certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        cert.setStatus(CertificateStatus.REJECTED);
        cert.setRejectedDate(java.time.LocalDateTime.now());

        if (body != null && body.containsKey("rejectionNote")) {
            cert.setRejectionNote(body.get("rejectionNote"));
        }

        certificateRepository.save(cert);

        return ResponseEntity.ok("Certificate rejected");
    }

}
