# Hexaq HMAC Request Signing 테스트 결과

| 항목 | 내용 |
|---|---|
| 목적 | 일회용 티켓 HMAC 서명의 단위 테스트 실행 기록 |
| 실행일 | 2026-09-11 |
| 결과 | 31건 전부 통과. 실패 0 |
| 관련 문서 | [HMAC](../01-hmac-request-signing.md), [보안종합](../overview.md), [아키텍처](../../technical/01-architecture.md) |

Spring 컨텍스트·Oracle·실 Redis는 올리지 않았다. Domain·Application·Infrastructure·프론트 유틸만 검증한다.

---

## 1. 요약

| 구분 | 건수 | 실패 | 스킵 | 소요 |
|---|---|---|---|---|
| Java (JUnit 5) | 28 | 0 | 0 | 3.769s |
| 프론트 (Node `node:test`) | 3 | 0 | 0 | 0.140s |
| **합계** | **31** | **0** | **0** | — |

성공률 100%. Gradle `BUILD SUCCESSFUL`.

---

## 2. 실행 명령

Java (프론트 `npmBuild`는 제외):

```
.\gradlew.bat test --tests "com.edu.springboot.domain.security.RequestSignaturePolicyTest" --tests "com.edu.springboot.infrastructure.security.HmacSha256Test" --tests "com.edu.springboot.infrastructure.security.RequestSignatureFilterTest" --tests "com.edu.springboot.infrastructure.persistence.memory.InMemoryRequestTicketStoreTest" --tests "com.edu.springboot.infrastructure.persistence.redis.RedisRequestTicketStoreTest" --tests "com.edu.springboot.application.security.IssueRequestTicketServiceTest" -x npmBuild
```

프론트:

```
node --test src/utils/requestSignature.test.js
```

`frontend-react` 디렉터리에서 실행.

---

## 3. 클래스별 결과

| 계층 | 테스트 클래스 | 건수 | 결과 |
|---|---|---|---|
| Domain | `RequestSignaturePolicyTest` | 5 | 통과 |
| Application | `IssueRequestTicketServiceTest` | 2 | 통과 |
| Infrastructure | `HmacSha256Test` | 3 | 통과 |
| Infrastructure | `InMemoryRequestTicketStoreTest` | 4 | 통과 |
| Infrastructure | `RedisRequestTicketStoreTest` | 3 | 통과 |
| Infrastructure | `RequestSignatureFilterTest` | 11 | 통과 |
| Utils | `requestSignature.test.js` | 3 | 통과 |

---

## 4. 케이스

### 4.1 Domain — `RequestSignaturePolicy`

| 케이스 | 결과 |
|---|---|
| 타임스탬프가 허용 오차 안이면 통과한다 | 통과 |
| 타임스탬프가 허용 오차를 넘으면 거절한다 | 통과 |
| 허용 오차가 음수면 거절한다 | 통과 |
| 서명은 대소문자를 무시하고 같으면 통과한다 | 통과 |
| 서명이 null이거나 길이가 다르면 거절한다 | 통과 |

### 4.2 Application — `IssueRequestTicketService`

| 케이스 | 결과 |
|---|---|
| 티켓 id·키·만료를 만들고 스토어에 저장한다 | 통과 |
| TTL이 1초 미만이면 1초로 올린다 | 통과 |

### 4.3 Infrastructure — `HmacSha256`

| 케이스 | 결과 |
|---|---|
| HMAC-SHA256 hex는 소문자이고 공개 벡터와 같다 | 통과 |
| 빈 바디 SHA-256은 공개 벡터와 같다 | 통과 |
| abc의 SHA-256은 공개 벡터와 같다 | 통과 |

서버·프론트가 같은 공개 벡터를 쓴다.

- HMAC-SHA256(`key`, `The quick brown fox jumps over the lazy dog`) = `f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8`
- SHA-256(빈 바이트) = `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855`

### 4.4 Infrastructure — `InMemoryRequestTicketStore`

| 케이스 | 결과 |
|---|---|
| 저장한 키를 한 번만 꺼낸다 | 통과 |
| 소비 시 티켓 공백을 제거한다 | 통과 |
| 없거나 공백인 티켓은 비어 있다 | 통과 |
| 이미 만료된 티켓은 소비하지 않는다 | 통과 |

### 4.5 Infrastructure — `RedisRequestTicketStore`

Redis는 모의 객체다. 실제 인스턴스는 쓰지 않았다.

| 케이스 | 결과 |
|---|---|
| 저장은 REQ_TICKET 접두와 TTL을 쓴다 | 통과 |
| 소비는 GETDEL에 해당한다 | 통과 |
| 공백 티켓은 Redis를 부르지 않는다 | 통과 |

### 4.6 Infrastructure — `RequestSignatureFilter`

실패 응답은 HTTP 400, `{"success":false,"data":null,"message":"요청이 유효하지 않습니다."}`.

| 케이스 | 결과 |
|---|---|
| 올바른 서명이면 필터를 통과하고 바디를 재사용할 수 있다 | 통과 |
| 헤더가 없으면 400이다 | 통과 |
| 타임스탬프가 숫자가 아니면 400이다 | 통과 |
| 시간 창을 벗어나면 티켓을 소비하지 않고 400이다 | 통과 |
| 없는 티켓이면 400이다 | 통과 |
| 서명이 틀리면 400이고 티켓은 이미 소비된다 | 통과 |
| 같은 티켓을 다시 쓰면 400이다 | 통과 |
| 서명 대상이 아닌 경로는 헤더 없이 통과한다 | 통과 |
| 티켓 발급 GET은 서명하지 않는다 | 통과 |
| 스위치가 꺼져 있으면 검증을 건너뛴다 | 통과 |
| 끝 슬래시가 있어도 로그인 POST는 검증한다 | 통과 |

### 4.7 Utils — `requestSignature.js`

| 케이스 | 결과 |
|---|---|
| 빈 문자열 SHA-256은 공개 벡터와 같다 | 통과 |
| HMAC-SHA256 hex는 소문자이고 공개 벡터와 같다 | 통과 |
| 정규 문자열은 timestamp + METHOD + path + bodyHash 이다 | 통과 |

---

## 5. 이번에 보지 않은 것

| 항목 | 이유 |
|---|---|
| `WebsiteLimDyApplicationTests` 컨텍스트 기동 | Oracle·Redis·메일 등 로컬 비밀값이 필요 |
| 실 Redis `GETDEL` | 단위 테스트는 `StringRedisTemplate` 모의 |
| 브라우저에서 7개 POST e2e | `client.js` 티켓 발급·헤더 부착은 유틸 벡터만 맞춤 |
| multipart 자료실 업로드 | [HMAC](../01-hmac-request-signing.md) 범위 밖 |

---

## 6. 테스트 파일

| 경로 |
|---|
| `src/test/java/com/edu/springboot/domain/security/RequestSignaturePolicyTest.java` |
| `src/test/java/com/edu/springboot/application/security/IssueRequestTicketServiceTest.java` |
| `src/test/java/com/edu/springboot/infrastructure/security/HmacSha256Test.java` |
| `src/test/java/com/edu/springboot/infrastructure/security/RequestSignatureFilterTest.java` |
| `src/test/java/com/edu/springboot/infrastructure/persistence/memory/InMemoryRequestTicketStoreTest.java` |
| `src/test/java/com/edu/springboot/infrastructure/persistence/redis/RedisRequestTicketStoreTest.java` |
| `frontend-react/src/utils/requestSignature.test.js` |
