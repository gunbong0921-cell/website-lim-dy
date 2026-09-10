# Hexaq 기술사양서

| 항목 | 내용 |
|---|---|
| 시스템명 | Hexaq (`website-lim-dy`) |
| 문서 성격 | 현재 구현 기준 기술 사양 |
| 작성일 | 2026-09-08 |
| 갱신일 | 2026-09-10 |
| 관련 문서 | [기술종합](./overview.md), [아키텍처](./01-architecture.md), [반응형](./05-responsive.md), [기능종합](../features/overview.md), [보안종합](../security/overview.md) |

이 문서는 **무엇을 어떤 조건으로 제공하는지**를 적는다. 객체 연결과 규칙 점검은 [아키텍처](./01-architecture.md), 기능별 발표 요약은 [기능종합](../features/overview.md)을 본다.

---

## 1. 시스템 개요

Hexaq는 기업 소개 홈페이지와 회원·게시판을 한 서버에서 제공하는 웹 애플리케이션이다.

- 방문자는 소개 페이지를 로그인 없이 본다.
- 회원은 이메일 또는 휴대폰 인증 후 가입하고, 세션으로 로그인한다.
- Google / Kakao / GitHub 소셜 로그인을 지원한다.
- 자유게시판, QnA, 자료실을 제공한다.
- 사이트 접속은 열어 두고, 비밀값은 `.env`에 두며, API 남용은 레이트 리밋, reCAPTCHA v3, 허니팟, 일회용 메일, HMAC, 가입 전 본인인증, 가입 직후 이상 탐지로 제한한다.

**구성:** React SPA를 Spring Boot가 정적 파일로 함께 서빙한다. 기본 포트는 `8282`이다.

```
브라우저
  → Spring Boot (8282)
       ├ 정적 SPA  (/ , /solutions, /board/…)
       └ REST API  (/api/**)
            → Application → Domain
            ← Infrastructure (Oracle 또는 MariaDB, Redis, SMTP, Solapi, OAuth, 국세청, reCAPTCHA, HMAC)
```

---

## 2. 기술 스택

| 구분 | 기술 | 비고 |
|---|---|---|
| 언어 | Java 21 | Gradle toolchain |
| 백엔드 | Spring Boot 4.0.8 | WAR 패키징 |
| 보안 | Spring Security + OAuth2 Client | 세션 쿠키 |
| 봇 차단 | Google reCAPTCHA v3, 허니팟, 일회용 메일 목록, HMAC 요청 서명 | 공개 표면. 비밀 키는 서버만 |
| 회원 DB | Spring Data JPA | 로컬·tunnel: Oracle(`ojdbc17`, XEPDB1). prod: MariaDB 10.11+ |
| 게시판 DB | MyBatis 4.0.1 | 같은 데이터소스. 검색은 `CONCAT`(양쪽 공통) |
| 인증 코드 | Redis 또는 메모리 | `PHONE_VERIFY_STORE`. 근거·요건은 [Redis](./04-redis.md) |
| 메일 | JavaMail, Naver SMTP | 포트 465 SSL |
| SMS | Solapi SDK 1.1.0 | 발신번호 콘솔 등록 필요 |
| 사업자 확인 | 국세청 오픈데이터 | `NTS_SERVICE_KEY` |
| 프론트 | React 19, React Router 7, Zustand 5 | Vite 8 빌드 |
| 첨부 | 로컬 디스크 `uploads/` | 요청당 최대 50MB |

---

## 3. 아키텍처 원칙

의존성은 **바깥 → 안쪽**만 허용한다. Domain은 Spring Web / MyBatis / SecurityContext를 쓰지 않는다. `@Entity`는 Domain에 허용한다.

| 계층 | 패키지 / 폴더 | 책임 |
|---|---|---|
| Presentation | `com.edu.springboot.presentation` | 요청 검증, 유스케이스 호출, 응답 변환 |
| Application | `com.edu.springboot.application` | 유스케이스, `@Transactional`, DTO |
| Domain | `com.edu.springboot.domain` | 엔티티, VO, Policy, Repository/Gateway 인터페이스 |
| Infrastructure | `com.edu.springboot.infrastructure` | JPA, MyBatis, Redis, Mail, SMS, OAuth, reCAPTCHA, 보안 필터 |
| Pages | `frontend-react/src/pages` | 화면 조립. `fetch` 금지 |
| Hooks | `frontend-react/src/hooks` | 화면 유스케이스 |
| API | `frontend-react/src/services/api` | `fetch`와 `{ success, data, message }` 파싱 |

API JSON 봉투는 다음으로 통일한다. 파일 다운로드만 바이너리다.

```json
{ "success": true, "data": {}, "message": "안내 문구" }
```

실패 시 `success`는 `false`, `data`는 `null`이다.

---

## 4. 기능 사양

### 4.1 소개 페이지

로그인 없이 열람한다.

| 경로 | 화면 | 내용 |
|---|---|---|
| `/` | `HomePage` | 메인 |
| `/solutions` | `SolutionsPage` | 솔루션 소개 |
| `/insights` | `InsightsPage` | 인사이트 |

### 4.2 회원가입

가입 전에 이메일 또는 휴대폰 **하나**로 6자리 인증을 끝낸다. 가입 API는 그 토큰을 한 번 쓰고 버린다.

| 항목 | 사양 |
|---|---|
| 회원 유형 | `INDIVIDUAL`(개인), `CORPORATE`(기업) |
| 아이디 | 이메일 |
| 비밀번호 | 8~20자, 영문·숫자·특수문자 포함, 공백 불가 |
| 인증 코드 | 6자리, TTL 180초, 재전송 60초, 동일 대상·IP 일 10회 |
| 저장 | Redis(기본) 또는 메모리. 이메일 키는 `MAIL:{email}` |
| 로그인 허용 | 이메일 인증 완료 **또는** 휴대폰 인증 완료 |
| 봇 차단 | 인증번호 발송·가입 요청에 reCAPTCHA v3. SMS는 점수 통과 뒤에만 발송. 가입은 허니팟. 이메일은 일회용 도메인 차단. 공개 쓰기는 HMAC |

기업 회원은 국세청에서 사업자번호·상호·대표·개업일을 확인한 뒤에만 가입한다. 개인 회원은 이 단계를 타지 않는다.

기동 시 `EnsureAdminMemberService`가 `.env`의 관리자 계정(`ADMIN_*`)을 보장한다.

### 4.3 로그인 · 소셜 · 계정 찾기

| 기능 | 사양 |
|---|---|
| 로컬 로그인 | 아이디(이메일) + 비밀번호 + reCAPTCHA v3. BCrypt. 세션 쿠키 (`credentials: 'include'`) |
| 로그아웃 | 세션 무효화 |
| 아이디 저장 | 브라우저 쿠키 (`useSavedLoginId`). zustand에는 넣지 않음 |
| 화면 상태 | `authStore`는 `member`, `ready`만 보관 |
| 소셜 | Google, Kakao, GitHub. 프론트는 fetch가 아니라 `/oauth2/authorization/{provider}` 리다이렉트 |
| 카카오 | 친구 목록을 서버가 동기화. 프론트는 `/me` 표시만 |
| GitHub | 공개 이메일이 없으면 GitHub emails API로 보강 |
| 아이디 찾기 | 가입 이메일로 로그인 아이디 발송. reCAPTCHA v3 |
| 비밀번호 찾기 | 임시 비밀번호 메일. reCAPTCHA v3 |

소셜 제공자: `LOCAL`, `GOOGLE`, `GITHUB`, `KAKAO`.

### 4.4 마이페이지

로그인 필수. `PrivatePage`가 세션이 없으면 `/login`으로 보낸다.

- 프로필 조회·수정: `GET /api/members/me`, `PUT /api/members/profile`
- 비밀번호 변경: `PUT /api/members/password` (현재 비밀번호 확인 + `PasswordPolicy`)

### 4.5 게시판

게시판 서비스는 합치지 않는다. `FreeBoardService` / `QnaBoardService` / `ArchiveBoardService` / `CommentService`.

| 게시판 | 쓰기 | 수정·삭제 | 좋아요 | 댓글 |
|---|---|---|---|---|
| 자유 | 비회원 가능, 글 비밀번호, 쓰기에 reCAPTCHA v3 | 글 비밀번호 또는 작성자 | 게스트 허용(쿠키), 회원은 DB 중복 방지 | 없음 |
| QnA | 로그인, 솔루션 영역 필수 | 로그인(작성자) | 로그인 | QnA만 |
| 자료실 | 로그인, 첨부 필수 | 로그인(작성자) | 로그인 | 없음 |

QnA 솔루션 슬러그: `web`, `mobile`, `ai`, `fintech`, `general`. 프론트 `qnaSolutions.js`와 도메인 `QnaSolution`이 같다.

| 부가 규칙 | 사양 |
|---|---|
| 목록 | 기본 페이지 1, 크기 10. 검색 타입·키워드 지원 |
| 조회수 | `BoardCookieService` + `ViewCountPolicy` 쿠키. 하루 1회만 증가 |
| 좋아요 | `LikeService` + `BoardLikeCounter` 맵. 게스트는 `BoardCookieService` |
| 첨부 분류 | `FileTypeClassifier` → `image` / `video` / `audio` / `download` |
| 화면 재생 | `FileViewer` 전략 맵. 타입 추가 시 switch를 늘리지 않음 |
| 업로드 한도 | 파일 50MB, 요청 50MB |
| 저장 위치 | `uploads/` (gitignore) |

---

## 5. 화면 라우트

React Router 기준. Spring `SpaController`가 동일 경로를 `index.html`로 포워드한다.

| 경로 | 인증 | 화면 |
|---|---|---|
| `/` | 공개 | 홈 |
| `/solutions` | 공개 | 솔루션 |
| `/insights` | 공개 | 인사이트 |
| `/login` | 공개 | 로그인 |
| `/signup` | 공개 | 회원가입 |
| `/forgot-id` | 공개 | 아이디 찾기 |
| `/forgot-password` | 공개 | 비밀번호 찾기 |
| `/mypage` | 로그인 | 마이페이지 |
| `/board/free` · `/write` · `/:id` · `/:id/edit` | 공개(쓰기는 비회원 가능) | 자유게시판 |
| `/board/qna/:solution` · 쓰기·상세·수정 | 쓰기는 로그인 | QnA |
| `/board/archive` · `/write` · `/:id` · `/:id/edit` | 쓰기는 로그인 | 자료실 |

`/board/qna`는 `/board/qna/general`로 보낸다. 1180px 이하 내비는 [05 반응형](./05-responsive.md).

---

## 6. API 사양

인증이 필요한 요청은 세션 쿠키를 사용한다. 미인증이면 `401`과 `"로그인이 필요합니다."`를 반환한다.

### 6.1 회원 · 인증

| Method | 경로 | 인증 | 설명 |
|---|---|---|---|
| GET | `/api/public/config` | 공개 | reCAPTCHA 사이트 키·활성 여부 |
| GET | `/api/public/request-ticket` | 공개 | HMAC용 일회용 티켓 (`ticketId`, `signingKey`, `expiresAt`) |
| POST | `/api/auth/login` | 공개 | 로그인 (`recaptchaToken`) |
| POST | `/api/auth/logout` | 공개 | 로그아웃 |
| POST | `/api/auth/forgot-id` | 공개 | 아이디 찾기 메일 (`recaptchaToken`) |
| POST | `/api/auth/forgot-password` | 공개 | 임시 비밀번호 메일 (`recaptchaToken`) |
| GET | `/api/members/check-id` | 공개 | 아이디 중복 |
| GET | `/api/members/check-email` | 공개 | 이메일 중복 |
| POST | `/api/members/business/verify` | 공개 | 국세청 사업자 확인 |
| POST | `/api/members/email/send-code` | 공개 | 이메일 인증번호 (`recaptchaToken`) |
| POST | `/api/members/email/verify` | 공개 | 이메일 코드 확인 |
| POST | `/api/members/phone/send-code` | 공개 | 휴대폰 인증번호 (`recaptchaToken`, SMS 전 점수 확인) |
| POST | `/api/members/phone/verify` | 공개 | 휴대폰 코드 확인 |
| POST | `/api/members/signup` | 공개 | 가입 (`recaptchaToken`) |
| POST | `/api/members/resend-verification` | 공개 | 가입 후 재발송(레거시) |
| POST | `/api/members/verify-email` | 공개 | 가입 후 이메일 확인(레거시) |
| GET | `/api/members/me` | 로그인 | 내 정보 |
| PUT | `/api/members/profile` | 로그인 | 프로필 변경 |
| PUT | `/api/members/password` | 로그인 | 비밀번호 변경 |

소셜 진입: `GET /oauth2/authorization/{google\|kakao\|github}`  
콜백: `/login/oauth2/code/{registrationId}`  
로컬은 `http://localhost:8282/...`. 터널·운영은 `{baseUrl}` (프로필 `tunnel` / `prod`). 콘솔 등록은 [04 소셜](../features/04-social-login.md), [Cloudflare Tunnel](./03-cloudflare-tunnel.md).

### 6.2 게시판 · 댓글 · 파일

`{type}`은 `free` / `qna` / `archive`.

| Method | 경로 | 인증 | 설명 |
|---|---|---|---|
| GET | `/api/boards/{type}` | 공개 | 목록 (`page`, `size`, `searchType`, `keyword`) |
| GET | `/api/boards/{type}/{id}` | 공개 | 상세(조회수 정책 적용) |
| POST | `/api/boards/free` | 공개 | 자유 글쓰기 (`recaptchaToken`) |
| PUT / DELETE | `/api/boards/free/{id}` | 공개(비번 또는 작성자) | 수정·삭제 |
| POST | `/api/boards/free/{id}/like` | 공개 | 좋아요 |
| POST | `/api/boards/qna` | 로그인 | QnA 글쓰기 |
| PUT / DELETE | `/api/boards/qna/{id}` | 로그인 | 수정·삭제 |
| POST | `/api/boards/qna/{id}/like` | 로그인 | 좋아요 |
| POST | `/api/boards/archive` | 로그인 | 자료실(multipart) |
| PUT / DELETE | `/api/boards/archive/{id}` | 로그인 | 수정·삭제 |
| POST | `/api/boards/archive/{id}/like` | 로그인 | 좋아요 |
| GET | `/api/comments/{boardId}` | 공개 | QnA 댓글 목록 |
| POST | `/api/comments/{boardId}` | 로그인 | 댓글 작성 |
| PUT / DELETE | `/api/comments/item/{commentId}` | 로그인 | 댓글 수정·삭제 |
| GET | `/api/files/{id}` | 공개 | 첨부 다운로드(바이너리) |

---

## 7. 보안 사양

홈페이지와 정적 자원은 막지 않는다. 반복 호출과 비밀값 유출을 줄이는 것이 목표다. 공개 쓰기·인증 발송은 reCAPTCHA v3 점수로 한 번 더 거른다. 가입은 허니팟, 이메일은 일회용 도메인, 공개 쓰기는 HMAC 서명을 추가로 본다. 회선 포화형 디도스는 이 한도만으로 막을 수 없고 CDN/WAF가 필요하다.

### 7.1 비밀값

| 규칙 | 내용 |
|---|---|
| 실제 값 | 프로젝트 루트 `.env`. Git 제외 |
| 템플릿 | `.env.example` (키 이름만) |
| 로드 | `spring.config.import=optional:file:.env[.properties]` |
| 참조 | `application.properties`는 `${환경변수}`만. 기본값에 비밀을 넣지 않음 |
| 운영 | `application-prod.properties` + 운영 OAuth 클라이언트, `CORS_ALLOWED_ORIGINS`에 실제 도메인 |

### 7.2 요청 제한 (`RateLimitFilter`)

상세는 [레이트 리밋](../security/02-rate-limiting.md).

IP + 경로 버킷, 기본 창 60초. 정적 파일(`/assets/**`, JS·CSS·이미지 등)은 제외한다.

| 대상 | 기본 한도(IP/분) | 환경 변수 |
|---|---|---|
| SPA 페이지 | 300 | `RATE_LIMIT_PAGE` |
| `/api/**` | 60 | `RATE_LIMIT_API` |
| 로그인·가입·인증번호·OAuth | 10 | `RATE_LIMIT_AUTH` |

초과 시 HTTP `429`, 헤더 `Retry-After`, 본문:

```json
{ "success": false, "data": null, "message": "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요." }
```

트래커 키는 최대 20,000개다. 넘치면 만료 항목을 지우고, 그래도 가득이면 사이트가 멈추지 않도록 해당 요청은 통과시킨다.

### 7.3 CORS · IP · 헤더

| 항목 | 사양 |
|---|---|
| CORS | `CORS_ALLOWED_ORIGINS`만 허용. 자격 증명 허용. HMAC 헤더 `X-Hexaq-Timestamp` / `X-Hexaq-Ticket` / `X-Hexaq-Signature` |
| 클라이언트 IP | 기본 `remoteAddr`. `TRUSTED_PROXY=true`일 때만 `X-Forwarded-For` / `X-Real-IP` |
| 클릭재킹 | `X-Frame-Options: DENY` |
| MIME | `X-Content-Type-Options: nosniff` |
| 오류 본문 | 알 수 없는 예외는 `"서버 오류가 발생했습니다."`만. 스택은 응답에 넣지 않음 |
| CSRF | SPA+세션 구성에서 현재 비활성 |

로컬은 `TRUSTED_PROXY=false`다. 리버스 프록시 뒤에서만 `true`로 둔다.

### 7.4 인가

Spring Security가 URL로 걸러 낸다. 그 외 요청은 공개다.

- 로그인 필수: 내 정보·프로필·비밀번호, QnA/자료실 쓰기·수정·삭제·좋아요, 댓글 쓰기·수정·삭제
- 자유게시판 쓰기·좋아요는 비회원 가능

### 7.5 reCAPTCHA v3

상세는 [reCAPTCHA v3](../security/03-recaptcha-v3.md).

| 항목 | 사양 |
|---|---|
| 검증 | `VerifyCaptchaService` → `CaptchaVerifier` → Google `siteverify` |
| 통과 | `success` + `score >= RECAPTCHA_MIN_SCORE`(기본 0.5) + action 일치 |
| 실패 | 400, `"로봇 확인에 실패했습니다. 다시 시도해 주세요."` |
| 비활성 | 사이트 키 또는 비밀 키가 비면 검증 생략 |
| 공개 설정 | `GET /api/public/config` (`recaptchaEnabled`, `recaptchaSiteKey`). 비밀 키는 내리지 않음 |
| SMS | 점수 통과 뒤에만 `SmsSender.send` |

소셜 로그인 리다이렉트에는 걸지 않는다.

### 7.6 허니팟

상세는 [허니팟](../security/04-honeypot-field.md).

가입 `POST /api/members/signup`의 `website`가 비어 있지 않으면 400, `"요청을 처리할 수 없습니다."` 화면에는 보이지 않는 필드다.

### 7.7 일회용 이메일

상세는 [일회용 메일](../security/05-disposable-email.md).

`check-email`, 이메일 인증번호 발송, 가입에서 DB 조회 전 `EmailPolicy` + `DisposableEmailCatalog`를 적용한다. 소셜 로그인과 아이디/비번 찾기에는 적용하지 않는다.

### 7.8 HMAC 요청 서명

상세는 [HMAC](../security/01-hmac-request-signing.md).

프론트는 `GET /api/public/request-ticket`으로 일회용 키를 받은 뒤 `X-Hexaq-Timestamp` / `X-Hexaq-Ticket` / `X-Hexaq-Signature`를 붙인다. 대상은 reCAPTCHA와 같은 공개 쓰기 7개 POST. 끄려면 `REQUEST_SIGNING_ENABLED=false`.

### 7.9 가입 전 본인인증 (이메일 DI)

상세는 [본인인증](../security/06-identity-verification.md).

가입 행의 `email`이 DI다. unique라서 같은 메일로는 두 번째 가입이 거절된다. 소유 확인은 이메일 또는 휴대폰 6자리 중 하나. 로그인 조건은 `emailVerified` 또는 `phoneVerified`. 통신사 PASS/NICE는 쓰지 않는다. `phone`은 unique가 아니다.

### 7.10 가입 직후 이상 탐지

상세는 [이상 탐지](../security/07-anomaly-guard.md).

로그인 회원의 게시글·댓글·좋아요 쓰기에 TTFA(3초), 신규 계정 분당 5회, GET 없는 직접 타격, `/me` 시퀀스를 적용한다. TTFA 위반 시 `trust_status=SUSPICIOUS`. 시퀀스 위반 시 세션 만료.

---

## 8. 외부 연동

Application은 포트만 본다. 구현은 Infrastructure다.

| 포트 | 현재 구현 | 설정 |
|---|---|---|
| `MemberRepository` | JPA | 로컬 Oracle / prod MariaDB |
| 게시판 Repository | MyBatis | 같은 데이터소스 |
| `VerificationStore` | Redis / 메모리 | `PHONE_VERIFY_STORE` |
| `BoardLikeCounter` | 게시판별 MyBatis 카운터 | — |
| `SmsSender` | Solapi | `SOLAPI_*` |
| `MailSender` | SMTP | `MAIL_*` |
| `FileStorage` | 로컬 디스크 | `app.upload-dir` (기본 `uploads`) |
| `BusinessRegistrationGateway` | 국세청 오픈데이터 | `NTS_SERVICE_KEY` |
| `PasswordEncryptor` | BCrypt | — |
| `CaptchaVerifier` | `GoogleRecaptchaV3Verifier` | `RECAPTCHA_*` |
| `DisposableEmailCatalog` | `ClasspathDisposableEmailCatalog` | `disposable-email-domains.txt` |
| `RequestTicketStore` | Redis / 메모리 | `PHONE_VERIFY_STORE` |
| `AnomalySignalStore` | Redis / 메모리 | `PHONE_VERIFY_STORE` |
| OAuth | Google / GitHub / Kakao | 각 `*_CLIENT_ID` / `SECRET` |

---

## 9. 환경 변수

`.env.example`과 같다. 값은 `.env`에만 둔다.

| 그룹 | 키 |
|---|---|
| DB | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` |
| 메일 | `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` |
| SMS | `SOLAPI_API_KEY`, `SOLAPI_API_SECRET`, `SOLAPI_SENDER` |
| 국세청 | `NTS_SERVICE_KEY` |
| OAuth | `GOOGLE_*`, `GITHUB_*`, `KAKAO_*`, `KAKAO_REDIRECT_URI` |
| 관리자 | `ADMIN_LOGIN_ID`, `ADMIN_PASSWORD`, `ADMIN_NAME`, `ADMIN_EMAIL`, `ADMIN_PHONE` |
| Redis | `REDIS_HOST`, `REDIS_PORT`, `PHONE_VERIFY_STORE` |
| 보안 | `CORS_ALLOWED_ORIGINS`, `TRUSTED_PROXY`, `RATE_LIMIT_*` |
| reCAPTCHA | `RECAPTCHA_ENABLED`, `RECAPTCHA_SITE_KEY`, `RECAPTCHA_SECRET_KEY`, `RECAPTCHA_MIN_SCORE` |
| HMAC | `REQUEST_SIGNING_ENABLED`, `REQUEST_TICKET_TTL_SECONDS`, `REQUEST_SIGNING_MAX_SKEW_SECONDS` |
| 이상 탐지 | `ANOMALY_GUARD_ENABLED`, `ANOMALY_TTFA_MS`, `ANOMALY_NEW_USER_*`, `ANOMALY_NAV_*`, `ANOMALY_DIRECT_HIT_LIMIT`, `ANOMALY_SIGNUP_TTL_HOURS`, `ANOMALY_STEP_TTL_HOURS` |

로컬 CORS 기본값: `http://localhost:8282`, `http://127.0.0.1:8282`, `https://*.trycloudflare.com`.

---

## 10. 빌드 · 실행

| 항목 | 사양 |
|---|---|
| 백엔드 실행 | 프로젝트 루트, 포트 8282 |
| 프론트 개발 | Vite `127.0.0.1:5173`, `/api` → `http://localhost:8282` |
| 운영 정적 파일 | `npm run build` → `src/main/resources/static` |
| Gradle | `processResources`가 `npmBuild`에 의존 |
| 패키징 | WAR (`war` 플러그인) |
| 업로드 | `uploads/`는 저장소에 올리지 않음 |

선행 조건(로컬·tunnel): Oracle(기본 `localhost:1523/XEPDB1`), Redis 6+(기본 `localhost:6379`, `PHONE_VERIFY_STORE=redis`일 때), `.env` 작성. Redis 근거·요건은 [04 Redis](./04-redis.md).

운영(`--spring.profiles.active=prod`): MariaDB 10.11+, `DB_URL=jdbc:mariadb://{host}:3306/{db}`, DDL은 `schema-mariadb.sql`. Linux는 `lower_case_table_names=1`을 권장한다. 기존 Oracle 데이터는 `scripts/migrate-oracle-to-mariadb.ps1`로 이관한다. `uploads/` 파일은 디스크라 스크립트가 복사하지 않는다.

로컬을 잠깐 공개할 때는 [Cloudflare Tunnel](./03-cloudflare-tunnel.md). 프로필 `tunnel` + `scripts/cloudflare-tunnel.ps1`. Vite `same-origin-assets`가 같은 출처 `crossorigin`을 뺀다.

---

## 11. 제약 · 알려진 한계

- CSRF는 꺼져 있다. 같은 출처 SPA+세션 전제다. 공개 쓰기 7개는 HMAC 일회용 티켓으로 스크립트 직접 호출을 줄인다.
- 앱 레이트 리밋은 단일 인스턴스 메모리 기준이다. 다중 인스턴스에서는 IP 한도가 서버마다 따로 잡힌다.
- `AuthController`는 HTTP 세션을 위해 Infrastructure `MemberSessionBinder`를 주입한다. `SocialOAuthSuccessHandler`·`KakaoFriendsClient`·`AdminAccountInitializer`는 Application을 직접 호출한다. 상세는 [아키텍처](./01-architecture.md) 위반 표.
- 인증 저장소 구현 클래스명(`RedisPhoneVerificationStore`)과 `PhoneVerificationPolicy`는 아직 휴대폰 어휘다. 포트·DTO는 `VerificationStore`, `VerifyCodeResult`로 바꿨다.
- 가입 후 `verify-email` / `resend-verification` 경로는 남아 있으나, 현재 가입은 사전 인증 완료가 기본이다.
- Quick Tunnel 호스트는 켤 때마다 바뀐다. 소셜·reCAPTCHA 콘솔은 그 호스트를 다시 넣어야 한다. Google·Kakao는 localhost를 유지한 채 **추가**, GitHub 콜백은 앱당 하나라 **교체**.
- Cloudflare가 CORS 헤더를 빼면 `crossorigin` 스크립트가 흰 화면을 만든다. 빌드 HTML에서 같은 출처 `crossorigin`을 제거한다.

---

## 12. 문서 역할

| 문서 | 역할 |
|---|---|
| [docs/README.md](../README.md) | 문서 목차 |
| [기술종합](./overview.md) | 기술 문서 안내 |
| 이 문서 (`02-technical-specification.md`) | 현재 시스템의 기능·API·보안·환경 사양 |
| [아키텍처](./01-architecture.md) | 계층·객체 연결, 규칙 준수 점검 |
| [기능종합](../features/overview.md) | 기능별 구현 발표 요약 |
| [보안종합](../security/overview.md) | 7겹 방어 종합, 시너지, 잔여 리스크 |
| [HMAC](../security/01-hmac-request-signing.md) | 일회용 티켓 HMAC |
| [레이트 리밋](../security/02-rate-limiting.md) | IP+경로 분당 한도 |
| [reCAPTCHA v3](../security/03-recaptcha-v3.md) | 봇 차단 객체·적용 API·SMS 전 게이트 |
| [허니팟](../security/04-honeypot-field.md) | 가입 허니팟 필드 |
| [일회용 메일](../security/05-disposable-email.md) | 일회용 메일 도메인·형식 검사 |
| [본인인증](../security/06-identity-verification.md) | 이메일 DI. 소유 확인은 이메일 또는 휴대폰 6자리 |
| [이상 탐지](../security/07-anomaly-guard.md) | 가입 직후 규칙 기반 이상 탐지 |
| [Cloudflare Tunnel](./03-cloudflare-tunnel.md) | 로컬 8282 Quick Tunnel 공개 |
| [반응형](./05-responsive.md) | 폰·태블릿 1180px. Header / CompactNav |
| `.env.example` | 환경 변수 키 목록 |
