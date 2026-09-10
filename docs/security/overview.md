# Hexaq 보안 종합

| 항목 | 내용 |
|---|---|
| 목적 | 7단계 보안 레이어를 한곳에서 본다. 무료 기술 6종과 가입 전 본인인증을 겹친다 |
| 적용일 | 2026-09-10 |
| 성격 | 보안만 종합. 객체 연결은 [아키텍처](../technical/01-architecture.md), API 사양은 [기술사양서](../technical/02-technical-specification.md) |
| 상세 | [HMAC](./01-hmac-request-signing.md), [레이트 리밋](./02-rate-limiting.md), [reCAPTCHA v3](./03-recaptcha-v3.md), [허니팟](./04-honeypot-field.md), [일회용 메일](./05-disposable-email.md), [본인인증](./06-identity-verification.md), [이상 탐지](./07-anomaly-guard.md) |

유료 WAF·봇 관리 솔루션은 쓰지 않는다. 비밀값은 `.env`에만 둔다. 값은 이 문서에 적지 않는다.

Hexaq에는 쿠폰·무료 크레딧이 없다. 가입 후 지키는 **실질 자산**은 Solapi SMS, SMTP, DB 쓰기, 게시글·댓글·좋아요다.

---

## 1. 7단계 보안 레이어

가입 전(1~6)에서 가짜 계정·SMS 낭비를 줄이고, 가입 후(7)에서 본인인증을 통과한 매크로의 핵심 API 직행을 줄인다. 정상 사용자에게 캡차 퀴즈를 띄우지 않는다.

```
브라우저
  → RateLimitFilter              2. IP 반복
  → RequestSignatureFilter       1. HMAC (공개 쓰기 7개)
  → Security
  → Honeypot / 일회용 메일 / reCAPTCHA / 인증번호   3~6. 가입·인증 발송
  → AnomalyNavigationInterceptor GET 기록
  → AnomalyGuardFilter           7. 가입 직후 핵심 쓰기
  → Controller → Application → Domain
```

| 단계 | 보안 레이어 | 적용 기술 | 방어 대상 및 역할 | Hexaq 구현 |
|---|---|---|---|---|
| 1 | Protocol / API | HMAC Request Signing | cURL, Postman 등 API 직접 타격 무력화 | 일회용 티켓 + `X-Hexaq-*`. 번들에 비밀키를 굽지 않음 |
| 2 | Network / Traffic | IP Rate Limiting | 동일 IP 기반의 매크로 연타 및 DDoS성 요청 차단 | `RateLimitFilter`. 정적 파일은 제외. 회선 포화 DDoS는 앱 한도 밖 |
| 3 | Behavioral AI | reCAPTCHA v3 | 행위 점수(Score) 기반 자동화 봇 필터링 | `VerifyCaptchaService` → `CaptchaVerifier`. SMS는 점수 통과 뒤에만 |
| 4 | UI / Frontend | Honeypot Field | HTML 파싱 폼 작성 봇을 백엔드 자원 소모 없이 1차 차단 | 가입 `website`. 값이 있으면 거절 |
| 5 | Data Verification | 일회용 이메일 차단 | Temp-Mail을 활용한 가짜 이메일 대량 가입 차단 | DB 전 `EmailPolicy` + `DisposableEmailCatalog` |
| 6 | Identity Verification | 이메일 DI (PASS/NICE 대체) | 1메일 1계정. 같은 이메일 재가입·명의 도용성 다계정 차단 | `Member.email` unique. 소유 확인은 이메일 또는 휴대폰 6자리. 통신사 API 없음. [본인인증](./06-identity-verification.md) |
| 7 | Post-Registration | 가입 직후 API 연타 탐지 | 본인인증을 통과한 지능형 봇의 즉시 쓰기(체리피킹) 차단 | TTFA, 신규 계정 한도, Zero-Nav, `/me` 시퀀스 |

1~6겹은 **가입·인증 전(또는 가입 조건)**. 7겹은 **로그인 후 쓰기**. 비회원 자유게시판 쓰기는 계정이 없어 7겹을 타지 않고, 캡차·HMAC·레이트 리밋으로 본다.

**6단계와 DI.** PASS/NICE 통신사 `DI` 대신 **이메일을 DI로 쓴다.** `email` unique가 DB에서 1메일 1계정을 강제한다. 휴대폰은 unique가 아니고 DI가 아니다. 다른 실메일로는 계정을 더 만들 수 있다. 그 축은 7단계 한도다.

### 1.1 시너지

| 공격 | 주로 걸리는 겹 | 비고 |
|---|---|---|
| 폼 필드만 채워 POST | 허니팟 | 캡차·DB보다 앞 |
| 임시 메일로 가입·인증 메일 연타 | 일회용 메일 | DB·SMTP 전 |
| 같은 IP로 로그인·인증 폭주 | 레이트 리밋 | 분당 버킷 |
| 엔드포인트만 알고 cURL | HMAC | 티켓 없는 호출 400 |
| 헤드리스로 점수 낮게 나옴 | reCAPTCHA | SMS는 점수 통과 뒤에만 |
| 같은 이메일로 재가입 | 이메일 DI | `email` unique |
| 인증 없이 로그인 | 본인인증 | 이메일 또는 휴대폰 인증 완료만 로그인 |
| 사람이 캡차·인증을 통과한 뒤 즉시 글/댓글/좋아요 | 이상 탐지 | TTFA 3초, 시퀀스, 신규 한도 |
| IP를 바꿔 천천히 치는 분산 봇 | 캡차 + HMAC + 본인인증 + 이상 탐지 | 레이트 리밋만으로는 부족 |

### 1.2 인프라·비용

| 구간 | 지키는 자원 |
|---|---|
| 1~5 (가입·SMS 전) | Solapi 건수, SMTP, Redis 인증 코드, 회원 INSERT |
| 6 (이메일 DI) | 같은 메일 재가입 차단. 소유 확인은 6자리. SMS는 1~5를 통과한 뒤에만 발송 |
| 7 (가입 후) | 게시글·댓글·좋아요 DB 쓰기, 애플리케이션 CPU |

허니팟과 일회용 메일은 외부 API를 부르지 않는다. 캡차는 Google `siteverify`가 있다. HMAC·이상 탐지는 Redis(또는 메모리) 조회다.

### 1.3 UX

정상 흐름에서는 퀴즈가 없다.

- reCAPTCHA v3는 점수만 본다
- 허니팟은 화면에 안 보인다
- HMAC은 `client.js`가 붙인다
- 본인인증은 이메일 DI + 이메일 또는 휴대폰 인증번호 1회
- 이상 탐지는 Interceptor/Filter + Redis

사람은 로그인 뒤 `GET /api/members/me`를 탄다. 그 조회가 CONTEXT 단계다.

---

## 2. 종합 보안 수준

유료 봇 관리 제품 없이, 오픈소스 스택(Spring Security, Redis, reCAPTCHA v3, 일회용 메일 목록) **6종**과 가입 전 본인인증을 겹친 구성이다.

이 문서의 **전체 방어율 약 99.9%**는 측정값이 아니다. 단순 봇·스크립트·임시메일·미인증 계정·즉시 직행 매크로를 일곱 겹으로 걸렀을 때의 **운영 판단**이다. 남은 구간은 §3.

| 평가 축 | 내용 |
|---|---|
| 다계정 | 이메일을 DI로 써서 같은 메일 재가입을 DB에서 막는다. 통신사 PASS/NICE는 없다. **다른 실메일**은 다른 DI라서 계정을 더 만들 수 있다 |
| SMS·서버 자원 | 1~5단계에서 봇을 쳐 낸 뒤에만 인증 문자를 보낸다. 건당 통신비 낭비를 줄인다 |
| 가입 후 자산 | 6단계를 통과한 정교한 작업장의 즉시 쓰기는 7단계(TTFA·신규 한도·시퀀스)로 속도를 제한한다. 크레딧은 없다. 대상은 게시글·댓글·좋아요 |
| UX | 캡차 퀴즈 없이, 이메일 DI와 휴대폰 또는 이메일 인증번호 1회 |

IP 레이트 리밋은 **프로세스 메모리**다. 인스턴스가 여러 대면 IP 한도가 서버마다 따로 잡힌다. HMAC 티켓과 이상 탐지 신호는 Redis를 쓰면 인스턴스 사이에 공유된다. 구축 근거와 요건은 [Redis](../technical/04-redis.md).

---

## 3. 잔여 리스크 (남은 약 0.1%)

남는 구간은 **다른 실메일(다른 DI)로 가입한 뒤 사람처럼 기다리는 작업**이다. 매크로가 가입 후 10분~24시간을 기다렸다가 `/me`와 게시판 GET을 치고 천천히 쓰면 TTFA·Zero-Nav·시퀀스를 피할 수 있다. SPA를 그대로 돌려 티켓과 `/me`를 받은 스크립트도 HMAC·시퀀스를 통과한다. 같은 이메일 재가입은 6단계에서 이미 거절된다.

| 잔여 | 이 7겹으로 | 비고 |
|---|---|---|
| 메일함 대여·여러 실메일로 다계정 | 일회용 도메인 차단 + 7단계 연타 탐지 + 신규 계정 분당 5회 | 이메일 DI는 같은 메일만 막는다. 다른 메일은 다른 DI |
| 휴대폰 OTP만 쓰고 번호로 다계정 | 휴대폰은 DI가 아님. 이메일이 달라야 가입됨 | 번호 unique 없음 |
| 가입 후 오래 기다린 뒤 사람처럼 쓰기 | 신규 계정 24시간 분당 5회 | 서비스 전체 타격은 한도로 제한 |
| 회선 포화 DDoS | 앱 한도로는 부족 | CDN/WAF(미구현) |

통신사 PASS/NICE를 붙이는 관문은 두지 않는다. DI는 이메일로 구현한다. 7단계와 계정당 한도로 남은 다계정·슬로 어택의 속도를 제한한다.

---

## 4. 기술별 한 줄과 문서

| 기술 | 한 줄 | 문서 |
|---|---|---|
| HMAC | 일회용 티켓으로 URL-only 호출을 거절 | [HMAC](./01-hmac-request-signing.md) |
| Rate Limit | IP+경로 분당 한도. 인증 10, API 60, 페이지 300. IP는 `ClientAddressPolicy` | [레이트 리밋](./02-rate-limiting.md) |
| reCAPTCHA v3 | 공개 쓰기·인증 발송 점수. SMS는 통과 뒤 | [reCAPTCHA v3](./03-recaptcha-v3.md) |
| Honeypot | 가입 `website`에 값이 있으면 거절 | [허니팟](./04-honeypot-field.md) |
| 일회용 메일 | `check-email`·인증 발송·가입, DB 전 | [일회용 메일](./05-disposable-email.md) |
| 본인인증 | 이메일 DI(unique). 소유 확인은 이메일 또는 휴대폰 6자리 | [본인인증](./06-identity-verification.md) |
| 이상 탐지 | TTFA 3초, 신규 5회/분, GET 없는 쓰기, `/me` 시퀀스 | [이상 탐지](./07-anomaly-guard.md) |

객체와 필터 순서는 [아키텍처](../technical/01-architecture.md) §4.9.
