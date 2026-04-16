package org.example.service;

import jakarta.transaction.Transactional;
import org.example.dto.MarksRequest;
import org.example.dto.SubjectMarks;
import org.example.model.Marks;
import org.example.model.Student;
import org.example.repository.MarksRepository;
import org.example.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MarksService {
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MarksRepository marksRepository;

    // ============================
    // SAVE / UPDATE MARKS
    // ============================
    @Transactional
    public void saveOrUpdateMarks(String usn, MarksRequest request) {

        marksRepository.deleteByStudentUsnAndSemester(
                usn,
                request.getSemester());
        Student student = studentRepository.findByUsn(usn);
        if (student == null || student.getMentorId() == null) {
            throw new IllegalStateException("Student mentor not assigned");
        }
        request.getSubjects().forEach(s -> {
            // ============================
            // VALIDATE MARKS CONSTRAINTS
            // ============================
            validateMarksConstraints(s);

            Marks m = new Marks();
            m.setStudentUsn(usn);
            m.setSemester(request.getSemester());
            m.setSubject(s.getName());
            m.setMse1(s.getMse1());
            m.setMse2(s.getMse2());
            m.setTask(s.getTask());
            m.setSee(s.getSee());
            m.setTotalMarks(s.getTotal());
            m.setGrade(s.getGrade());
            m.setValidated(false);
            m.setActivityPoints(0);
            m.setMentorId(student.getMentorId());
            m.setStatus("submitted"); // Set initial status to submitted

            marksRepository.save(m);
        });
    }

    // ============================
    // VALIDATE MARKS CONSTRAINTS
    // ============================
    private void validateMarksConstraints(SubjectMarks s) {
        StringBuilder errors = new StringBuilder();

        // Validate MSE1 (max 20)
        if (s.getMse1() != null && s.getMse1() > 20) {
            errors.append("MSE 1 should not exceed 20. ");
        }
        // Validate MSE2 (max 20)
        if (s.getMse2() != null && s.getMse2() > 20) {
            errors.append("MSE 2 should not exceed 20. ");
        }
        // Validate Task (max 10)
        if (s.getTask() != null && s.getTask() > 10) {
            errors.append("Task score should not exceed 10. ");
        }
        // Validate SEE (max 50)
        if (s.getSee() != null && s.getSee() > 50) {
            errors.append("SEE score should not exceed 50. ");
        }

        // Validate Total (max 100, which is MSE1+MSE2+Task+SEE = 20+20+10+50 = 100)
        int mse1 = s.getMse1() != null ? s.getMse1() : 0;
        int mse2 = s.getMse2() != null ? s.getMse2() : 0;
        int task = s.getTask() != null ? s.getTask() : 0;
        int see = s.getSee() != null ? s.getSee() : 0;
        int total = mse1 + mse2 + task + see;

        if (total > 100) {
            errors.append("Total marks should not exceed 100. ");
        }

        // Throw exception if any validation failed
        if (errors.length() > 0) {
            throw new IllegalArgumentException(
                    "Invalid marks for subject '" + s.getName() + "': " + errors.toString().trim());
        }
    }

    // ============================
    // GET MARKS FOR SEMESTER
    // ============================
    public Map<String, Object> getMarksForSemester(String usn, Integer sem) {

        List<Marks> marks = marksRepository.findByStudentUsnAndSemester(usn, sem);

        return Map.of(
                "semester", sem,
                "subjects", marks.stream().map(m -> Map.of(
                        "name", m.getSubject(),
                        "mse1", m.getMse1(),
                        "mse2", m.getMse2(),
                        "task", m.getTask(),
                        "see", m.getSee(),
                        "total", m.getTotalMarks(),
                        "grade", m.getGrade(),
                        "validated", m.getValidated())).toList());
    }

    public Map<String, Integer> getPerformance(Integer semester, String mentorId) {

        Object result = marksRepository.getPerformanceStats(semester, mentorId);

        Map<String, Integer> map = new HashMap<>();

        if (result == null) {
            map.put("high", 0);
            map.put("average", 0);
            map.put("low", 0);
            return map;
        }

        Object[] row;

        // 🔥 HANDLE NESTED ARRAY CASE
        if (result instanceof Object[] arr && arr.length == 1 && arr[0] instanceof Object[]) {
            row = (Object[]) arr[0];
        } else {
            row = (Object[]) result;
        }

        map.put("high", row[0] != null ? ((Number) row[0]).intValue() : 0);
        map.put("average", row[1] != null ? ((Number) row[1]).intValue() : 0);
        map.put("low", row[2] != null ? ((Number) row[2]).intValue() : 0);

        return map;
    }
}
