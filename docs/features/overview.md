# Hexaq 기능 종합

클린 아키텍처 · 기능별로 무엇이 어떻게 구현됐는지

| 항목 | 내용 |
|---|---|
| 문서 성격 | 기능 목차와 발표 요약. 상세는 아래 번호 문서 |
| 갱신일 | 2026-09-10 |
| 관련 | [docs/README.md](../README.md), [기술종합](../technical/overview.md), [보안종합](../security/overview.md) |

설정·공격 대비는 기능이 아니라 보안이다. 7겹은 [보안종합](../security/overview.md).

---

## 한 장으로 보는 구조

의존성은 바깥에서 안쪽으로만 흐른다.

```
화면 (React)
  → Presentation (Controller)
    → Application (유스케이스)
      → Domain (규칙 · 인터페이스)
        ← Infrastructure (JPA / MyBatis / Redis / SMS / Mail / reCAPTCHA / HMAC)
```

프론트도 같다. **페이지는 fetch를 하지 않는다.**

```
pages → hooks → services/api → 서버
```

응답은 항상 `{ success, data, message }`

---

## 기능 목록

| 번호 | 문서 | 한 줄 |
|---|---|---|
| 01 | [회원가입 · 본인 인증](./01-signup-verification.md) | 이메일 DI(unique). 가입 전 이메일 또는 휴대폰 6자리 |
| 02 | [기업 회원 · 사업자 확인](./02-business-registration.md) | 국세청 API 확인 후 기업 가입 |
| 03 | [로그인 · 로그아웃](./03-login-logout.md) | 이메일+비밀번호, 세션 쿠키 |
| 04 | [소셜 로그인](./04-social-login.md) | Google / Kakao / GitHub. 공개 URL이면 콘솔 콜백 |
| 05 | [아이디 · 비밀번호 찾기](./05-find-id-password.md) | 가입 이메일로 안내 메일 |
| 06 | [마이페이지](./06-mypage.md) | 프로필·비밀번호. 카카오 친구는 표시만 |
| 07 | [게시판](./07-boards.md) | 자유 / QnA / 자료실, 서비스 분리 |
| 08 | [조회수 · 좋아요 · 댓글](./08-views-likes-comments.md) | `BoardCookieService`, `BoardLikeCounter`, QnA 댓글 |
| 09 | [자료실 첨부](./09-archive-attachments.md) | 파일 타입 전략 맵 |

---

## 교체 가능한 구현 (DIP)

Application은 인터페이스만 본다. 구현은 설정의 문제다.

| 무엇을 | 인터페이스 | 지금 구현 |
|---|---|---|
| 회원 저장 | `MemberRepository` | JPA (로컬 Oracle / prod MariaDB) |
| 게시글 저장 | 게시판 Repository | MyBatis (같은 데이터소스) |
| 인증 코드 | `VerificationStore` | Redis / 메모리 |
| 좋아요 카운트 | `BoardLikeCounter` | 게시판별 MyBatis 카운터 |
| 클라이언트 주소 | `ClientAddressPolicy` | 신뢰 프록시 규칙 |
| 문자 | `SmsSender` | Solapi |
| 메일 | `MailSender` | SMTP |
| 파일 | `FileStorage` | 로컬 디스크 |
| 사업자 | `BusinessRegistrationGateway` | 국세청 |
| 캡차 | `CaptchaVerifier` | Google reCAPTCHA v3 |
| 일회용 메일 | `DisposableEmailCatalog` | 클래스패스 txt |
| HMAC 티켓 | `RequestTicketStore` | Redis / 메모리 |
| 이상 탐지 | `AnomalySignalStore` | Redis / 메모리 |
| 비밀값 | 환경 변수 | `.env` → `application.properties` |

객체 연결은 [아키텍처](../technical/01-architecture.md), API 사양은 [기술사양서](../technical/02-technical-specification.md).

---

## 발표 한 줄 요약

- **화면**은 조립만, **훅**이 유스케이스, **API**만 fetch
- **도메인**에 규칙, **애플리케이션**에 트랜잭션, **인프라**에 Spring·DB·외부 API
- 인증·메일·SMS·파일·사업자 확인·캡차는 **포트에 구현을 꽂는** 방식
- 게시판·댓글은 **역할이 다른 서비스를 억지로 합치지 않음**
- 비밀값은 **`.env`**, 홈페이지는 열어 두고 반복 요청은 [보안종합](../security/overview.md) 7겹
