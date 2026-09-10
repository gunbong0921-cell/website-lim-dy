# 04. Redis (일시 저장소)

| 항목 | 내용 |
|---|---|
| 목적 | 인증 코드·HMAC 티켓·이상 탐지 신호를 TTL로 둔다. 회원·게시글의 원본이 아니다 |
| 적용일 | 2026-09-10 |
| 관련 | [기술종합](./overview.md), [기술사양서](./02-technical-specification.md), [아키텍처](./01-architecture.md), [본인인증](../security/06-identity-verification.md), [HMAC](../security/01-hmac-request-signing.md), [이상 탐지](../security/07-anomaly-guard.md) |

연결은 `.env`의 `REDIS_HOST` / `REDIS_PORT`다. 비밀값은 이 문서에 적지 않는다.

---

## 1. 구축 근거

Oracle/MariaDB는 회원·게시글을 남긴다. Redis는 **짧게 살고 만료·1회 소비가 필요한 값**만 둔다.

| 이유 | 설명 |
|---|---|
| TTL | 인증번호 180초, 재전송 쿨다운 60초, HMAC 티켓 60초. DB 폴링·스케줄 삭제 없이 키가 사라진다 |
| 1회 소비 | 가입 토큰·HMAC 티켓은 `GETDEL`에 해당한다. 재사용하면 SMS·공개 API가 뚫린다 |
| 카운터 | 일 10회 발송, 신규 계정 분당 한도, SETNX(첫 핵심 API)는 `INCR` / `SET NX` + `EXPIRE` |
| 최근 조회 | Zero-Nav는 최근 GET 리스트다. Redis LIST + 짧은 TTL |
| 인스턴스 공유 | 앱을 여러 대 띄우면 HMAC 티켓과 이상 탐지 신호가 서버마다 갈라지면 안 된다. IP 레이트 리밋은 프로세스 메모리라 Redis와 무관하다 |
| DB와 분리 | 인증 코드·티켓을 회원 테이블에 넣지 않는다. 만료 행이 늘지 않고, 본문 DB 장애와 수명이 다르다 |

메모리는 로컬 대체다. 프로세스 재시작이면 코드·티켓·신호가 전부 사라진다. 운영과 터널에서 `PHONE_VERIFY_STORE=redis`가 기본인 이유다.

쓰지 않는 것: 세션(HttpSession), 회원·게시글 원본, 첨부 파일, 레이트 리밋 버킷.

---

## 2. 무엇을 담는가

Application은 포트만 본다. `PHONE_VERIFY_STORE`가 `redis`이면 세 구현이 같은 Redis에 붙는다.

| 포트 | 구현 | 키(접두) | TTL(기본) |
|---|---|---|---|
| `VerificationStore` | `RedisPhoneVerificationStore` | `PHONE_VERIFY:`, `PHONE_VERIFY_COOLDOWN:`, `PHONE_TOKEN:`, `PHONE_VERIFY_DAILY:*` | 코드 180초, 쿨다운 60초, 가입 토큰 30분, 일한도는 그날 자정(Asia/Seoul) |
| `RequestTicketStore` | `RedisRequestTicketStore` | `REQ_TICKET:` | `REQUEST_TICKET_TTL_SECONDS` (60초) |
| `AnomalySignalStore` | `RedisAnomalySignalStore` | `ANOMALY:*` | 가입·단계 24시간, GET 히스토리 5분, 신규 한도 창 60초 |

이상 탐지 키 상세는 [07 이상 탐지](../security/07-anomaly-guard.md) §3.

이메일 인증 코드의 저장 키는 번호가 아니라 `MAIL:{email}`이다. 접두 `PHONE_VERIFY:`는 구현 클래스명과 같이 남아 있다.

---

## 3. 구축 요건

| 구분 | 요건 |
|---|---|
| 제품 | Redis 6 이상 (Spring Data Redis / Lettuce). 클러스터·Sentinel은 이 앱에 필요 없다 |
| 네트워크 | 앱이 `REDIS_HOST:REDIS_PORT`에 TCP로 붙는다. 기본 `localhost:6379` |
| 인증 | 현재 설정에 Redis 비밀번호는 없다. 운영에서 `requirepass`를 켜면 `spring.data.redis.password`를 `.env`로만 추가한다 |
| 지속성 | RDB/AOF는 필수가 아니다. Redis가 비면 사용자는 인증번호를 다시 받고, HMAC 티켓을 다시 발급받으면 된다. 회원 데이터는 DB에 있다 |
| 메모리 | 짧은 TTL 문자열·작은 LIST. 전용 대용량 인스턴스는 필요 없다 |
| 스위치 | `PHONE_VERIFY_STORE=redis`이면 Redis가 **기동 중 도달 가능**해야 한다. 로컬만 쓸 때는 `memory` |
| 운영 | 프로필 `prod`도 같은 호스트/포트 변수를 쓴다. DB(MariaDB)와 Redis는 별 프로세스다 |
| Linux 방화벽 | 6379를 앱 서버(또는 동일 호스트)만 열 것. 공개 인터넷에 Redis를 두지 않는다 |

로컬 확인: Redis가 6379에서 listen하고 `.env`에 `PHONE_VERIFY_STORE=redis`, `REDIS_HOST=localhost`, `REDIS_PORT=6379`.

기동 순서: Redis → (DB) → Spring. Redis가 꺼진 채로 `PHONE_VERIFY_STORE=redis`이면 인증 발송·티켓·이상 탐지가 실패한다.

---

## 4. 설정

`application.properties`:

```
app.phone-verify.store=${PHONE_VERIFY_STORE:redis}
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.redis.repositories.enabled=false
```

`repositories.enabled=false`는 Redis를 Spring Data 엔티티 저장소로 쓰지 않기 때문이다. 접근은 `StringRedisTemplate`뿐이다.

| `.env` 키 | 기본 | 의미 |
|---|---|---|
| `PHONE_VERIFY_STORE` | `redis` | `redis` 또는 `memory`. 세 포트가 같이 갈린다 |
| `REDIS_HOST` | `localhost` | Redis 호스트 |
| `REDIS_PORT` | `6379` | Redis 포트 |

빈은 `PhoneVerificationStoreConfig`, `RequestTicketStoreConfig`, `AnomalySignalStoreConfig`가 `@ConditionalOnProperty`로 고른다. Domain/Application에 Redis 타입이 없다.

---

## 5. Docker

앱(Spring 8282)은 호스트에서 돌리고 Redis만 컨테이너로 띄운다. `.env`는 `REDIS_HOST=localhost`, `REDIS_PORT=6379`, `PHONE_VERIFY_STORE=redis`. 주석이 붙은 Compose 전문은 [docker-compose.redis.md](./docker-compose.redis.md)에 있다. 포트는 `127.0.0.1`만 연다.

기동: `docker compose -f docker-compose.redis.yml up -d` 또는 아래 `docker run`. 확인: `docker exec hexaq-redis redis-cli ping` → `PONG`.

```powershell
# Hexaq Redis. 앱은 호스트 8282, 이 컨테이너만 6379.
# -d: 백그라운드. --restart: 도커 재시작 후에도 유지.
# 127.0.0.1: 같은 PC의 Spring만 붙는다. 공개 인터넷에 열지 않는다.
docker run -d --name hexaq-redis --restart unless-stopped `
  -p 127.0.0.1:6379:6379 `
  redis:7-alpine
```
