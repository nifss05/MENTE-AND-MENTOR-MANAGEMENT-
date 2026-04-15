package org.example.repository;

import org.example.dto.SubjectMarks;
import org.example.model.Marks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MarksRepository extends JpaRepository<Marks, Long> {

    List<Marks> findByValidatedFalseAndStudentUsnIn(List<String> studentUsns);

    List<Marks> findByValidatedFalseAndMentorId(String mentorId);

    List<Marks> findByStudentUsn(String studentUsn);

    List<Marks> findByStudentUsnAndSemester(String studentUsn, Integer semester);

    void deleteByStudentUsnAndSemester(String studentUsn, Integer semester);

    List<Marks> findByValidatedFalse();

    @Query("""
            SELECT new org.example.dto.SubjectMarks(m.subject, AVG(m.totalMarks))
            FROM Marks m
            WHERE m.semester = :semester
            GROUP BY m.subject
            """)
    List<SubjectMarks> getSubjectAveragesBySemester(Integer semester);

    @Query("""
            SELECT new org.example.dto.SubjectMarks(m.subject, AVG(m.totalMarks))
            FROM Marks m
            GROUP BY m.subject
            """)
    List<SubjectMarks> getSubjectAveragesAll();

    @Query(value = """
            SELECT
            SUM(CASE WHEN total_marks >= 75 THEN 1 ELSE 0 END),
            SUM(CASE WHEN total_marks BETWEEN 50 AND 74 THEN 1 ELSE 0 END),
            SUM(CASE WHEN total_marks < 50 THEN 1 ELSE 0 END)
            FROM marks
            WHERE semester = :semester
            AND mentor_id = :mentorId
            """, nativeQuery = true)
    Object[] getPerformanceStats(
            @Param("semester") Integer semester,
            @Param("mentorId") String mentorId);
}