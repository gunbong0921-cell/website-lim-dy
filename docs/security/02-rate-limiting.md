# Hexaq IP Rate Limiting

| 항목 | 내용 |
|---|---|
| 목적 | 같은 IP의 무차별 연타와 인증 API 폭주를 분당 버킷으로 끊는다 |
| 적용일 | 2026-09-10 |
| 관련 문서 | [보안종합](./overview.md), [HMAC](./01-hmac-request-signing.md), [reCAPTCHA v3](./03-recaptcha-v3.md), [아키텍처](../technical/01-architecture.md), [기술사양서](../technical/02-technical-specification.md) |

유료 WAF는 쓰지 않는다. 이 한도는 애플리케이션 필터다. 회선 포화형 디도스는 막지 못한다.

---

## 1. 왜 쓰는가

로그인·가입·인증번호는 Solapi·SMTP·세션을 소모한다. 같은 IP가 짧은 시간에 같은 경로를 반복하면 분당 한도로 거절한다.

막는 것: 단일 IP의 무차별 연타, 인증 API 폭주.  
막지 못하는 것: IP를 바꿔 치는 분산 봇, 회선 포화 DDoS. 그 경우는 캡차·HMAC·이상 탐지와 CDN/WAF를 본다.

필터 순서는 [아키텍처](../technical/01-architecture.md) §4.9. HMAC 필터보다 **앞**이다.

```
브라우저
  → RateLimitFilter
  → RequestSignatureFilter
  → Security
```

---

## 2. 한도

IP + 경로 버킷, 기본 창 60초. 정적 파일(`/assets/**`, JS·CSS·이미지 등)은 제외한다.

| 대상 | 기본 한도(IP/분) | 환경 변수 |
|---|---|---|
| SPA 페이지 | 300 | `RATE_LIMIT_PAGE` |
| `/api/**` | 60 | `RATE_LIMIT_API` |
| 로그인·가입·인증번호·OAuth | 10 | `RATE_LIMIT_AUTH` |
| `/assets/**`, JS·CSS·이미지 | 제한 없음 | — |

초과 시 HTTP `429`, 헤더 `Retry-After`, 본문:

```json
{ "success": false, "data": null, "message": "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요." }
```

트래커 키는 최대 20,000개다. 넘치면 만료 항목을 지우고, 그래도 가득이면 사이트가 멈추지 않도록 해당 요청은 통과시킨다.

IP 레이트 리밋은 **프로세스 메모리**다. 인스턴스가 여러 대면 IP 한도가 서버마다 따로 잡힌다. HMAC 티켓과 이상 탐지 신호는 Redis를 쓰면 인스턴스 사이에 공유된다. Redis 근거·요건은 [04 Redis](../technical/04-redis.md).

---

## 3. 객체

| 계층 | 객체 | 책임 |
|---|---|---|
| Domain | `ClientAddressPolicy` | 신뢰 프록시 여부에 따른 주소 규칙. HTTP 모름 |
| Presentation | `RequestClientIp` | `X-Forwarded-For` / `X-Real-IP` 헤더를 정책에 전달 |
| Infrastructure | `RateLimitFilter` | 버킷 판정. 정적 파일 제외. 같은 `ClientAddressPolicy` |

Domain/Application은 이 필터를 보지 않는다. 페이지는 한도를 계산하지 않는다. 컨트롤러는 Infrastructure IP 해석기를 주입하지 않는다.

로컬은 `TRUSTED_PROXY=false`다. 리버스 프록시·Cloudflare Tunnel 뒤에서만 `true`(프로필 `tunnel` 기본).

---

## 4. 설정

`.env` (값은 Git에 올리지 않음):

| 키 | 기본 | 의미 |
|---|---|---|
| `RATE_LIMIT_PAGE` | `300` | SPA 페이지 분당 |
| `RATE_LIMIT_API` | `60` | `/api/**` 분당 |
| `RATE_LIMIT_AUTH` | `10` | 로그인·가입·인증·OAuth 분당 |
| `TRUSTED_PROXY` | `false` | `true`일 때만 포워드 헤더로 IP |

키 이름은 `.env.example`을 본다.
