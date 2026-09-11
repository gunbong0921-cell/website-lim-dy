# Hexaq Honeypot Field 테스트 결과

| 항목 | 내용 |
|---|---|
| 목적 | 가입 `website` 허니팟의 단위 테스트 실행 기록 |
| 실행일 | 2026-09-11 |
| 결과 | 7건 전부 통과. 실패 0 |
| 관련 문서 | [허니팟](../04-honeypot-field.md), [보안종합](../overview.md), [아키텍처](../../technical/01-architecture.md) |

Spring 컨텍스트·Oracle은 올리지 않았다. Domain·Application·프론트 필드 계약만 검증한다.

---

## 1. 요약

| 구분 | 건수 | 실패 | 스킵 | 소요 |
|---|---|---|---|---|
| Java (JUnit 5) | 4 | 0 | 0 | 1.463s |
| 프론트 (Node `node:test`) | 3 | 0 | 0 | 0.098s |
| **합계** | **7** | **0** | **0** | — |

성공률 100%. Gradle `BUILD SUCCESSFUL`.

실패 메시지는 `"요청을 처리할 수 없습니다."`다. 허니팟임을 알리지 않는다.

---

## 2. 실행 명령

Java:

```
.\gradlew.bat test --tests "com.edu.springboot.domain.member.HoneypotPolicyTest" --tests "com.edu.springboot.application.member.SignUpServiceHoneypotTest" -x npmBuild
```

프론트 (`frontend-react`에서):

```
node --test src/components/member/HoneypotField.source.test.js
```

---

## 3. 클래스별 결과

| 계층 | 테스트 클래스 | 건수 | 결과 |
|---|---|---|---|
| Domain | `HoneypotPolicyTest` | 2 | 통과 |
| Application | `SignUpServiceHoneypotTest` | 2 | 통과 |
| Components | `HoneypotField.source.test.js` | 3 | 통과 |

---

## 4. 케이스

### 4.1 Domain — `HoneypotPolicy`

| 케이스 | 결과 |
|---|---|
| null과 공백은 통과한다 | 통과 |
| 값이 있으면 봇으로 본다 | 통과 |

### 4.2 Application — `SignUpService`

캡차·일회용 메일·DB보다 **앞**에서 끊는다.

| 케이스 | 결과 |
|---|---|
| website에 값이 있으면 캡차와 DB 없이 거절한다 | 통과 |
| website가 비면 허니팟을 건너뛰고 다음 검사를 한다 | 통과 |

빈 값은 허니팟을 건너뛴 뒤 `"필수 항목을 모두 입력하세요."`로 이어진다. 가입 본문은 이번 범위가 아니다.

### 4.3 Components — `HoneypotField`

| 케이스 | 결과 |
|---|---|
| name=website 이고 탭·자동완성을 끈다 | 통과 |
| CSS는 display:none만 쓰지 않고 화면 밖에 둔다 | 통과 |
| 가입 훅은 website가 없으면 빈 문자열을 보낸다 | 통과 |

`.hx-hp`는 `left: -10000px`다. `display:none`만 쓰지 않는다.

---

## 5. 이번에 보지 않은 것

| 항목 | 이유 |
|---|---|
| 브라우저에서 가입 폼 e2e | 컴포넌트 소스·CSS 계약만 봄. 렌더 러너 없음 |
| 로그인·인증번호·게시판 | 문서대로 허니팟은 `POST /api/members/signup`만 |
| 스텔스 봇(보이는 칸만 채움) | 문서의 남은 한계. reCAPTCHA·HMAC과 겹침 |
| `WebsiteLimDyApplicationTests` 컨텍스트 기동 | Oracle·Redis·메일 등 로컬 비밀값이 필요 |

---

## 6. 테스트 파일

| 경로 |
|---|
| `src/test/java/com/edu/springboot/domain/member/HoneypotPolicyTest.java` |
| `src/test/java/com/edu/springboot/application/member/SignUpServiceHoneypotTest.java` |
| `frontend-react/src/components/member/HoneypotField.source.test.js` |
