package com.edu.springboot.domain.member;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

	@Column(name = "login_id", nullable = false, unique = true, length = 100)
	private String loginId;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(nullable = false, length = 30)
	private String phone;

	@Column(length = 200)
	private String company;

	@Enumerated(EnumType.STRING)
	@Column(name = "member_type", nullable = false, length = 20)
	private MemberType memberType = MemberType.INDIVIDUAL;

	@Column(length = 300)
	private String address;

	@Column(name = "job_title", length = 100)
	private String jobTitle;

	@Column(name = "business_number", length = 20)
	private String businessNumber;

	@Column(name = "company_name", length = 200)
	private String companyName;

	@Column(name = "ceo_name", length = 50)
	private String ceoName;

	@Column(name = "workplace_address", length = 300)
	private String workplaceAddress;

	@Column(name = "terms_service", length = 1)
	private String termsService = "N";

	@Column(name = "terms_privacy", length = 1)
	private String termsPrivacy = "N";

	@Column(name = "terms_marketing", length = 1)
	private String termsMarketing = "N";

	@Column(name = "terms_corporate", length = 1)
	private String termsCorporate = "N";

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MemberRole role = MemberRole.USER;

	@Enumerated(EnumType.STRING)
	@Column(name = "oauth_provider", length = 20)
	private AuthProvider oauthProvider = AuthProvider.LOCAL;

	@Column(name = "oauth_id", length = 100)
	private String oauthId;

	@Column(length = 100)
	private String nickname;

	@Column(name = "profile_image", length = 1000)
	private String profileImage;

	@Column(name = "kakao_welcome_sent", length = 1)
	private String kakaoWelcomeSent = "N";

	@Column(name = "kakao_welcome_sent_at")
	private LocalDateTime kakaoWelcomeSentAt;

	@Column(name = "email_verified", nullable = false, length = 1)
	private String emailVerified = "N";

	@Column(name = "phone_verified", nullable = false, length = 1)
	private String phoneVerified = "N";

	@Enumerated(EnumType.STRING)
	@Column(name = "verification_channel", length = 20)
	private VerificationChannel verificationChannel = VerificationChannel.EMAIL;

	@Column(name = "verification_code", length = 20)
	private String verificationCode;

	@Column(name = "verification_expires")
	private LocalDateTime verificationExpires;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public MemberType memberType() {
		return memberType == null ? MemberType.INDIVIDUAL : memberType;
	}

	public void assignType(MemberType type) {
		this.memberType = type == null ? MemberType.INDIVIDUAL : type;
	}

	public void agreeTerms(boolean service, boolean privacy, boolean marketing, boolean corporate) {
		this.termsService = yn(service);
		this.termsPrivacy = yn(privacy);
		this.termsMarketing = yn(marketing);
		this.termsCorporate = yn(corporate);
	}

	public boolean marketingAgreed() {
		return "Y".equals(termsMarketing);
	}

	private String yn(boolean value) {
		return value ? "Y" : "N";
	}

	public MemberRole role() {
		return role == null ? MemberRole.USER : role;
	}

	public boolean isAdmin() {
		return role().isAdmin();
	}

	public void assignRole(MemberRole memberRole) {
		this.role = memberRole == null ? MemberRole.USER : memberRole;
	}

	public boolean isEmailVerified() {
		return "Y".equals(emailVerified);
	}

	public boolean isPhoneVerified() {
		return "Y".equals(phoneVerified);
	}

	public boolean isSignupVerified() {
		return isEmailVerified() || isPhoneVerified();
	}

	public void markEmailVerified() {
		this.emailVerified = "Y";
		this.verificationCode = null;
		this.verificationExpires = null;
	}

	public void markPhoneVerified() {
		this.phoneVerified = "Y";
	}

	public void assignVerificationChannel(VerificationChannel channel) {
		this.verificationChannel = channel == null ? VerificationChannel.EMAIL : channel;
	}

	public boolean matchesVerification(String code, LocalDateTime now) {
		return code != null
			&& code.equals(verificationCode)
			&& verificationExpires != null
			&& now.isBefore(verificationExpires);
	}

	public AuthProvider oauthProvider() {
		return oauthProvider == null ? AuthProvider.LOCAL : oauthProvider;
	}

	public boolean googleLinked() {
		return oauthProvider().google() && oauthId != null && !oauthId.isBlank();
	}

	public boolean socialLinked() {
		return oauthProvider().social() && oauthId != null && !oauthId.isBlank();
	}

	public void linkOauth(AuthProvider provider, String providerId) {
		this.oauthProvider = provider == null ? AuthProvider.LOCAL : provider;
		this.oauthId = providerId;
	}

	public boolean needsPhone() {
		return phone == null || phone.isBlank() || "-".equals(phone.trim());
	}

	public void applySocialProfile(String nickname, String profileImage) {
		if (nickname != null && !nickname.isBlank()) {
			this.nickname = nickname.trim();
			this.name = this.nickname.length() > 50 ? this.nickname.substring(0, 50) : this.nickname;
		}
		if (profileImage != null && !profileImage.isBlank()) {
			this.profileImage = profileImage.trim();
		}
	}

	public boolean kakaoWelcomeSent() {
		return "Y".equals(kakaoWelcomeSent);
	}

	public void recordKakaoWelcome(boolean sent) {
		this.kakaoWelcomeSent = sent ? "Y" : "N";
		this.kakaoWelcomeSentAt = LocalDateTime.now();
	}

	public void changePassword(String encodedPassword) {
		this.password = encodedPassword;
	}

	public void updateProfile(String name, String phone, String company, String address, String jobTitle,
		String workplaceAddress) {
		this.name = name;
		this.phone = phone;
		this.company = company;
		this.address = address;
		this.jobTitle = jobTitle;
		this.workplaceAddress = workplaceAddress;
	}

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = createdAt;
		if (emailVerified == null) {
			emailVerified = "N";
		}
		if (phoneVerified == null) {
			phoneVerified = "N";
		}
		if (verificationChannel == null) {
			verificationChannel = VerificationChannel.EMAIL;
		}
		if (role == null) {
			role = MemberRole.USER;
		}
		if (memberType == null) {
			memberType = MemberType.INDIVIDUAL;
		}
		if (oauthProvider == null) {
			oauthProvider = AuthProvider.LOCAL;
		}
		if (kakaoWelcomeSent == null) {
			kakaoWelcomeSent = "N";
		}
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
