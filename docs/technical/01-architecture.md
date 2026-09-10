# Hexaq 아키텍처 · 객체 연결 가이드

이 문서는 `.cursor/rules`의 클린 아키텍처, SOLID, 프론트엔드 분리 규칙을 기준으로 **현재 코드가 규칙을 지키는지**, **기능·객체가 어떻게 연결되고 작동하는지**를 정리한다.

- 적용 규칙: `clean-architecture.mdc`, `oop-solid.mdc`, `frontend-architecture.mdc`
- 점검 시점: 2026-09-10 (객체 정리 반영)
- 문서 목차: [docs/README.md](../README.md)
- 기술 종합: [기술종합](./overview.md)
- 기능 종합: [기능종합](../features/overview.md)
- 보안 종합: [보안종합](../security/overview.md)
- HMAC: [01-hmac-request-signing.md](../security/01-hmac-request-signing.md)
- 레이트 리밋: [02-rate-limiting.md](../security/02-rate-limiting.md)
- reCAPTCHA v3: [03-recaptcha-v3.md](../security/03-recaptcha-v3.md)
- 허니팟: [04-honeypot-field.md](../security/04-honeypot-field.md)
- 일회용 메일: [05-disposable-email.md](../security/05-disposable-email.md)
- 본인인증: [06-identity-verification.md](../security/06-identity-verification.md)
- 이상 탐지: [07-anomaly-guard.md](../security/07-anomaly-guard.md)
- 반응형: [05-responsive.md](./05-responsive.md)
- 승인된 객체 정리는 [§8](#8-객체-정리-2026-09-10)과 각 기능·보안 md에 적는다. 남은 위반은 보고만 한다.

---

## 1. 전체 구조

의존성은 **바깥 → 안쪽**만 허용한다.

```
브라우저 (React)
    ↓  HTTP  { success, data, message }
presentation  (Controller, ApiResponse)
    ↓
application   (유스케이스, DTO, @Transactional)
    ↓
domain        (엔티티, Policy, Repository/Gateway 인터페이스)
    ↑
infrastructure (JPA, MyBatis, Redis, Solapi, Mail, Security, NTS, reCAPTCHA)
```

프론트는 같은 흐름을 UI 쪽에서 반복한다.

```
pages (화면 조립)
  → hooks (유스케이스)
    → services/api (fetch·파싱만)
      → backend
store/authStore.js  ← 로그인 사용자만 (zustand)
```

| 계층 | 패키지 / 폴더 | 하는 일 | 하지 않는 일 |
|---|---|---|---|
| Domain | `com.edu.springboot.domain` | 규칙, VO, 포트 인터페이스, `@Entity` | HTTP, MyBatis, SecurityContext |
| Application | `com.edu.springboot.application` | 도메인 조립, 트랜잭션 | Servlet, SecurityContext, `*Impl` 직접 의존 |
| Infrastructure | `com.edu.springboot.infrastructure` | JPA·MyBatis·Redis·SMS·Mail·OAuth 구현 | 비즈니스 규칙 |
| Presentation | `com.edu.springboot.presentation` | 검증, Application 호출, 응답 변환 | 도메인 규칙 직접 작성 |
| Pages | `frontend-react/src/pages` | 훅·컴포넌트 조립 | `fetch`, `*Api` 직접 호출 |
| Hooks | `frontend-react/src/hooks` | 화면 유스케이스 | 화면 마크업 |
| API | `frontend-react/src/services/api` | 호출과 `{ success, data, message }` 파싱 | UI 포맷팅 |

---

## 2. 규칙 준수 요약

전반적으로 **Application → Domain DIP**, **Domain 순수성**, **게시판별 서비스 분리**, **프론트 fetch 격리**는 잘 지켜진다.  
어긋나는 지점은 주로 **Infrastructure가 Application을 호출**하는 인바운드 어댑터(OAuth·관리자 기동)와, **Presentation이 세션 바인더를 주입**하는 부분이다.

### 잘 지키는 점

| 규칙 | 실제 |
|---|---|
| Domain → 바깥 계층 금지 | Domain은 `jakarta.persistence`(허용된 `@Entity`)와 JDK만 사용. Spring Web / MyBatis / Security 없음 |
| Application은 인터페이스만 | 모든 유스케이스가 `MemberRepository`, `SmsSender` 등 포트에 의존. `*Impl` 주입 없음 |
| Application/Domain이 SecurityContext를 안 봄 | `SecurityContextHolder`는 Presentation·Infrastructure에만 있음 |
| 컨트롤러가 Domain 엔티티를 안 내려줌 | JSON은 Application DTO + `ApiResponse` |
| API 봉투 | `{ success, data, message }` (`presentation.dto.ApiResponse`) |
| SRP 게시판 | `FreeBoardService` / `QnaBoardService` / `ArchiveBoardService` / `CommentService` 분리 |
| 첨부 전략 | `FileTypeClassifier` + 프론트 `getFileType` + `FileViewer` 맵 |
| 프론트 API 격리 | pages·components는 `fetch`/`memberApi`/`boardApi`를 직접 호출하지 않음 |
| 캡차 DIP | Application은 `CaptchaVerifier`만. 점수는 `CaptchaPolicy`. 페이지는 `grecaptcha` 미호출 |
| 허니팟 DIP | 판정은 `HoneypotPolicy`. 컨트롤러는 `website` 전달만 |
| 일회용 메일 DIP | Application은 `DisposableEmailCatalog`만. 목록 파일은 Infrastructure |
| HMAC DIP | 필터는 `RequestSignaturePolicy` + `RequestTicketStore`. Application 미호출. 페이지는 HMAC 미계산 |
| 이상 탐지 DIP | 필터는 `AnomalyPolicy` + `AnomalySignalStore`. 인터셉터는 조회 기록만. Application 미호출 |
| 클라이언트 IP | 규칙은 `ClientAddressPolicy`. 컨트롤러는 `RequestClientIp`. 필터는 같은 정책 |
| 조회수·좋아요 쿠키 | 컨트롤러는 `BoardCookieService`만. `ViewCountPolicy`/`LikePolicy`는 Application |
| 좋아요 OCP | `BoardLikeCounter` 맵. 게시판 추가 시 카운터 빈만 추가 |
| 인증 이름 | `VerificationStore`, `SendVerificationResult`, `VerifyCodeResult` |
| zustand 범위 | `authStore`는 `member`, `ready`만 보관 |

### 어긋나는 점 (남은 것)

| 심각도 | 규칙 | 위치 | 내용 |
|---|---|---|---|
| 높음 | Infrastructure는 Domain만 | `SocialOAuthSuccessHandler`, `KakaoFriendsClient`, `AdminAccountInitializer` | Application 서비스·DTO를 직접 호출/사용. OAuth 핸들러는 인바운드 어댑터 |
| 중간 | Presentation은 Application만 | `AuthController` | HTTP 세션을 위해 Infrastructure `MemberSessionBinder`를 주입. `SecurityContextHolder`는 컨트롤러에서 제거됨 |
| 중간 | API 봉투 | `FileController` | 파일 다운로드는 바이너리라 `ApiResponse`를 쓰지 않음 (의도) |
| 낮음 | 프론트 조립만 | `SignUpPage.jsx` | fetch는 없지만 검증·채널 전환 로직이 페이지에 큼 |
| 낮음 | `useViewCount` | 규칙에 명시 | 조회수는 서버 쿠키. 전용 훅 없음 |
| 낮음 | zustand에 아이디 | `useSavedLoginId` | 저장 아이디는 쿠키. 스토어가 아님 |

---

## 3. 계층별 주요 객체

### 3.1 Domain — 규칙과 포트

비즈니스 규칙은 여기에 둔다. 구현체는 Infrastructure가 제공한다.

| 객체 | 책임 |
|---|---|
| `Member` | 회원 상태. 이메일/휴대폰 인증 플래그, `VerificationChannel`, 소셜 제공자 |
| `MemberRepository` | 회원 조회·저장 포트. Application은 Impl이 아니라 이 인터페이스만 봄 |
| `PasswordPolicy` / `PasswordEncryptor` | 비밀번호 규칙 / 해시 포트 |
| `PhoneVerificationPolicy` | 6자리 코드, TTL 180초, 재전송 60초, 일 10회. 이메일·휴대폰 공통 |
| `VerificationStore` | 코드·토큰 저장 포트 (Redis 또는 메모리). 키는 번호 또는 `MAIL:{email}` |
| `ClientAddressPolicy` | 신뢰 프록시 여부에 따른 클라이언트 주소·호스트 규칙. HTTP 모름 |
| `BoardLikeCounter` | 게시판별 좋아요 카운트 증가 포트 |
| `VerificationChannel` | `EMAIL` / `PHONE` |
| `SmsSender` / `MailSender` | 문자·메일 발송 포트 |
| `CaptchaVerifier` / `CaptchaResult` / `CaptchaAction` / `CaptchaPolicy` | reCAPTCHA 검증 포트·점수 규칙. `requiredOnHost`는 `*.trycloudflare.com`만 생략. 검증기는 action switch 없음 |
| `HoneypotPolicy` | 가입 `website`가 비어 있지 않으면 봇으로 본다 |
| `EmailPolicy` / `DisposableEmailCatalog` | 이메일 형식, 일회용 도메인 포트 |
| `RequestTicket` / `RequestTicketStore` / `RequestSignaturePolicy` | 일회용 서명 티켓과 시간 창·상수시간 비교. HTTP 없음 |
| `AnomalyPolicy` / `AnomalySignalStore` / `AnomalyVerdict` | 가입 직후 TTFA·신규 한도·Zero-Nav·시퀀스. HTTP 없음 |
| `MemberTrustStatus` | `ACTIVE` / `SUSPICIOUS` |
| `BusinessRegistrationGateway` | 국세청 사업자 확인 포트 |
| `KakaoTalkGateway` / `KakaoFriendRepository` | 카카오 친구·메시지 |
| `ViewCountPolicy` | 하루 1회 조회수 증가 여부 |
| `LikePolicy` | 게스트 좋아요 허용 여부(자유게시판) |
| `FileTypeClassifier` | 확장자 → `image`/`video`/`audio`/`download` |
| `FileStorage` | 파일 저장 포트 |
| `FreeBoard` / `QnaBoard` / `ArchiveBoard` + 각 Repository | 게시판별 계약. 공통 `write()`를 억지로 맞추지 않음 (LSP) |

### 3.2 Application — 유스케이스

컨트롤러는 여기만 호출한다. `@Transactional`은 이 계층에 둔다.

| 유스케이스 | 입력 | 하는 일 |
|---|---|---|
| `VerifyCaptchaService` | 토큰, `CaptchaAction`, IP, 호스트 | 공개 터널 호스트이거나 비활성이면 통과. 아니면 `CaptchaVerifier` + `CaptchaPolicy` |
| `GetRecaptchaPublicConfigService` | 호스트 | 프론트용 사이트 키·활성 여부. 터널 호스트면 끔. 비밀 키 없음 |
| `RejectDisposableEmailService` | 이메일 | 형식 + 일회용 도메인. DB 전 |
| `IssueRequestTicketService` | — | HMAC용 일회용 `signingKey` 발급 |
| `RememberSignupService` | 로그인 아이디 | 가입 시각을 이상 탐지 스토어에 기록 |
| `SendPhoneVerificationService` | 휴대폰, IP, 캡차 토큰 | **캡차 →** 정책 검사 → 코드 생성 → `SmsSender` → `VerificationStore` |
| `VerifyPhoneCodeService` | 휴대폰, 코드 | 코드 확인 → 일회용 토큰. `VerifyCodeResult` |
| `SendEmailVerificationService` | 이메일, IP, 캡차 토큰 | 캡차 후 동일 정책·스토어, `MailSender`. `SendVerificationResult` |
| `VerifyEmailCodeService` | 이메일, 코드 | 동일. `VerifyCodeResult.target`은 이메일 |
| `BoardCookieService` | 게시판 타입, 글 ID | 조회수·게스트 좋아요 쿠키 이름·만료. 컨트롤러가 Policy를 보지 않음 |
| `VerifyBusinessRegistrationService` | 사업자번호·상호·대표·개업일 | `BusinessRegistrationGateway` 호출 (캡차 없음) |
| `SignUpService` | `SignUpCommand`, 캡차 토큰, IP | 허니팟 → 캡차 → 일회용 메일 → 검증 → 토큰 소비 → `Member` 생성 |
| `LoginService` | 아이디, 비밀번호, 캡차 토큰, IP | 캡차 후 해시 비교. 이메일 또는 휴대폰 인증 완료 계정만 통과 |
| `SocialLoginService` | `SocialProfile` | 기존 연결 또는 신규 가입, 카카오 친구 동기화 |
| `MemberProfileService` | 로그인 아이디 | 내 정보, 프로필·비밀번호 변경 |
| `FindLoginIdService` / `PasswordResetService` | 이메일, 캡차 토큰, IP | 캡차 후 아이디 안내 / 임시 비밀번호 메일 |
| `EnsureAdminMemberService` | 설정값 | 기동 시 관리자 계정 보장 |
| `FreeBoardService` 등 | 게시글 필드 (+ 자유 쓰기는 캡차) | 게시판별 CRUD + 조회수 정책 |
| `CommentService` | QnA 글 ID | 댓글만 담당 |
| `LikeService` | 게시판 타입, 글 ID | `BoardLikeCounter` 맵으로 카운트 증가. 중복 방지는 회원 DB |
| `FileDownloadService` | 파일 ID | 저장소에서 바이트 조회 |

### 3.3 Infrastructure — 교체 가능한 세부사항

| 구현 | 포트 |
|---|---|
| `MemberRepositoryImpl` (JPA) | `MemberRepository` |
| `*BoardRepositoryImpl` (MyBatis) | 게시판 Repository |
| `RedisPhoneVerificationStore` / `InMemoryPhoneVerificationStore` | `VerificationStore` (`app.phone-verify.store`) |
| `SolapiSmsSender` | `SmsSender` |
| `SpringMailSender` | `MailSender` |
| `LocalFileStorage` | `FileStorage` |
| `NtsOpendataBusinessRegistrationGateway` | `BusinessRegistrationGateway` |
| `GoogleRecaptchaV3Verifier` | `CaptchaVerifier` |
| `ClasspathDisposableEmailCatalog` | `DisposableEmailCatalog` |
| `RedisRequestTicketStore` / `InMemoryRequestTicketStore` | `RequestTicketStore` (`app.phone-verify.store`) |
| `RedisAnomalySignalStore` / `InMemoryAnomalySignalStore` | `AnomalySignalStore` (`app.phone-verify.store`) |
| `BcryptPasswordEncryptor` | `PasswordEncryptor` |
| `KakaoTalkMemoClient` | `KakaoTalkGateway` |
| `SecurityConfig` + OAuth 핸들러 | 세션·소셜 로그인, 클릭재킹 방지 헤더 (이 계층에만 둘 것) |
| `WebCorsConfig` | `allowedOriginPatterns`. `CORS_ALLOWED_ORIGINS` + `https://*.trycloudflare.com` |
| `RateLimitFilter` | IP별 반복 요청 제한. 정적 파일은 통과. `ClientAddressPolicy` |
| `RequestSignatureFilter` | 공개 쓰기 7개 POST의 HMAC·일회용 티켓. 레이트 리밋 다음 |
| `AnomalyGuardFilter` | 로그인 회원 핵심 쓰기 4규칙. Security 다음 |
| `AnomalyNavigationInterceptor` | `GET /me`·게시판 조회로 CONTEXT/GET 히스토리 기록 |
| `MemberSessionBinder` / `SessionPrincipal` | 세션 바인딩. Application DTO를 받지 않음 |
| `FreeBoardLikeCounter` 등 | `BoardLikeCounter` |

도메인 정책 빈은 `DomainBeanConfig`에서 생성한다. `ViewCountPolicy`, `LikePolicy`, `PasswordPolicy`, `PhoneVerificationPolicy`, `CaptchaPolicy`, `HoneypotPolicy`, `EmailPolicy`, `RequestSignaturePolicy`, `AnomalyPolicy`, `ClientAddressPolicy`, `FileTypeClassifier`.

### 3.4 Presentation — HTTP 입구

- JSON 성공/실패: `ApiResponse`
- 예외 → 메시지: `GlobalExceptionHandler` (알 수 없는 예외는 상세를 숨김)
- 회원: `MemberController`, `EmailVerificationController`, `PhoneVerificationController`
- 공개 설정: `PublicConfigController` (`GET /api/public/config`, `GET /api/public/request-ticket`)
- 인증: `AuthController` (`RequestClientIp`, `MemberSessionBinder`+`SessionPrincipal`)
- 게시판: `FreeBoardController`, `QnaBoardController`, `ArchiveBoardController` (`BoardCookieService`)
- 댓글/파일: `CommentController`, `FileController`
- SPA: `SpaController` (React 정적 파일)
- HTTP 어댑터: `RequestClientIp` / `RequestHostname` — 헤더만 꺼내 `ClientAddressPolicy`에 넘김. 컨트롤러가 Infrastructure 해석기를 보지 않음

---

## 4. 기능별 연결 · 동작

### 4.1 회원가입과 본인 인증

가입 전에 **선택한 채널에서만** 6자리 인증을 끝낸다. 가입 API는 그 토큰을 한 번 쓰고 버린다.

```
SignUpPage
  ├ SignUpTypeTabs          개인 / 기업
  ├ SignUpVerifyTabs        이메일 인증 / 휴대폰 인증
  ├ useEmailVerification    또는  usePhoneVerification  (발송 전 useRecaptcha)
  └ useSignUp.signUp        (가입 전 useRecaptcha)
        ↓
RateLimitFilter (로그인·인증 한도)
        ↓
RequestSignatureFilter (가입 POST HMAC)
        ↓
EmailVerificationController  |  PhoneVerificationController
        ↓ RequestClientIp → ClientAddressPolicy
Send*VerificationService
  · VerifyCaptchaService.require  (SMS는 이 통과 뒤에만 발송)
  · EMAIL이면 RejectDisposableEmailService (DB 전)
  · Policy + VerificationStore + MailSender/SmsSender
Verify*CodeService       → 일회용 토큰  (캡차 없음)
        ↓
MemberController POST /api/members/signup
        ↓
SignUpService
  · HoneypotPolicy.tripped(website)
  · VerifyCaptchaService.require(SIGNUP)
  · RejectDisposableEmailService.requireAllowed
  · VerificationChannel.from(...)
  · PHONE이면 휴대폰 숫자로 토큰 소비
  · EMAIL이면 MAIL:{email} 키로 토큰 소비
  · Member 저장, markPhoneVerified / markEmailVerified
```

**프론트 토큰 필드**는 채널과 무관하게 `phoneVerificationToken`이다. 백엔드 `SignUpCommand` 필드명과 맞춘 것이다.

기업 회원은 추가로:

```
useSignUp.verifyBusiness
  → POST /api/members/business/verify
  → VerifyBusinessRegistrationService
  → BusinessRegistrationGateway (국세청)
```

가입 후에도 예전 경로 `POST /api/members/verify-email`이 남아 있다. 현재 `SignUpResult.needsEmailVerification()`은 false라 화면의 가입 후 코드 입력 단계는 거의 타지 않는다.

로그인 허용 조건 (`LoginService`): `isEmailVerified() || isPhoneVerified()`.

### 4.2 로그인 · 소셜

```
LoginPage
  ├ useAuth.login → useRecaptcha(login) → POST /api/auth/login → LoginService(캡차) → SessionPrincipal → MemberSessionBinder.bind
  └ SocialLoginButtons → /oauth2/authorization/{google|kakao|github}
                              ↓
                     Spring Security (infrastructure)
                              ↓
                     SocialOAuthSuccessHandler
                       · GitHubUserEmailClient (이메일 보강)
                       · SocialLoginService.loginOrSignUp
                       · KakaoFriendsClient → 친구 목록
                       · SessionPrincipal → MemberSessionBinder.bind
                              ↓
                     SPA 리다이렉트 → App bootstrap → GET /api/members/me
```

세션은 쿠키(`credentials: 'include'`). zustand `authStore`는 서버 세션의 화면 복사본이다.

아이디 저장은 `useSavedLoginId` 쿠키. 로그아웃은 `AuthController` → `MemberSessionBinder.unbind` (세션 무효화 + `SecurityContextHolder` 정리는 바인더 안).

### 4.3 아이디·비밀번호 찾기

```
FindIdPage        → useAuth.findLoginId      → 캡차 forgot_id → FindLoginIdService → MailSender
ForgotPasswordPage → useAuth.forgotPassword → 캡차 forgot_password → PasswordResetService → 임시 비밀번호 메일
```

### 4.4 마이페이지

```
App PrivatePage (member 없으면 /login)
  → MyPage
      useAuth.updateProfile  → PUT /api/members/profile
      useAuth.changePassword → PUT /api/members/password
```

카카오 프로필·친구는 `/me` 응답의 표시 데이터다. 프론트가 카카오 API를 다시 치지 않는다.

### 4.5 게시판 (자유 / QnA / 자료실)

세 게시판은 **서비스를 합치지 않는다**. 공통은 조회수 정책과 응답 DTO 정도다.

```
BoardListPage  → useBoardList    → GET  /api/boards/{type}
BoardViewPage  → useBoardDetail  → GET  /api/boards/{type}/{id}
               → useBoardCommand → DELETE
               → useLike         → POST .../like
BoardWritePage → useBoardCommand → POST/PUT
                 자료실은 FormData + 파일
```

**조회수:** 컨트롤러가 쿠키 존재만 보고 `alreadyViewedToday`를 넘긴다. 쿠키 이름·만료는 `BoardCookieService` → `ViewCountPolicy`. DB 증가는 게시판 서비스 `read(id, alreadyViewedToday)`. 프론트 전용 조회수 훅은 없다.

**좋아요:** `LikeService`가 `BoardLikeCounter` 맵으로 게시판별 카운트를 올린다. 게스트 쿠키도 `BoardCookieService`.

**쓰기 계약이 다른 이유 (LSP):**

| 게시판 | 인증 | write 입력 |
|---|---|---|
| 자유 | 비회원 가능, 글 비밀번호, reCAPTCHA | title, content, writer, password, recaptchaToken |
| QnA | 로그인, 솔루션 영역 | title, content, solution |
| 자료실 | 로그인, 첨부 필수 | title, content, files |

공통 인터페이스 하나에 첨부를 억지로 넣지 않았다.

QnA 경로의 솔루션 슬러그는 `frontend-react/src/board/qnaSolutions.js`와 도메인 `QnaSolution`이 맞춘다.

### 4.6 댓글

QnA만 댓글을 쓴다. 게시판 서비스에 넣지 않고 `CommentService`로 분리했다.

```
BoardViewPage → useComment → /api/comments/{boardId} 및 /api/comments/item/{id}
```

### 4.7 좋아요

```
useLike.like
  → POST /api/boards/{type}/{id}/like
  → LikeService
       LikePolicy.allowsGuest(type)   자유게시판만 게스트 허용
       로그인이면 LikeRepository 중복 검사
       switch로 해당 게시판 likeCount 증가
```

자유게시판 게스트는 컨트롤러가 쿠키로 중복을 막는다. `LikePolicy`를 Presentation이 직접 보는 지점이다.

### 4.8 자료실 첨부 (전략)

백엔드:

```
ArchiveBoardService
  → FileStorage.store(...)
  → FileTypeClassifier.classify(filename)   ExtensionFileTypeClassifier
  → BoardFileRepository.save
```

프론트:

```
getFileType(name, fileType)     확장자 Set으로 분류
FileViewer.strategies[type]     image / video / audio / download 맵
```

타입을 늘릴 때는 Set과 맵에 항목을 추가한다. `FileViewer`에 switch를 늘리지 않는다.

다운로드: `GET /api/files/{id}` → `FileDownloadService` → 바이너리 응답.

### 4.9 설정 · 요청 제한

홈페이지 접속은 열어 둔다. 비밀값은 Git에 올리지 않고, API 남용은 레이트 리밋, reCAPTCHA v3, 허니팟, 일회용 메일, HMAC, 가입 전 본인인증, 가입 직후 이상 탐지로 끊는다.

```
브라우저
  → RateLimitFilter     정적 파일 제외, IP+경로 버킷
  → RequestSignatureFilter  공개 쓰기 7개 POST
  → SecurityConfig      CORS · 보안 헤더
  → AnomalyGuardFilter  로그인 회원 핵심 쓰기
  → Controller
  → Honeypot / RejectDisposableEmail / VerifyCaptchaService / 인증번호
```

| 구분 | 내용 |
|---|---|
| 비밀값 | `.env` (gitignore). 키 이름만 `.env.example` |
| 로드 | `application.properties` → `spring.config.import=optional:file:.env[.properties]` |
| 참조 | `application.properties`는 `${DB_PASSWORD}`처럼 환경 변수만. 기본값에 비밀 없음 |
| CORS | `WebCorsConfig` `allowedOriginPatterns`. 터널 패턴 `https://*.trycloudflare.com` |
| IP | `ClientAddressPolicy` + `RequestClientIp`. 기본은 `remoteAddr`. 리버스 프록시 뒤에서만 `TRUSTED_PROXY=true` |
| 호스트 | `ClientAddressPolicy.resolveHost` + `RequestHostname`. 신뢰 프록시면 `X-Forwarded-Host` |
| 한도 초과 | `429` + `{ success: false, data: null, message: "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요." }` |

한도 (기본, IP당 1분):

| 대상 | 횟수 | `.env` |
|---|---|---|
| SPA 페이지 | 300 | `RATE_LIMIT_PAGE` |
| `/api/**` | 60 | `RATE_LIMIT_API` |
| 로그인·가입·인증번호·OAuth | 10 | `RATE_LIMIT_AUTH` |
| `/assets/**`, JS·CSS·이미지 | 제한 없음 | — |

레이트 리밋: [02-rate-limiting.md](../security/02-rate-limiting.md). reCAPTCHA: 사이트 키는 `GET /api/public/config`로만 프론트에 준다. 비밀 키는 `.env`의 `RECAPTCHA_SECRET_KEY`. 상세는 [reCAPTCHA v3](../security/03-recaptcha-v3.md).  
허니팟: [허니팟](../security/04-honeypot-field.md). 일회용 메일: [일회용 메일](../security/05-disposable-email.md). HMAC: [HMAC](../security/01-hmac-request-signing.md). 본인인증: [본인인증](../security/06-identity-verification.md). 가입 직후 이상 탐지: [이상 탐지](../security/07-anomaly-guard.md).

운영은 `application-prod.properties`에 실제 도메인 CORS를 넣고, 프록시 뒤에서만 `TRUSTED_PROXY=true`로 둔다. 로컬 공개는 프로필 `tunnel` + [Cloudflare Tunnel](./03-cloudflare-tunnel.md). 회선 포화형 디도스는 앱 한도로 막을 수 없고 CDN/WAF가 필요하다.

---

## 5. 프론트 객체 연결

```
App.jsx
  useAuth.bootstrap → memberApi.me → authStore.setMember

pages/          라우트 화면. 훅만 호출
components/     UI만. Header(데스크톱), CompactNav(폰·태블릿), FileViewer …
hooks/          useAuth, useCompactNav, useSignUp, usePhoneVerification, useEmailVerification,
                useRecaptcha, useBoardList, useBoardDetail, useBoardCommand, useLike, useComment
services/api/   client.js 가 유일한 fetch. 공개 쓰기 7개는 HMAC 헤더
                memberApi / boardApi / commentApi / publicApi
store/          authStore.js
utils/          getFileType, memberForm, passwordPolicy, requestSignature
```

`Layout`이 `useCompactNav`로 Header를 빼고 CompactNav를 둔다. 상세는 [05 반응형](./05-responsive.md).

`useBoardCommand`는 게시판 타입별 API를 맵으로 고른다 (`writeByType`, `updateByType`). 페이지가 `boardApi`를 import하지 않는다.

소셜 버튼은 fetch가 아니라 `<a href="/oauth2/authorization/...">` 리다이렉트다.

---

## 6. 요청이 한 바퀴 도는 예

휴대폰 인증 후 가입:

1. 사용자가 **휴대폰 인증** 탭을 고른다. `SignUpVerifyTabs` → `form.verificationChannel = 'PHONE'`
2. `usePhoneVerification.send` → `useRecaptcha.execute('phone_send_code')` → `POST /api/members/phone/send-code`
3. `RateLimitFilter`가 IP 한도를 본다. `RequestSignatureFilter`가 HMAC·티켓을 본다. 통과하면 `PhoneVerificationController` → `RequestClientIp` → `SendPhoneVerificationService`
4. `VerifyCaptchaService`가 점수를 본다. 미달이면 SMS를 보내지 않는다
5. `PhoneVerificationPolicy`가 번호·쿨다운·일일 한도를 검사하고 6자리 코드를 만든다
6. `SolapiSmsSender`가 문자를 보낸다. `RedisPhoneVerificationStore`가 TTL과 함께 저장한다
7. 사용자가 코드를 입력. `verify` → `VerifyPhoneCodeService` → 일회용 토큰
8. 가입 제출. `useSignUp.signUp` → 캡차 `signup` → HMAC 후 `POST /api/members/signup` (`phoneVerificationToken`, 빈 `website`)
9. `SignUpService`가 허니팟·캡차·일회용 메일 검사 후 토큰을 소비하고 `Member.markPhoneVerified()` 후 저장한다
10. 로그인 화면으로 이동. `LoginService`는 캡차 후 휴대폰 인증 완료 계정을 통과시킨다

이메일을 고르면 5~7단계만 `MailSender`와 `MAIL:{email}` 키로 바뀐다. 저장소·정책 객체는 같다. 캡차는 같다.

---

## 7. 빈 연결 (DIP)

Spring이 인터페이스에 구현을 꽂는다. Application 코드에 `new RedisPhoneVerificationStore`가 없다.

| 포트 | 구현 | 선택 |
|---|---|---|
| `VerificationStore` | Redis 또는 InMemory (`RedisPhoneVerificationStore` / `InMemoryPhoneVerificationStore`) | `app.phone-verify.store` |
| `BoardLikeCounter` | `FreeBoardLikeCounter` / `QnaBoardLikeCounter` / `ArchiveBoardLikeCounter` | 게시판 빈 추가 |
| `SmsSender` | `SolapiSmsSender` | Solapi 키 |
| `MailSender` | `SpringMailSender` | SMTP |
| `CaptchaVerifier` | `GoogleRecaptchaV3Verifier` | `RECAPTCHA_*`. 키 없으면 검증 생략 |
| `DisposableEmailCatalog` | `ClasspathDisposableEmailCatalog` | `disposable-email-domains.txt` |
| `RequestTicketStore` | Redis 또는 InMemory | `app.phone-verify.store` |
| `AnomalySignalStore` | Redis 또는 InMemory | `app.phone-verify.store` |
| `MemberRepository` | JPA Impl | — |
| 게시판 Repository | MyBatis Impl | — |
| `FileStorage` | `LocalFileStorage` | 로컬 디스크 |

회원은 JPA, 게시판은 MyBatis다. Domain은 둘 다 모른다. 데이터소스는 프로필이다. 기본·`tunnel`은 Oracle, `prod`는 MariaDB.

DB·메일·OAuth·SMS 키는 `.env`에서 읽고, Application/Domain은 파일 경로를 모른다.

---

## 8. 객체 정리 (2026-09-10)

`.cursor` 클린 아키텍처·SOLID에 맞춰 아래를 반영했다. 기능별 상세는 각 md.

| 변경 | 계층 | 객체 | 이유 |
|---|---|---|---|
| 추가 | Domain | `ClientAddressPolicy` | IP 규칙은 HTTP를 모른다 |
| 추가 | Presentation | `RequestClientIp` | 헤더 추출만. `ClientIpResolver` 삭제 |
| 추가 | Infrastructure | `SessionPrincipal` | 세션 바인더가 Application `MemberResponse`를 받지 않음 |
| 추가 | Application | `BoardCookieService`, `CookieInstruction` | 컨트롤러가 `ViewCountPolicy`/`LikePolicy`를 직접 쓰지 않음 |
| 추가 | Domain | `BoardLikeCounter` | 좋아요 게시판 switch 제거 (OCP) |
| 추가 | Infrastructure | `FreeBoardLikeCounter`, `QnaBoardLikeCounter`, `ArchiveBoardLikeCounter` | 게시판별 카운트 증가 |
| 이름 | Domain | `VerificationStore` (`PhoneVerificationStore` 삭제) | 이메일·휴대폰 공통 포트 |
| 이름 | Application DTO | `SendVerificationResult`, `VerifyCodeResult` | 채널 중립. `target` 필드 |
| 추가 | 설정 | `application-tunnel.properties`, `scripts/cloudflare-tunnel.ps1` | 로컬 HTTPS 공개. 포워드 헤더·OAuth `{baseUrl}` |
| 변경 | Infrastructure | `WebCorsConfig` | `allowedOriginPatterns` + Security `CorsConfigurationSource` |
| 프론트 | Vite | `same-origin-assets` | 같은 출처 JS·CSS `crossorigin` 제거. 터널 흰 화면 방지 |
| 추가 | 프론트 | `useCompactNav`, `CompactNav`, `hexaq-compact.css` | 1180px 이하 햄버거. Header 로고는 컴팩트 DOM에 없음 |

남은 손볼 순서:

1. `SocialOAuthSuccessHandler`, `KakaoFriendsClient`, `AdminAccountInitializer`가 Application을 직접 호출하지 않게 포트를 둔다.
2. `AuthController`의 `MemberSessionBinder` 주입을 Presentation 포트 뒤로 옮긴다.
3. Redis/메모리 구현 클래스명 `*PhoneVerificationStore`, 정책명 `PhoneVerificationPolicy`를 채널 중립으로 맞출지 결정한다.
4. `SignUpPage` 로직을 훅으로 더 내린다. 규칙의 `useViewCount`는 조회수가 서버 쿠키라 전용 훅이 없다.
