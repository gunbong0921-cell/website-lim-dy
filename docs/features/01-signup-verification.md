# 01. 회원가입 · 본인 인증

| 항목 | 내용 |
|---|---|
| 관련 | [기능종합](./overview.md), [본인인증](../security/06-identity-verification.md), [허니팟](../security/04-honeypot-field.md), [일회용 메일](../security/05-disposable-email.md), [reCAPTCHA v3](../security/03-recaptcha-v3.md) |

**구현:** 가입 전에 이메일 또는 휴대폰 중 **하나**를 골라 6자리 인증을 끝낸다.

| 구분 | 내용 |
|---|---|
| 화면 | `SignUpPage` · 개인/기업 탭 · 이메일/휴대폰 탭 |
| 훅 | `useSignUp` · `useEmailVerification` · `usePhoneVerification` · `useRecaptcha` |
| API | `POST /api/members/email|phone/send-code` · `/verify` · `/signup` |
| 유스케이스 | 허니팟 → `VerifyCaptchaService` → `RejectDisposableEmailService` → `Send*VerificationService` → `Verify*CodeService` → `SignUpService` |
| 규칙 | `PhoneVerificationPolicy` (TTL 180초, 재전송 60초, 일 10회). 이메일·휴대폰 공통 |
| 포트 | `VerificationStore` (키: 번호 또는 `MAIL:{email}`) |
| DTO | `SendVerificationResult`, `VerifyCodeResult` (`verificationToken`, `target`) |
| IP | `RequestClientIp` → `ClientAddressPolicy` |
| 구현 | Solapi SMS · SMTP 메일 · Redis(또는 메모리) 코드 저장 |

**흐름**

1. 탭으로 인증 수단 선택
2. 코드 발송 전 reCAPTCHA v3 (`phone_send_code` / `email_send_code`). SMS는 점수 통과 뒤에만 발송. 이메일은 일회용 도메인 차단
3. 코드 발송 → Redis에 TTL 저장
4. 코드 확인 → 일회용 토큰 발급
5. 가입 시 허니팟·캡차 `signup`·HMAC 후 토큰을 한 번 쓰고 버림
6. 이메일 인증 또는 휴대폰 인증 완료 계정만 로그인 가능. **DI는 이메일**(`email` unique). 같은 메일 재가입은 거절

기업 가입은 [02 사업자 확인](./02-business-registration.md)을 추가로 탄다.
