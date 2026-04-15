
package org.example.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificates")
public class Certificate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long certificateId;

	// private String filePath;

	private String studentUsn;
	private String mentorId;

	private String certificateType;
	private String filePath;

	private Integer activityPoints;
	private String status; // PENDING / APPROVED / REJECTED

	private String validatedBy;

	private LocalDateTime validatedAt;

	private Integer semester;

	private String rejectionNote;

	private LocalDateTime rejectedDate;

	public Long getCertificateId() {
		return certificateId;
	}

	public void setCertificateId(Long certificateId) {
		this.certificateId = certificateId;
	}

	public String getStudentUsn() {
		return studentUsn;
	}

	public void setStudentUsn(String studentUsn) {
		this.studentUsn = studentUsn;
	}

	public String getMentorId() {
		return mentorId;
	}

	public void setMentorId(String mentorId) {
		this.mentorId = mentorId;
	}

	public String getCertificateType() {
		return certificateType;
	}

	public void setCertificateType(String certificateType) {
		this.certificateType = certificateType;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Integer getActivityPoints() {
		return activityPoints;
	}

	public void setActivityPoints(Integer activityPoints) {
		this.activityPoints = activityPoints;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getValidatedBy() {
		return validatedBy;
	}

	public void setValidatedBy(String validatedBy) {
		this.validatedBy = validatedBy;
	}

	public LocalDateTime getValidatedAt() {
		return validatedAt;
	}

	public void setValidatedAt(LocalDateTime validatedAt) {
		this.validatedAt = validatedAt;
	}

	public Integer getSemester() {
		return semester;
	}

	public void setSemester(Integer semester) {
		this.semester = semester;
	}

	public String getRejectionNote() {
		return rejectionNote;
	}

	public void setRejectionNote(String rejectionNote) {
		this.rejectionNote = rejectionNote;
	}

	public LocalDateTime getRejectedDate() {
		return rejectedDate;
	}

	public void setRejectedDate(LocalDateTime rejectedDate) {
		this.rejectedDate = rejectedDate;
	}
}
