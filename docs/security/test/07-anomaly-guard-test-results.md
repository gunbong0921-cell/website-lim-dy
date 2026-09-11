# Hexaq 규칙 기반 이상 탐지 테스트 결과

| 항목 | 내용 |
|---|---|
| 목적 | TTFA·신규 한도·Zero-Nav·시퀀스 단위 테스트 실행 기록 |
| 실행일 | 2026-09-11 |
| 결과 | 35건 전부 통과. 실패 0 |
| 관련 문서 | [이상 탐지](../07-anomaly-guard.md), [보안종합](../overview.md), [본인인증](../06-identity-verification.md) |

Spring 컨텍스트·Oracle·실 Redis는 올리지 않았다. Domain·Application·Infrastructure만 검증한다. 프론트 전용 유틸은 없다.

---

## 1. 요약

| 구분 | 건수 | 실패 | 스킵 | 소요 |
|---|---|---|---|---|
| Java (JUnit 5) | 35 | 0 | 0 | 1.880s |
| **합계** | **35** | **0** | **0** | — |

성공률 100%. Gradle `BUILD SUCCESSFUL`.

실패 메시지:

- 429: `"요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."` + `Retry-After: 60`
- 401(시퀀스): `"세션이 만료되었습니다. 다시 로그인해 주세요."`

비회원 자유게시판 쓰기는 계정이 없어 이 규칙을 타지 않는다.

---

## 2. 실행 명령

```
.\gradlew.bat test --tests "com.edu.springboot.domain.anomaly.AnomalyPolicyTest" --tests "com.edu.springboot.infrastructure.security.AnomalyPathsTest" --tests "com.edu.springboot.infrastructure.persistence.memory.InMemoryAnomalySignalStoreTest" --tests "com.edu.springboot.infrastructure.persistence.redis.RedisAnomalySignalStoreTest" --tests "com.edu.springboot.application.anomaly.RememberSignupServiceTest" --tests "com.edu.springboot.infrastructure.security.AnomalyNavigationInterceptorTest" --tests "com.edu.springboot.infrastructure.security.AnomalyGuardFilterTest" -x npmBuild
```

---

## 3. 클래스별 결과

| 계층 | 테스트 클래스 | 건수 | 결과 |
|---|---|---|---|
| Domain | `AnomalyPolicyTest` | 7 | 통과 |
| Infrastructure | `AnomalyPathsTest` | 4 | 통과 |
| Infrastructure | `InMemoryAnomalySignalStoreTest` | 6 | 통과 |
| Infrastructure | `RedisAnomalySignalStoreTest` | 4 | 통과 |
| Application | `RememberSignupServiceTest` | 2 | 통과 |
| Infrastructure | `AnomalyNavigationInterceptorTest` | 3 | 통과 |
| Infrastructure | `AnomalyGuardFilterTest` | 9 | 통과 |

---

## 4. 케이스

### 4.1 Domain — `AnomalyPolicy`

HTTP·DB를 모른다. 네 규칙과 관리자 통과만 판정한다.

| 케이스 | 결과 |
|---|---|
| 관리자는 항상 통과한다 | 통과 |
| 이미 SUSPICIOUS면 계속 거절한다 | 통과 |
| CONTEXT 단계 없이 쓰면 시퀀스다 | 통과 |
| 최근 GET 없이 핵심 쓰기를 하면 Zero-Nav다 | 통과 |
| 가입 후 3초 안에 첫 핵심 API면 TTFA다 | 통과 |
| 신규 계정이 분당 한도를 넘으면 거절한다 | 통과 |
| 가입 24시간 이내만 신규 계정이다 | 통과 |

### 4.2 Infrastructure — `AnomalyPaths`

| 케이스 | 결과 |
|---|---|
| 로그인 회원 게시글·댓글·좋아요 쓰기는 핵심 API다 | 통과 |
| 조회와 가입 API는 핵심 쓰기가 아니다 | 통과 |
| GET `/me`와 게시판 GET은 CONTEXT 조회다 | 통과 |
| `apiName`은 동사와 자원을 붙인다 | 통과 |

### 4.3 Infrastructure — `AnomalySignalStore`

메모리는 실제 `InMemoryAnomalySignalStore`. Redis는 `StringRedisTemplate` 모의.

| 케이스 | 결과 |
|---|---|
| 가입 시각을 기록하고 읽는다 | 통과 |
| 첫 핵심 API는 한 번만 차지한다 | 통과 |
| GET을 남기면 최근 조회가 있다 | 통과 |
| CONTEXT 단계를 표시한다 | 통과 |
| GET 없이 쓴 횟수를 세고 지울 수 있다 | 통과 |
| 같은 계정·API 윈도 횟수를 올린다 | 통과 |
| 가입 시각은 `ANOMALY:SIGNUP` 키에 둔다 | 통과 |
| 첫 핵심 API는 SETNX다 | 통과 |
| CONTEXT 단계는 `ANOMALY:STEP` 키다 | 통과 |
| 신규 계정 한도는 `ANOMALY:NEW_USER` 키다 | 통과 |

### 4.4 Application — `RememberSignupService`

| 케이스 | 결과 |
|---|---|
| loginId를 정규화해 24시간 TTL로 저장한다 | 통과 |
| 빈 loginId는 저장하지 않는다 | 통과 |

### 4.5 Infrastructure — `AnomalyNavigationInterceptor`

판정은 하지 않는다. GET 기록과 CONTEXT만 남긴다.

| 케이스 | 결과 |
|---|---|
| 로그인 후 GET `/me`는 CONTEXT와 조회 기록을 남긴다 | 통과 |
| 게시판 GET은 조회만 남기고 CONTEXT는 아니다 | 통과 |
| 비로그인이면 기록을 남기지 않는다 | 통과 |

### 4.6 Infrastructure — `AnomalyGuardFilter`

`SecurityContext`는 이 계층만 본다.

| 케이스 | 결과 |
|---|---|
| `/me` 없이 핵심 쓰면 401이고 세션을 지운다 | 통과 |
| 가입 직후 첫 핵심 쓰기는 429이고 SUSPICIOUS로 표시한다 | 통과 |
| 이미 SUSPICIOUS면 핵심 쓰기를 429로 막는다 | 통과 |
| 관리자는 핵심 쓰기를 건너뛴다 | 통과 |
| 비회원 쓰기는 이상 탐지를 타지 않는다 | 통과 |
| 스위치가 꺼져 있으면 검증을 건너뛴다 | 통과 |
| 조회 요청은 핵심 쓰기가 아니라서 건너뛴다 | 통과 |
| CONTEXT는 있으나 최근 GET이 없으면 Zero-Nav 429다 | 통과 |
| CONTEXT와 최근 GET이 있고 가입이 오래되면 통과한다 | 통과 |

---

## 5. 이번에 보지 않은 것

| 항목 | 이유 |
|---|---|
| 실 Redis | `StringRedisTemplate` 모의. 메모리는 실제 `InMemoryAnomalySignalStore` |
| SPA를 그대로 돌려 `/me`를 친 뒤 쓰는 봇 | 문서의 남은 한계. 캡차·HMAC·허니팟·본인인증과 겹침 |
| 가입 후 3초를 기다리는 봇 | 문서의 남은 한계 |
| 비회원 자유게시판 쓰기 | 계정이 없어 이 규칙을 타지 않음. 캡차·HMAC·레이트 리밋 |
| `WebsiteLimDyApplicationTests` 컨텍스트 기동 | Oracle·Redis·메일 등 로컬 비밀값이 필요 |

---

## 6. 테스트 파일

| 경로 |
|---|
| `src/test/java/com/edu/springboot/domain/anomaly/AnomalyPolicyTest.java` |
| `src/test/java/com/edu/springboot/infrastructure/security/AnomalyPathsTest.java` |
| `src/test/java/com/edu/springboot/infrastructure/persistence/memory/InMemoryAnomalySignalStoreTest.java` |
| `src/test/java/com/edu/springboot/infrastructure/persistence/redis/RedisAnomalySignalStoreTest.java` |
| `src/test/java/com/edu/springboot/application/anomaly/RememberSignupServiceTest.java` |
| `src/test/java/com/edu/springboot/infrastructure/security/AnomalyNavigationInterceptorTest.java` |
| `src/test/java/com/edu/springboot/infrastructure/security/AnomalyGuardFilterTest.java` |
