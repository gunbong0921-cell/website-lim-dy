# Hexaq Honeypot Field

| 항목 | 내용 |
|---|---|
| 목적 | HTML을 훑어 모든 입력칸을 채우는 단순 봇의 회원가입을 서버 비용 없이 거절 |
| 적용일 | 2026-09-10 |
| 관련 문서 | [기술사양서](../technical/02-technical-specification.md), [아키텍처](../technical/01-architecture.md), [기능종합](../features/overview.md), [테스트 결과](./test/04-honeypot-field-test-results.md), [reCAPTCHA v3](./03-recaptcha-v3.md), [보안종합](./overview.md) |

사람 눈에는 안 보이는 `website` 칸이다. 값은 이 문서에 비밀을 두지 않는다.

---

## 1. 왜 쓰는가

reCAPTCHA v3와 IP 레이트 리밋은 네트워크·외부 API 비용이 있다. 소스만 파싱하는 봇은 숨은 필드까지 채우는 경우가 많다. 그 값이 있으면 **캡차·DB보다 앞에서** 끊는다.

막는 것: 폼 필드 이름을 모아 JSON/폼으로 보내는 단순 가입 봇.  
막지 못하는 것: 화면을 렌더하고 보이는 칸만 채우는 스텔스 봇. 그 경우는 [reCAPTCHA v3](./03-recaptcha-v3.md)와 [HMAC](./01-hmac-request-signing.md)를 본다.

---

## 2. 적용 범위

회원가입 `POST /api/members/signup`만. 로그인·인증번호·게시판에는 두지 않는다.

실패 메시지: `"요청을 처리할 수 없습니다."`  
허니팟임을 알리지 않는다.

---

## 3. 객체 연결

페이지는 fetch를 하지 않는다. 숨김 UI는 컴포넌트, 판정은 Domain.

```
SignUpPage
  → HoneypotField (website, 화면 밖 숨김)
  → useSignUp.signUp  payload.website
        ↓
MemberController.SignUpRequest.website
        ↓
SignUpCommand.website
        ↓
SignUpService  캡차·DB 전 HoneypotPolicy.tripped
```

| 계층 | 객체 | 책임 |
|---|---|---|
| Domain | `HoneypotPolicy` | `tripped(website)` — null/blank만 통과 |
| Application | `SignUpCommand` / `SignUpService` | 필드 전달. 값이 있으면 `BusinessException` |
| Presentation | `MemberController.SignUpRequest` | `website`만 받아 Command로 넘김. 판정 없음 |
| 컴포넌트 | `HoneypotField` | `name="website"`, `autoComplete="off"`, `tabIndex={-1}`, `aria-hidden` |
| 훅 | `useSignUp` | payload에 `website` 포함(사람은 빈 문자열) |

`HoneypotPolicy`는 `DomainBeanConfig`에 등록한다.

CSS는 `display:none`만 쓰지 않는다. `.hx-hp`로 화면 밖에 두어, 숨김 스타일만 보고 필드를 건너뛰는 봇에도 남긴다.

---

## 4. 설정

없음. 항상 검사한다.
