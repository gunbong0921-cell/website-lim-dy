# Hexaq HMAC Request Signing

| 항목 | 내용 |
|---|---|
| 목적 | API URL만 알고 Postman·cURL·스크립트로 공개 쓰기 엔드포인트를 직접 치는 호출을 거절 |
| 적용일 | 2026-09-10 |
| 관련 문서 | [보안종합](./overview.md), [레이트 리밋](./02-rate-limiting.md), [reCAPTCHA v3](./03-recaptcha-v3.md), [아키텍처](../technical/01-architecture.md), [기술사양서](../technical/02-technical-specification.md) |

프론트 번들에 HMAC 비밀키를 굽지 않는다. 값은 이 문서에 적지 않는다.

---

## 1. 왜 쓰는가

정적 `Client Secret`을 SPA에 넣으면 누구나 JS에서 꺼낸다. 그러면 서명 검증이 성립하지 않는다.

서버가 **일회용 signingKey**(Dynamic CSRF Token)를 발급하고, 프론트는 그 키로 HMAC-SHA256을 계산한다. URL만 아는 호출은 헤더가 없어 거절된다. 같은 서명을 다시 쓰면 티켓이 이미 소비되어 거절된다.

막는 것: 헤더 없이 엔드포인트만 치는 Postman/cURL, 캡처한 요청 재전송.  
막지 못하는 것: SPA를 로드해 티켓을 받은 뒤 서명하는 스크립트, 캡차 대행, 스텔스 브라우저. 그 경우는 캡차·허니팟·일회용 메일을 함께 본다.

multipart 자료실 업로드는 바디 정규화가 달라 이번 범위에서 제외한다.

---

## 2. 적용 범위

reCAPTCHA와 같은 공개 쓰기 JSON 7개. 캡차가 꺼져 있어도 서명 스위치가 켜져 있으면 서명은 필요하다.

| 엔드포인트 |
|---|
| `POST /api/auth/login` |
| `POST /api/members/signup` |
| `POST /api/members/email/send-code` |
| `POST /api/members/phone/send-code` |
| `POST /api/auth/forgot-id` |
| `POST /api/auth/forgot-password` |
| `POST /api/boards/free` |

티켓 발급 `GET /api/public/request-ticket`은 서명하지 않는다.

실패 시 HTTP 400, `"요청이 유효하지 않습니다."`

---

## 3. 프로토콜

1. `GET /api/public/request-ticket` → `{ ticketId, signingKey, expiresAt }`
2. 서버는 Redis(또는 메모리)에 `ticketId → signingKey`를 TTL(기본 60초)로 저장
3. 정규 문자열: `timestamp + "\n" + METHOD + "\n" + path + "\n" + sha256(rawBody)`
4. `signature = HMAC-SHA256(signingKey, canonical)` (hex, 소문자)
5. 헤더: `X-Hexaq-Timestamp`, `X-Hexaq-Ticket`, `X-Hexaq-Signature`
6. 필터가 시간 창(±5분)·HMAC·티켓 **1회 소비**를 확인

`timestamp`는 epoch 밀리초 문자열. `path`는 쿼리 없는 request URI. `rawBody`는 요청 바이트 그대로(프론트는 `JSON.stringify` 결과 문자열).

---

## 4. 객체 연결

Infrastructure 필터는 Application을 호출하지 않는다. Domain 정책과 티켓 포트만 본다. 페이지는 HMAC을 직접 계산하지 않는다.

```
pages / hooks
  → services/api/client.js
      GET /api/public/request-ticket
      HMAC headers
        ↓
RateLimitFilter
        ↓
RequestSignatureFilter
  · RequestSignaturePolicy (시간 창, 상수시간 비교)
  · RequestTicketStore.consume
  · HmacSha256
        ↓
Controller
```

| 계층 | 객체 | 책임 |
|---|---|---|
| Domain | `RequestTicket` | id, signingKey, 만료 |
| Domain | `RequestTicketStore` | `save` / `consume` 포트 |
| Domain | `RequestSignaturePolicy` | 시간 창, 서명 비교. HTTP 모름 |
| Application | `IssueRequestTicketService` | 난수 티켓 발급 |
| Application | `IssuedRequestTicket` | 공개 응답 DTO. 컨트롤러가 Domain을 노출하지 않음 |
| Presentation | `PublicConfigController` | `GET /api/public/request-ticket` |
| Infrastructure | `RedisRequestTicketStore` / `InMemoryRequestTicketStore` | `PHONE_VERIFY_STORE`와 동일 스위치 |
| Infrastructure | `RequestSignatureFilter` | 대상 POST만. `RateLimitFilter` 다음 |
| Infrastructure | `HmacSha256` / `CachedBodyHttpServletRequest` | HMAC·바디 재사용 |
| API | `client.js` | 대상 경로만 서명. 티켓 GET은 `skipSign` |
| Utils | `requestSignature.js` | Web Crypto HMAC-SHA256 |

`RequestSignaturePolicy`는 `DomainBeanConfig`에 등록한다.

---

## 5. 설정

`.env` (값은 Git에 올리지 않음):

| 키 | 기본 | 의미 |
|---|---|---|
| `REQUEST_SIGNING_ENABLED` | `true` | 기능 스위치 |
| `REQUEST_TICKET_TTL_SECONDS` | `60` | 티켓 TTL |
| `REQUEST_SIGNING_MAX_SKEW_SECONDS` | `300` | 타임스탬프 허용 오차 |

`application.properties`:

```
app.request-signing.enabled=${REQUEST_SIGNING_ENABLED:true}
app.request-signing.ticket-ttl-seconds=${REQUEST_TICKET_TTL_SECONDS:60}
app.request-signing.max-skew-seconds=${REQUEST_SIGNING_MAX_SKEW_SECONDS:300}
```

꺼져 있으면 필터가 검증을 건너뛴다(로컬에서 헤더 없이 디버깅할 때). 운영은 켠다.

공개 설정 `GET /api/public/config`에는 서명 키를 넣지 않는다.
