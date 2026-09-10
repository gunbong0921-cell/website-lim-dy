# 05. 아이디 · 비밀번호 찾기

| 항목 | 내용 |
|---|---|
| 관련 | [기능종합](./overview.md), [reCAPTCHA v3](../security/03-recaptcha-v3.md), [HMAC](../security/01-hmac-request-signing.md) |

**구현:** 가입 이메일로 안내 메일 발송.

| 기능 | 화면 | 유스케이스 | 결과 |
|---|---|---|---|
| 아이디 찾기 | `FindIdPage` | `FindLoginIdService` (캡차 `forgot_id`) | 로그인 아이디 메일 |
| 비밀번호 찾기 | `ForgotPasswordPage` | `PasswordResetService` (캡차 `forgot_password`) | 임시 비밀번호 메일 |

메일 포트는 `MailSender` → `SpringMailSender` 한 곳만 탄다. 캡차·HMAC용 IP는 `RequestClientIp` → `ClientAddressPolicy`.

일회용 메일 차단은 이 경로에 적용하지 않는다. 이미 가입된 주소로만 보낸다.
