# 03. 로그인 · 로그아웃

| 항목 | 내용 |
|---|---|
| 관련 | [기능종합](./overview.md), [reCAPTCHA v3](../security/03-recaptcha-v3.md), [HMAC](../security/01-hmac-request-signing.md), [레이트 리밋](../security/02-rate-limiting.md), [Cloudflare Tunnel](../technical/03-cloudflare-tunnel.md) |

**구현:** 이메일(아이디) + 비밀번호. 세션 쿠키. 화면 상태는 zustand.

| 구분 | 내용 |
|---|---|
| 화면 | `LoginPage` · 아이디 저장(쿠키) |
| 훅 | `useAuth.login` → `useRecaptcha` (`login`) |
| API | `POST /api/auth/login` · `/logout` |
| 유스케이스 | `LoginService` (`VerifyCaptchaService` 먼저) |
| 세션 | `SessionPrincipal` → `MemberSessionBinder.bind`. 로그아웃은 `unbind` |
| IP | `RequestClientIp` → `ClientAddressPolicy` |
| 보안 | BCrypt · `RateLimitFilter`(로그인 분당 10회) · reCAPTCHA v3 · HMAC |

로그인 조건: **이메일 인증 완료 또는 휴대폰 인증 완료**

컨트롤러는 Infrastructure `MemberSessionBinder`를 아직 직접 주입한다. 남은 위반은 [아키텍처](../technical/01-architecture.md) §2.

터널(HTTPS)로 열면 세션 쿠키에 `Secure`가 붙는다. 로컬 HTTP와 공개 URL은 출처가 다르므로 세션이 공유되지 않는다. 공개 기동은 [Cloudflare Tunnel](../technical/03-cloudflare-tunnel.md).
