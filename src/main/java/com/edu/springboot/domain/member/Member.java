package com.edu.springboot.domain.member;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "HEXAQ_MEMBER")
@Getter
@Setter
@NoArgsConstructor
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "login_id", nullable = false, unique = true, length = 50)
	private String loginId;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(nullable = false, length = 30)
	private String phone;

	@Column(length = 100)
	private String company;

	@Column(name = "email_verified", nullable = false, length = 1)
	private String emailVerified = "N";

	@Column(name = "verification_code", length = 20)
	private String verificationCode;

	@Column(name = "verification_expires")
	private LocalDateTime verificationExpires;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public boolean isEmailVerified() {
		return "Y".equals(emailVerified);
	}

	public void markEmailVerified() {
		this.emailVerified = "Y";
		this.verificationCode = null;
		this.verificationExpires = null;
	}

	public boolean matchesVerification(String code, LocalDateTime now) {
		return code != null
			&& code.equals(verificationCode)
			&& verificationExpires != null
			&& now.isBefore(verificationExpires);
	}

	public void changePassword(String encodedPassword) {
		this.password = encodedPassword;
	}

	public void updateProfile(String name, String phone, String company) {
		this.name = name;
		this.phone = phone;
		this.company = company;
	}

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = createdAt;
		if (emailVerified == null) {
			emailVerified = "N";
		}
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
