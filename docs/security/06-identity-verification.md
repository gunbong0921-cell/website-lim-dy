# Hexaq 가입 전 본인인증

| 항목 | 내용 |
|---|---|
| 목적 | 이메일을 DI(중복가입확인정보)로 써서 1메일 1계정을 DB에서 강제한다. 가입 전 소유 확인은 이메일 또는 휴대폰 6자리 |
| 적용일 | 2026-09-10 |
| 관련 문서 | [보안종합](./overview.md), [일회용 메일](./05-disposable-email.md), [reCAPTCHA v3](./03-recaptcha-v3.md), [이상 탐지](./07-anomaly-guard.md), [기능 01](../features/01-signup-verification.md) |

7단계 중 **6. Identity Verification**. PASS/NICE 통신사 API는 쓰지 않는다. `DI`에 해당하는 키는 **이메일**이다. 값은 이 문서에 적지 않는다.

---

## 1. 왜 쓰는가

작업장이 같은 신원으로 계정을 여러 개 만들면 SMS·게시판이 소모된다. 통신사 `DI` 대신 `Member.email` unique가 그 역할을 한다. 같은 이메일로는 두 번째 가입이 거절된다.

1~5단계(HMAC, 레이트 리밋, 캡차, 허니팟, 일회용 메일)를 통과한 요청만 인증 코드를 보낸다. 일회용 메일은 DI 후보를 DB 전에 버린다.

막는 것: 같은 이메일 재가입, 인증 없는 로그인, Temp-mail로 DI를 무한히 만드는 것.  
막지 못하는 것: **다른 실메일**로 만드는 다계정, 메일함 대여. 그 축은 [이상 탐지](./07-anomaly-guard.md)와 신규 계정 한도다. 휴대폰은 DI가 아니다.

---

## 2. 현재 구현

| 구분 | 내용 |
|---|---|
| DI | `email` unique. 통신사 PASS/NICE 없음. 휴대폰 unique 아님 |
| 소유 확인 | 가입 전 이메일 **또는** 휴대폰 6자리 중 하나 |
| 화면 | `SignUpPage` 이메일/휴대폰 탭 |
| API | `POST /api/members/email/send-code`, `phone/send-code`, `/verify`, `/signup` |
| 규칙 | `PhoneVerificationPolicy` (TTL 180초, 재전송 60초, 일 10회). 이메일도 같은 저장소 |
| 포트 | `VerificationStore`. 구현 클래스명은 아직 Redis/InMemory `*PhoneVerificationStore` |
| DTO | `SendVerificationResult`, `VerifyCodeResult` (`verificationToken`, `target`) |
| IP | `RequestClientIp` → `ClientAddressPolicy` |
| 구현 | SMTP · Solapi · Redis(또는 메모리) |
| 로그인 조건 | `emailVerified` 또는 `phoneVerified` |

가입 행에는 이메일이 항상 있다. 휴대폰 탭으로 인증해도 DI는 그 행의 `email`이다. 이미 쓰인 이메일이면 INSERT가 실패한다.

흐름은 [기능 01](../features/01-signup-verification.md). SMS는 reCAPTCHA 점수 통과 뒤에만 발송한다. 이메일은 일회용 도메인을 DB 전에 거절한다.

| DB | 제약 | 역할 |
|---|---|---|
| `email` | unique, not null | DI. 1메일 1계정 |
| `login_id` | unique | 로그인 아이디 |
| `phone` | unique 아님 | 연락처·선택 인증 채널. DI 아님 |

소셜 로그인도 제공자 이메일을 회원 `email`에 넣는다. 같은 메일이면 기존 계정에 붙는다.

---

## 3. PASS/NICE와의 대응

| PASS/NICE | Hexaq (이메일 DI) |
|---|---|
| 통신사 `DI` unique | `email` unique |
| 점유·실명 확인 | 이메일 또는 휴대폰 6자리 소유 확인 |
| 명의 도용 차단 | 같은 메일 재가입 차단. 다른 실메일은 다른 DI |

통신사 CI/DI를 추가로 받을 계획은 이 문서의 현재 구현이 아니다. 6단계는 이메일 DI로 닫는다.

---

## 4. 객체 연결

Application은 포트만 본다. 페이지는 인증 API를 직접 `fetch`하지 않는다.

```
hooks/useEmailVerification 또는 usePhoneVerification
  → send-code (캡차·HMAC·레이트 리밋 뒤)
  → RequestClientIp → ClientAddressPolicy
  → Send*VerificationService
      PhoneVerificationPolicy
      SmsSender / MailSender
      VerificationStore
  → verify → VerifyCodeResult (일회용 토큰)
  → signup 에서 토큰 1회 소비
  → Member.email unique (DI)
```

| 계층 | 객체 | 책임 |
|---|---|---|
| Domain | `Member.email` | DI. unique |
| Domain | `EmailPolicy` / `DisposableEmailCatalog` | 일회용 도메인. DI 후보 사전 거절 |
| Domain | `PhoneVerificationPolicy` | TTL, 재전송, 일일 한도 |
| Domain | `VerificationStore` | 코드·토큰 포트 |
| Domain | `ClientAddressPolicy` | 발송 IP 규칙 |
| Presentation | `RequestClientIp` | HTTP 헤더 → 정책 |
| Application | `RejectDisposableEmailService` | DB 전 일회용 메일 |
| Application | `Send*VerificationService` / `Verify*CodeService` | `SendVerificationResult` / `VerifyCodeResult` |
| Application | `SignUpService` | 토큰 소비, `email` unique INSERT |
| Infrastructure | Solapi / SMTP / Redis 또는 메모리 / JPA | 구현 |

상세 화면·API는 기능 문서, 필터 순서는 [아키텍처](../technical/01-architecture.md) §4.9.
