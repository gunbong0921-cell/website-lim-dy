# Hexaq 일회용 이메일 차단 테스트 결과

| 항목 | 내용 |
|---|---|
| 목적 | 형식·블랙리스트 게이트의 단위 테스트 실행 기록 |
| 실행일 | 2026-09-11 |
| 결과 | 13건 전부 통과. 실패 0 |
| 관련 문서 | [일회용 메일](../05-disposable-email.md), [보안종합](../overview.md), [아키텍처](../../technical/01-architecture.md) |

Spring 컨텍스트·Oracle·GitHub 목록 실호출은 하지 않았다. Domain·Application·Infrastructure·프론트 형식 힌트만 검증한다.

---

## 1. 요약

| 구분 | 건수 | 실패 | 스킵 | 소요 |
|---|---|---|---|---|
| Java (JUnit 5) | 11 | 0 | 0 | 1.552s |
| 프론트 (Node `node:test`) | 2 | 0 | 0 | 0.138s |
| **합계** | **13** | **0** | **0** | — |

성공률 100%. Gradle `BUILD SUCCESSFUL`.

실패 메시지:

- 형식: `"이메일 형식이 올바르지 않습니다."`
- 일회용 도메인: `"일회용 이메일은 사용할 수 없습니다."`

---

## 2. 실행 명령

Java:

```
.\gradlew.bat test --tests "com.edu.springboot.domain.member.EmailPolicyTest" --tests "com.edu.springboot.application.member.RejectDisposableEmailServiceTest" --tests "com.edu.springboot.infrastructure.mail.ClasspathDisposableEmailCatalogTest" --tests "com.edu.springboot.application.member.SignUpServiceDisposableEmailTest" --tests "com.edu.springboot.application.member.SendEmailVerificationServiceDisposableEmailTest" -x npmBuild
```

프론트 (`frontend-react`에서):

```
node --test src/utils/memberForm.email.test.js
```

---

## 3. 클래스별 결과

| 계층 | 테스트 클래스 | 건수 | 결과 |
|---|---|---|---|
| Domain | `EmailPolicyTest` | 3 | 통과 |
| Application | `RejectDisposableEmailServiceTest` | 3 | 통과 |
| Application | `SignUpServiceDisposableEmailTest` | 2 | 통과 |
| Application | `SendEmailVerificationServiceDisposableEmailTest` | 1 | 통과 |
| Infrastructure | `ClasspathDisposableEmailCatalogTest` | 2 | 통과 |
| Utils | `memberForm.email.test.js` | 2 | 통과 |

---

## 4. 케이스

### 4.1 Domain — `EmailPolicy`

일회용 여부는 모른다. 길이 100, `..`, `@.`, `.@`만 본다.

| 케이스 | 결과 |
|---|---|
| 정상적인 이메일은 통과한다 | 통과 |
| 빈 값·길이 초과·연속 점은 거절한다 | 통과 |
| 도메인은 @ 뒤를 소문자로 꺼낸다 | 통과 |

### 4.2 Application — `RejectDisposableEmailService`

소문자 정규화 → 형식 → 카탈로그. DB를 보지 않는다.

| 케이스 | 결과 |
|---|---|
| 허용 메일은 소문자로 정규화해 돌려준다 | 통과 |
| 형식이 틀리면 형식 메시지로 거절한다 | 통과 |
| 일회용 도메인이면 카탈로그 메시지로 거절한다 | 통과 |

### 4.3 Application — `SignUpService` / `SendEmailVerificationService`

`GET /api/members/check-email`, `POST /api/members/email/send-code`는 회원 테이블·SMTP 전에 끊는다.

| 케이스 | 결과 |
|---|---|
| 중복확인은 일회용 메일이면 DB를 보지 않는다 | 통과 |
| 허용 메일은 정규화한 주소로 DB를 본다 | 통과 |
| 일회용 메일이면 회원 조회와 SMTP를 하지 않는다 | 통과 |

### 4.4 Infrastructure — `ClasspathDisposableEmailCatalog`

목록은 `classpath:security/disposable-email-domains.txt`. 런타임에 GitHub를 치지 않는다.

| 케이스 | 결과 |
|---|---|
| 목록에 있는 도메인과 그 하위 도메인을 막는다 | 통과 |
| 일반 메일과 TLD는 막지 않는다 | 통과 |

`mailinator.com`, `foo.mailinator.com`, `10minutemail.com`, `guerrillamail.com`은 막힌다. `gmail.com`, `naver.com`, `com`은 통과한다.

### 4.5 Utils — `isValidEmailFormat`

화면 힌트다. 차단의 정본은 서버다.

| 케이스 | 결과 |
|---|---|
| 정상적인 이메일은 통과한다 | 통과 |
| 빈 값·길이 초과·연속 점은 거절한다 | 통과 |

---

## 5. 이번에 보지 않은 것

| 항목 | 이유 |
|---|---|
| 가입 `signUp` 전체 유스케이스 | 같은 `requireAllowed`를 `validate`에서 호출. 중복확인·발송 게이트로 DB 전을 확인 |
| 소셜 로그인 이메일 | 문서대로 제공자 확인 값이라 이 검사를 타지 않음 |
| 아이디·비밀번호 찾기 | 문서대로 이미 가입된 주소만 조회 |
| 목록에 없는 신규 임시메일 | 문서의 남은 한계 |
| GitHub에서 목록 갱신 | 런타임 fetch 없음. 파일 교체만 |
| `WebsiteLimDyApplicationTests` 컨텍스트 기동 | Oracle·Redis·메일 등 로컬 비밀값이 필요 |

---

## 6. 테스트 파일

| 경로 |
|---|
| `src/test/java/com/edu/springboot/domain/member/EmailPolicyTest.java` |
| `src/test/java/com/edu/springboot/application/member/RejectDisposableEmailServiceTest.java` |
| `src/test/java/com/edu/springboot/application/member/SignUpServiceDisposableEmailTest.java` |
| `src/test/java/com/edu/springboot/application/member/SendEmailVerificationServiceDisposableEmailTest.java` |
| `src/test/java/com/edu/springboot/infrastructure/mail/ClasspathDisposableEmailCatalogTest.java` |
| `frontend-react/src/utils/memberForm.email.test.js` |
