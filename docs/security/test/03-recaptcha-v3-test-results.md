# Hexaq reCAPTCHA v3 테스트 결과

| 항목 | 내용 |
|---|---|
| 목적 | 점수·action 게이트와 공개 설정의 단위 테스트 실행 기록 |
| 실행일 | 2026-09-11 |
| 결과 | 19건 전부 통과. 실패 0 |
| 관련 문서 | [reCAPTCHA v3](../03-recaptcha-v3.md), [보안종합](../overview.md), [아키텍처](../../technical/01-architecture.md) |

Spring 컨텍스트·Oracle·Google `siteverify` 실호출은 하지 않았다. Domain·Application·Infrastructure만 검증한다. 검증기 테스트는 로컬 HTTP로 JSON만 받는다.

---

## 1. 요약

| 구분 | 건수 | 실패 | 스킵 | 소요 |
|---|---|---|---|---|
| Java (JUnit 5) | 19 | 0 | 0 | 1.949s |
| **합계** | **19** | **0** | **0** | — |

성공률 100%. Gradle `BUILD SUCCESSFUL`.

컷오프는 문서 기본값 0.5를 쓴다. 0.3 이하는 봇으로 보고 거절된다.

---

## 2. 실행 명령

```
.\gradlew.bat test --tests "com.edu.springboot.domain.captcha.CaptchaPolicyTest" --tests "com.edu.springboot.domain.captcha.CaptchaActionTest" --tests "com.edu.springboot.application.captcha.VerifyCaptchaServiceTest" --tests "com.edu.springboot.application.captcha.GetRecaptchaPublicConfigServiceTest" --tests "com.edu.springboot.infrastructure.captcha.GoogleRecaptchaV3VerifierTest" -x npmBuild
```

---

## 3. 클래스별 결과

| 계층 | 테스트 클래스 | 건수 | 결과 |
|---|---|---|---|
| Domain | `CaptchaPolicyTest` | 3 | 통과 |
| Domain | `CaptchaActionTest` | 2 | 통과 |
| Application | `VerifyCaptchaServiceTest` | 7 | 통과 |
| Application | `GetRecaptchaPublicConfigServiceTest` | 3 | 통과 |
| Infrastructure | `GoogleRecaptchaV3VerifierTest` | 4 | 통과 |

---

## 4. 케이스

### 4.1 Domain — `CaptchaPolicy`

| 케이스 | 결과 |
|---|---|
| 성공이고 action이 같고 점수가 컷오프 이상이면 통과한다 | 통과 |
| 실패·null·action 불일치면 거절한다 | 통과 |
| localhost는 캡차가 필요하고 터널 호스트는 아니다 | 통과 |

### 4.2 Domain — `CaptchaAction` / `CaptchaResult`

| 케이스 | 결과 |
|---|---|
| 문서의 7개 action과 같다 | 통과 |
| 네트워크 실패 결과는 success=false다 | 통과 |

7개: `login`, `signup`, `email_send_code`, `phone_send_code`, `forgot_id`, `forgot_password`, `board_write_free`.

### 4.3 Application — `VerifyCaptchaService`

실패 메시지: `"로봇 확인에 실패했습니다. 다시 시도해 주세요."`

| 케이스 | 결과 |
|---|---|
| 사이트 키와 비밀 키가 있을 때만 활성이다 | 통과 |
| 터널 호스트면 검증기를 부르지 않는다 | 통과 |
| 비활성이면 검증기를 부르지 않는다 | 통과 |
| 토큰이 비면 실패 메시지로 거절한다 | 통과 |
| 점수가 컷오프 미만이면 거절한다 | 통과 |
| action이 다르면 거절한다 | 통과 |
| 점수와 action이 맞으면 통과한다 | 통과 |

### 4.4 Application — `GetRecaptchaPublicConfigService`

| 케이스 | 결과 |
|---|---|
| 활성이고 localhost면 사이트 키만 내린다 | 통과 |
| 터널 호스트면 캡차를 끈다 | 통과 |
| 키가 비면 캡차를 끈다 | 통과 |

공개 설정에 비밀 키는 넣지 않는다.

### 4.5 Infrastructure — `GoogleRecaptchaV3Verifier`

점수 컷오프는 검증기가 하지 않는다. `CaptchaPolicy`가 판정한다.

| 케이스 | 결과 |
|---|---|
| 비밀 키나 토큰이 비면 실패 결과다 | 통과 |
| 네트워크 실패는 success=false다 | 통과 |
| Google JSON의 success·score·action만 옮긴다 | 통과 |
| success가 false여도 점수는 판정하지 않고 그대로 둔다 | 통과 |

---

## 5. 이번에 보지 않은 것

| 항목 | 이유 |
|---|---|
| Google `siteverify` 실호출 | 비밀 키·네트워크. 로컬 HTTP로 JSON만 받음 |
| `useRecaptcha` 훅 e2e | 브라우저 `grecaptcha.execute`. 프론트 테스트 러너 없음 |
| SMS 발송 전 게이트 통합 | `SendPhoneVerificationService`는 캡차 실패 시 `SmsSender`를 부르지 않음. 유스케이스 전체는 이번 범위 밖 |
| `WebsiteLimDyApplicationTests` 컨텍스트 기동 | Oracle·Redis·메일 등 로컬 비밀값이 필요 |
| 스텔스 봇·캡차 대행 | 문서의 남은 한계. 허니팟·HMAC·이상 탐지와 겹침 |

---

## 6. 테스트 파일

| 경로 |
|---|
| `src/test/java/com/edu/springboot/domain/captcha/CaptchaPolicyTest.java` |
| `src/test/java/com/edu/springboot/domain/captcha/CaptchaActionTest.java` |
| `src/test/java/com/edu/springboot/application/captcha/VerifyCaptchaServiceTest.java` |
| `src/test/java/com/edu/springboot/application/captcha/GetRecaptchaPublicConfigServiceTest.java` |
| `src/test/java/com/edu/springboot/infrastructure/captcha/GoogleRecaptchaV3VerifierTest.java` |
