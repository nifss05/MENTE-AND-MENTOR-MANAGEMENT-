package org.example.repository;

import java.util.List;

import org.example.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Student findByUserId(Long userId);

    Student findByUsn(String usn);

    List<Student> findByMentorId(String mentorId);
}
