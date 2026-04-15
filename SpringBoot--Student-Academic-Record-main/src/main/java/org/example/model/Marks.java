package org.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "marks")
public class Marks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long markId;

    @Column(nullable = false)
    private String studentUsn;

    @Column(name = "mentor_id")
    private String mentorId;
    private Integer semester;

    private String subject;

    private Integer mse1;
    private Integer mse2;
    private Integer task;
    private Integer see;

    private Integer totalMarks;
    private String grade;
    private Integer activityPoints;
    private Boolean validated = false;

    // --- Getters and Setters ---
    public Long getMarkId() {
        return markId;
    }

    public void setMarkId(Long markId) {
        this.markId = markId;
    }

    public String getStudentUsn() {
        return studentUsn;
    }

    public void setStudentUsn(String studentUsn) {
        this.studentUsn = studentUsn;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }

    public Boolean getValidated() {
        return validated;
    }

    public void setValidated(Boolean validated) {
        this.validated = validated;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public Integer getMse1() {
        return mse1;
    }

    public void setMse1(Integer mse1) {
        this.mse1 = mse1;
    }

    public Integer getMse2() {
        return mse2;
    }

    public void setMse2(Integer mse2) {
        this.mse2 = mse2;
    }

    public Integer getTask() {
        return task;
    }

    public void setTask(Integer task) {
        this.task = task;
    }

    public Integer getSee() {
        return see;
    }

    public void setSee(Integer see) {
        this.see = see;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Integer getActivityPoints() {
        return activityPoints;
    }

    public void setActivityPoints(Integer activityPoints) {
        this.activityPoints = activityPoints;
    }

    public String getMentorId() {
        return mentorId;
    }

    // Setter
    public void setMentorId(String mentorId) {
        this.mentorId = mentorId;
    }

}
