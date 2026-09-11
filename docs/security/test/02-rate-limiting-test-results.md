# Hexaq IP Rate Limiting 테스트 결과

| 항목 | 내용 |
|---|---|
| 목적 | IP+경로 분당 버킷의 단위 테스트 실행 기록 |
| 실행일 | 2026-09-11 |
| 결과 | 21건 전부 통과. 실패 0 |
| 관련 문서 | [레이트 리밋](../02-rate-limiting.md), [보안종합](../overview.md), [아키텍처](../../technical/01-architecture.md) |

Spring 컨텍스트·Oracle은 올리지 않았다. Domain·Presentation·Infrastructure만 검증한다.

---

## 1. 요약

| 구분 | 건수 | 실패 | 스킵 | 소요 |
|---|---|---|---|---|
| Java (JUnit 5) | 21 | 0 | 0 | 1.383s |
| **합계** | **21** | **0** | **0** | — |

성공률 100%. Gradle `BUILD SUCCESSFUL`.

한도는 테스트에서 인증 2 / API 3 / 페이지 4로 낮춰 연타를 짧게 재현했다. 운영 기본값은 인증 10 / API 60 / 페이지 300이다.

---

## 2. 실행 명령

```
.\gradlew.bat test --tests "com.edu.springboot.domain.security.ClientAddressPolicyTest" --tests "com.edu.springboot.presentation.http.RequestClientIpTest" --tests "com.edu.springboot.infrastructure.security.RateLimitFilterTest" -x npmBuild
```

---

## 3. 클래스별 결과

| 계층 | 테스트 클래스 | 건수 | 결과 |
|---|---|---|---|
| Domain | `ClientAddressPolicyTest` | 7 | 통과 |
| Presentation | `RequestClientIpTest` | 2 | 통과 |
| Infrastructure | `RateLimitFilterTest` | 12 | 통과 |

---

## 4. 케이스

### 4.1 Domain — `ClientAddressPolicy`

| 케이스 | 결과 |
|---|---|
| 프록시를 믿지 않으면 포워드 헤더를 무시한다 | 통과 |
| 프록시를 믿지 않고 remoteAddr가 비면 unknown이다 | 통과 |
| 신뢰 프록시면 X-Forwarded-For 첫 주소를 쓴다 | 통과 |
| 신뢰 프록시이고 Forwarded-For가 없으면 X-Real-IP를 쓴다 | 통과 |
| 신뢰 프록시여도 헤더가 없으면 remoteAddr를 쓴다 | 통과 |
| 신뢰 프록시면 X-Forwarded-Host 첫 값을 쓴다 | 통과 |
| 프록시를 믿지 않으면 서버 이름을 쓴다 | 통과 |

### 4.2 Presentation — `RequestClientIp`

| 케이스 | 결과 |
|---|---|
| 프록시를 믿지 않으면 remoteAddr만 넘긴다 | 통과 |
| 신뢰 프록시면 X-Forwarded-For를 정책에 넘긴다 | 통과 |

### 4.3 Infrastructure — `RateLimitFilter`

실패 응답은 HTTP 429, `Retry-After`, `{"success":false,"data":null,"message":"요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."}`.

| 케이스 | 결과 |
|---|---|
| 인증 버킷은 한도를 넘으면 429이다 | 통과 |
| API 버킷은 인증과 따로 센다 | 통과 |
| 페이지 버킷은 API와 따로 센다 | 통과 |
| IP가 다르면 같은 경로도 따로 센다 | 통과 |
| 초과 응답은 429와 Retry-After와 본문이다 | 통과 |
| 정적 파일은 한도를 보지 않는다 | 통과 |
| index.html과 업로드는 한도를 보지 않는다 | 통과 |
| 스위치가 꺼져 있으면 검증을 건너뛴다 | 통과 |
| 프록시를 믿지 않으면 같은 X-Forwarded-For여도 IP를 나눈다 | 통과 |
| 신뢰 프록시면 X-Forwarded-For 첫 주소로 버킷을 묶는다 | 통과 |
| OAuth와 인증번호 경로는 인증 버킷이다 | 통과 |
| 창이 지나면 다시 허용한다 | 통과 |

로그인·가입은 같은 `auth` 키를 쓴다. `/api/**`는 `api`, 그 외 페이지는 `page`다.

---

## 5. 이번에 보지 않은 것

| 항목 | 이유 |
|---|---|
| `WebsiteLimDyApplicationTests` 컨텍스트 기동 | Oracle·Redis·메일 등 로컬 비밀값이 필요 |
| 트래커 키 20,000개 초과 시 통과 | 프로세스 메모리 한도. 단위 테스트에서 2만 키를 채우지 않음 |
| 여러 인스턴스 공유 | 문서대로 레이트 리밋은 프로세스 메모리라 인스턴스마다 따로 잡힘 |
| 회선 포화 DDoS | 애플리케이션 필터 범위 밖 |

---

## 6. 테스트 파일

| 경로 |
|---|
| `src/test/java/com/edu/springboot/domain/security/ClientAddressPolicyTest.java` |
| `src/test/java/com/edu/springboot/presentation/http/RequestClientIpTest.java` |
| `src/test/java/com/edu/springboot/infrastructure/security/RateLimitFilterTest.java` |
