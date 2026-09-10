# Docker Compose — Hexaq Redis

| 항목 | 내용 |
|---|---|
| 목적 | 인증 코드·HMAC 티켓·이상 탐지용 Redis만 컨테이너로 띄운다. 회원·게시글 DB가 아니다 |
| 적용일 | 2026-09-10 |
| 관련 | [04 Redis](./04-redis.md), [기술사양서](./02-technical-specification.md), [HMAC](../security/01-hmac-request-signing.md), [이상 탐지](../security/07-anomaly-guard.md) |

Spring은 호스트 `8282`에서 실행한다. Compose는 Redis만 정의한다. `.env`: `PHONE_VERIFY_STORE=redis`, `REDIS_HOST=localhost`, `REDIS_PORT=6379`. 비밀값은 적지 않는다.

실행 파일은 저장소 루트의 [docker-compose.redis.yml](../../docker-compose.redis.yml)이다.

```
docker compose -f docker-compose.redis.yml up -d
docker exec hexaq-redis redis-cli ping
```

---

## 1. 객체

| 계층 | 객체 | 책임 |
|---|---|---|
| 컨테이너 | `hexaq-redis` (`redis:7-alpine`) | TTL 키. 클러스터·Sentinel 없음 |
| 네트워크 | `127.0.0.1:6379` | 같은 PC의 Spring만 접속 |
| 앱 설정 | `REDIS_HOST` / `REDIS_PORT` | [04 Redis](./04-redis.md) §4 |
| 스위치 | `PHONE_VERIFY_STORE=redis` | `VerificationStore` · `RequestTicketStore` · `AnomalySignalStore` |

기동 순서: Redis 컨테이너 → (Oracle 또는 MariaDB) → Spring. Redis가 꺼져 있으면 인증 발송·티켓·이상 탐지가 실패한다.

---

## 2. Compose (기본 — AOF 없음)

RDB/AOF는 필수가 아니다. 컨테이너를 지우면 인증번호·티켓·이상 탐지 신호만 사라지고, 회원 데이터는 DB에 남는다.

```yaml
# Hexaq Redis. 회원·게시글은 Oracle(로컬) / MariaDB(prod)에 둔다.
# 이 컨테이너는 PHONE_VERIFY: / REQ_TICKET: / ANOMALY: 만 담는다.
services:
  redis:
    # Redis 6 이상. Alpine이면 이미지가 작다. 클러스터 불필요.
    image: redis:7-alpine
    container_name: hexaq-redis
    # 도커 엔진이 다시 떠도 Redis를 올린다. Spring보다 먼저 살아 있어야 한다.
    restart: unless-stopped
    ports:
      # 127.0.0.1 만. 0.0.0.0:6379 로 바꾸지 않는다.
      - "127.0.0.1:6379:6379"
    command:
      # appendonly no: 재시작 후 코드·티켓 유실 허용. 사용자는 인증번호를 다시 받으면 된다.
      - redis-server
      - --appendonly
      - "no"
```

---

## 3. Compose (선택 — AOF)

키를 컨테이너 재시작 뒤에도 남기려면 볼륨과 AOF를 켠다. 회원 원본 백업이 아니다.

```yaml
# 선택. 인증 코드 TTL(180초)·티켓(60초)은 AOF여도 만료되면 사라진다.
services:
  redis:
    image: redis:7-alpine
    container_name: hexaq-redis
    restart: unless-stopped
    ports:
      - "127.0.0.1:6379:6379"
    volumes:
      # /data 에 AOF. 호스트 경로는 Compose가 관리하는 볼륨.
      - hexaq-redis-data:/data
    command:
      - redis-server
      - --appendonly
      - "yes"

volumes:
  hexaq-redis-data:
```

---

## 4. `docker run` (Compose 없이)

```powershell
# Hexaq Redis. 앱은 호스트 8282, 이 컨테이너만 6379.
# --restart unless-stopped: 도커 재시작 후에도 유지.
# 127.0.0.1: 같은 PC의 Spring만 붙는다.
docker run -d --name hexaq-redis --restart unless-stopped `
  -p 127.0.0.1:6379:6379 `
  redis:7-alpine
```

---

## 5. 하지 않는 것

- Spring·Oracle·MariaDB를 이 Compose에 넣지 않는다
- 6379를 공개 인터페이스에 매핑하지 않는다
- Redis를 회원·게시글·세션·레이트 리밋 버킷 저장소로 쓰지 않는다
