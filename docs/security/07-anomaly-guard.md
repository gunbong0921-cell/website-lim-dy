# Hexaq 규칙 기반 이상 탐지

| 항목 | 내용 |
|---|---|
| 목적 | 가입 직후 스크립트가 핵심 API를 연타하는 계정을 Redis 규칙으로 거른다. ML 없음 |
| 적용일 | 2026-09-10 |
| 관련 문서 | [보안종합](./overview.md), [본인인증](./06-identity-verification.md), [아키텍처](../technical/01-architecture.md), [기술사양서](../technical/02-technical-specification.md) |

7단계 중 **7. Post-Registration**. 쿠폰·크레딧·온보딩 화면은 없다. Hexaq에서 **핵심 자원 API**는 로그인 회원의 게시글·댓글·좋아요 쓰기(POST/PUT/DELETE)다. 비회원 자유게시판 쓰기는 계정이 없어 이 규칙을 타지 않는다.

본인인증(이메일 DI)을 통과한 작업장의 즉시 쓰기를 여기서 속도를 제한한다. 다른 실메일·슬로 어택까지 끝내지 못한다. 종합은 [보안종합](./overview.md) §3.

---

## 1. 네 규칙

| 규칙 | 탐지 | 처리 |
|---|---|---|
| TTFA | 가입 시각과 첫 핵심 API 간격 < 3초 | 429, `Member.trustStatus = SUSPICIOUS` |
| 신규 계정 한도 | 가입 24시간 이내, 같은 계정·API가 1분에 5회 초과 | 429 |
| Zero-Navigation | 최근 5분 GET(`/api/members/me` 또는 `/api/boards/**`) 없이 핵심 쓰기 1회 이상 | 429 |
| 시퀀스 | `GET /api/members/me`(CONTEXT 단계) 없이 핵심 쓰기 | 세션 무효, 401 |

실패 메시지:

- 429: `"요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."`
- 401(시퀀스): `"세션이 만료되었습니다. 다시 로그인해 주세요."`

관리자 계정은 건너뛴다. 이미 `SUSPICIOUS`면 핵심 쓰기를 계속 429로 막는다.

사람은 로그인 뒤 `App`이 `GET /api/members/me`를 먼저 친다. 봇이 POST만 치면 시퀀스·Zero-Nav에 걸린다.

---

## 2. 객체 연결

Infrastructure 필터/인터셉터는 Application을 호출하지 않는다. Domain 정책과 포트만 본다.

```
가입 SignUpService / SocialLoginService
  → RememberSignupService
  → AnomalySignalStore.rememberSignup
        ↓
로그인 후 GET /api/members/me, GET /api/boards/**
  → AnomalyNavigationInterceptor
  → pushGet, markStep(CONTEXT)
        ↓
핵심 POST/PUT/DELETE
  → AnomalyGuardFilter (Security 다음)
  → AnomalyPolicy.decide
  → 거부 또는 Controller
```

| 계층 | 객체 | 책임 |
|---|---|---|
| Domain | `AnomalyPolicy` | 네 규칙 판정. HTTP 모름 |
| Domain | `AnomalyVerdict` / `AnomalyStep` | 결과, CONTEXT 단계 이름 |
| Domain | `AnomalySignalStore` | 가입시각·윈도·GET 히스토리·단계 포트 |
| Domain | `MemberTrustStatus` | `ACTIVE` / `SUSPICIOUS` |
| Application | `RememberSignupService` | 가입 직후 Redis(또는 메모리)에 시각 기록 |
| Infrastructure | `RedisAnomalySignalStore` / `InMemoryAnomalySignalStore` | `PHONE_VERIFY_STORE`와 같은 스위치 |
| Infrastructure | `AnomalyNavigationInterceptor` | 조회 기록. 판정 없음 |
| Infrastructure | `AnomalyGuardFilter` | 핵심 쓰기만 검사. `SecurityContext`는 이 계층만 |
| Infrastructure | `AnomalyPaths` | 어떤 URL이 핵심 쓰기/조회인지 (HTTP) |

`AnomalyPolicy`는 `DomainBeanConfig`에 등록한다.

---

## 3. Redis 키

| 키 | 용도 | TTL |
|---|---|---|
| `ANOMALY:SIGNUP:{loginId}` | 가입 epoch ms | 24시간 |
| `ANOMALY:FIRST:{loginId}` | 첫 핵심 API 여부 (SETNX) | 24시간 |
| `ANOMALY:NEW_USER:{loginId}:{apiName}` | 슬라이딩 윈도 카운트 | 60초 |
| `ANOMALY:NAV:{loginId}` | GET 경로 리스트 | 5분 |
| `ANOMALY:DIRECT:{loginId}` | GET 없이 연속 쓰기 | 5분 |
| `ANOMALY:STEP:{loginId}:CONTEXT` | `/me` 조회 완료 | 24시간 |

---

## 4. 설정

`.env` (값은 Git에 올리지 않음):

| 키 | 기본 | 의미 |
|---|---|---|
| `ANOMALY_GUARD_ENABLED` | `true` | 기능 스위치 |
| `ANOMALY_TTFA_MS` | `3000` | 첫 핵심 API 최소 간격 |
| `ANOMALY_NEW_USER_AGE_HOURS` | `24` | 신규 계정으로 보는 기간 |
| `ANOMALY_NEW_USER_LIMIT` | `5` | 신규 계정 분당 한도 (계정당 사용량 한도) |
| `ANOMALY_NEW_USER_WINDOW_SECONDS` | `60` | 윈도 |
| `ANOMALY_NAV_LOOKBACK_SECONDS` | `300` | GET 기록 유효 시간 |
| `ANOMALY_DIRECT_HIT_LIMIT` | `1` | GET 없이 허용하는 핵심 쓰기 횟수 |
| `ANOMALY_SIGNUP_TTL_HOURS` | `24` | 가입 시각·첫 행동 키 TTL |
| `ANOMALY_STEP_TTL_HOURS` | `24` | CONTEXT 단계 TTL |

남은 한계: SPA를 그대로 돌려 `/me`를 친 뒤 쓰는 봇, 가입 후 3초를 기다리는 봇은 통과할 수 있다. 캡차·HMAC·허니팟·본인인증과 겹쳐 쓴다.
