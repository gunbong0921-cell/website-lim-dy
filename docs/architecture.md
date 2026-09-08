# Hexaq 아키텍처 · 객체 연결 가이드

이 문서는 `.cursor/rules`의 클린 아키텍처, SOLID, 프론트엔드 분리 규칙을 기준으로 **현재 코드가 규칙을 지키는지**, **기능·객체가 어떻게 연결되고 작동하는지**를 정리한다.

- 적용 규칙: `clean-architecture.mdc`, `oop-solid.mdc`, `frontend-architecture.mdc`
- 점검 시점: 2026-09-08
- 위반은 보고만 한다. 수정은 별도 승인 후 진행한다.

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
infrastructure (JPA, MyBatis, Redis, Solapi, Mail, Security, NTS)
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
어긋나는 지점은 주로 **Infrastructure가 Application을 호출**하고, **컨트롤러가 Domain Policy·쿠키를 직접 다루는** 부분이다.

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
| zustand 범위 | `authStore`는 `member`, `ready`만 보관 |

### 어긋나는 점 (승인 전 수정하지 않음)

| 심각도 | 규칙 | 위치 | 내용 |
|---|---|---|---|
| 높음 | Infrastructure는 Domain만 | `SocialOAuthSuccessHandler`, `MemberSessionBinder`, `KakaoFriendsClient`, `AdminAccountInitializer` | Application 서비스·DTO를 직접 호출/사용 |
| 높음 | Presentation은 Application만 | `AuthController` | Infrastructure `MemberSessionBinder` 주입, `SecurityContextHolder.clearContext()` |
| 높음 | Presentation에 도메인 규칙 금지 | `FreeBoardController`, `QnaBoardController`, `ArchiveBoardController` | `ViewCountPolicy`, `LikePolicy`를 컨트롤러가 직접 사용 (쿠키 이름·만료) |
| 중간 | OCP | `LikeService.increase()` | `"FREE"/"QNA"/"ARCHIVE"` switch. 게시판 추가 시 이 클래스 수정 |
| 중간 | 이름 통일 | 이메일 인증 | `SendPhoneVerificationResult`, `VerifyPhoneCodeResult`, `PhoneVerificationStore`를 이메일이 재사용. 토큰 필드명도 `phone` |
| 중간 | API 봉투 | `FileController` | 파일 다운로드는 바이너리라 `ApiResponse`를 쓰지 않음 (의도 가능) |
| 낮음 | 프론트 조립만 | `SignUpPage.jsx` | fetch는 없지만 검증·채널 전환 로직이 페이지에 큼 |
| 낮음 | `useViewCount` | 규칙에 명시 | 조회수는 `useBoardDetail` → `boardApi.read`의 서버 부수효과. 전용 훅 없음 |
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
| `PhoneVerificationPolicy` | 6자리 코드, TTL 180초, 재전송 60초, 일 10회 |
| `PhoneVerificationStore` | 코드·토큰 저장 포트 (Redis 또는 메모리). 이메일 키 `MAIL:{email}`도 여기에 저장 |
| `VerificationChannel` | `EMAIL` / `PHONE` |
| `SmsSender` / `MailSender` | 문자·메일 발송 포트 |
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
| `SendPhoneVerificationService` | 휴대폰, 클라이언트 IP | 정책 검사 → 코드 생성 → `SmsSender` → 스토어 저장 |
| `VerifyPhoneCodeService` | 휴대폰, 코드 | 코드 확인 → 일회용 토큰 발급 |
| `SendEmailVerificationService` | 이메일, IP | 동일 정책·스토어, `MailSender` |
| `VerifyEmailCodeService` | 이메일, 코드 | 동일. 반환 타입은 아직 `VerifyPhoneCodeResult` |
| `VerifyBusinessRegistrationService` | 사업자번호·상호·대표·개업일 | `BusinessRegistrationGateway` 호출 |
| `SignUpService` | `SignUpCommand` | 검증 → 토큰 소비 → `Member` 생성 |
| `LoginService` | 아이디, 비밀번호 | 해시 비교. 이메일 또는 휴대폰 인증 완료 계정만 통과 |
| `SocialLoginService` | `SocialProfile` | 기존 연결 또는 신규 가입, 카카오 친구 동기화 |
| `MemberProfileService` | 로그인 아이디 | 내 정보, 프로필·비밀번호 변경 |
| `FindLoginIdService` / `PasswordResetService` | 이메일 | 아이디 안내 / 임시 비밀번호 메일 |
| `EnsureAdminMemberService` | 설정값 | 기동 시 관리자 계정 보장 |
| `FreeBoardService` 등 | 게시글 필드 | 게시판별 CRUD + 조회수 정책 |
| `CommentService` | QnA 글 ID | 댓글만 담당 |
| `LikeService` | 게시판 타입, 글 ID | 좋아요 중복 방지 후 카운트 증가 |
| `FileDownloadService` | 파일 ID | 저장소에서 바이트 조회 |

### 3.3 Infrastructure — 교체 가능한 세부사항

| 구현 | 포트 |
|---|---|
| `MemberRepositoryImpl` (JPA) | `MemberRepository` |
| `*BoardRepositoryImpl` (MyBatis) | 게시판 Repository |
| `RedisPhoneVerificationStore` / `InMemoryPhoneVerificationStore` | `PhoneVerificationStore` (`app.phone-verify.store`) |
| `SolapiSmsSender` | `SmsSender` |
| `SpringMailSender` | `MailSender` |
| `LocalFileStorage` | `FileStorage` |
| `NtsOpendataBusinessRegistrationGateway` | `BusinessRegistrationGateway` |
| `BcryptPasswordEncryptor` | `PasswordEncryptor` |
| `KakaoTalkMemoClient` | `KakaoTalkGateway` |
| `SecurityConfig` + OAuth 핸들러 | 세션·소셜 로그인 (이 계층에만 둘 것) |

도메인 정책 빈은 `DomainBeanConfig`에서 생성한다. `ViewCountPolicy`, `LikePolicy`, `PasswordPolicy`, `PhoneVerificationPolicy`, `FileTypeClassifier`.

### 3.4 Presentation — HTTP 입구

- JSON 성공/실패: `ApiResponse`
- 예외 → 메시지: `GlobalExceptionHandler`
- 회원: `MemberController`, `EmailVerificationController`, `PhoneVerificationController`
- 인증: `AuthController`
- 게시판: `FreeBoardController`, `QnaBoardController`, `ArchiveBoardController`
- 댓글/파일: `CommentController`, `FileController`
- SPA: `SpaController` (React 정적 파일)

---

## 4. 기능별 연결 · 동작

### 4.1 회원가입과 본인 인증

가입 전에 **선택한 채널에서만** 6자리 인증을 끝낸다. 가입 API는 그 토큰을 한 번 쓰고 버린다.

```
SignUpPage
  ├ SignUpTypeTabs          개인 / 기업
  ├ SignUpVerifyTabs        이메일 인증 / 휴대폰 인증
  ├ useEmailVerification    또는  usePhoneVerification
  └ useSignUp.signUp
        ↓
EmailVerificationController  |  PhoneVerificationController
        ↓
Send*VerificationService → Policy + Store + MailSender/SmsSender
Verify*CodeService       → 일회용 토큰
        ↓
MemberController POST /api/members/signup
        ↓
SignUpService
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
  ├ useAuth.login → POST /api/auth/login → LoginService → MemberSessionBinder
  └ SocialLoginButtons → /oauth2/authorization/{google|kakao|github}
                              ↓
                     Spring Security (infrastructure)
                              ↓
                     SocialOAuthSuccessHandler
                       · GitHubUserEmailClient (이메일 보강)
                       · SocialLoginService.loginOrSignUp
                       · KakaoFriendsClient → 친구 목록
                       · MemberSessionBinder.bind
                              ↓
                     SPA 리다이렉트 → App bootstrap → GET /api/members/me
```

세션은 쿠키(`credentials: 'include'`). zustand `authStore`는 서버 세션의 화면 복사본이다.

아이디 저장은 `useSavedLoginId` 쿠키. 로그아웃은 `AuthController`가 세션 무효화 + `SecurityContextHolder` 정리.

### 4.3 아이디·비밀번호 찾기

```
FindIdPage        → useAuth.findLoginId      → FindLoginIdService → MailSender
ForgotPasswordPage → useAuth.forgotPassword → PasswordResetService → 임시 비밀번호 메일
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

**조회수:** 컨트롤러가 쿠키를 읽고 `ViewCountPolicy`로 오늘 봤는지 판단한 뒤, 서비스 `read(id, alreadyViewedToday)`가 DB 증가를 결정한다. 프론트 전용 조회수 훅은 없다.

**쓰기 계약이 다른 이유 (LSP):**

| 게시판 | 인증 | write 입력 |
|---|---|---|
| 자유 | 비회원 가능, 글 비밀번호 | title, content, writer, password |
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

---

## 5. 프론트 객체 연결

```
App.jsx
  useAuth.bootstrap → memberApi.me → authStore.setMember

pages/          라우트 화면. 훅만 호출
components/     UI만. SignUpVerifyTabs, SocialLoginButtons, FileViewer, Header …
hooks/          useAuth, useSignUp, usePhoneVerification, useEmailVerification,
                useBoardList, useBoardDetail, useBoardCommand, useLike, useComment
services/api/   client.js 가 유일한 fetch
                memberApi / boardApi / commentApi
store/          authStore.js
utils/          getFileType, memberForm, passwordPolicy
```

`useBoardCommand`는 게시판 타입별 API를 맵으로 고른다 (`writeByType`, `updateByType`). 페이지가 `boardApi`를 import하지 않는다.

소셜 버튼은 fetch가 아니라 `<a href="/oauth2/authorization/...">` 리다이렉트다.

---

## 6. 요청이 한 바퀴 도는 예

휴대폰 인증 후 가입:

1. 사용자가 **휴대폰 인증** 탭을 고른다. `SignUpVerifyTabs` → `form.verificationChannel = 'PHONE'`
2. `usePhoneVerification.send` → `POST /api/members/phone/send-code`
3. `PhoneVerificationController` → `SendPhoneVerificationService`
4. `PhoneVerificationPolicy`가 번호·쿨다운·일일 한도를 검사하고 6자리 코드를 만든다
5. `SolapiSmsSender`가 문자를 보낸다. `RedisPhoneVerificationStore`가 TTL과 함께 저장한다
6. 사용자가 코드를 입력. `verify` → `VerifyPhoneCodeService` → 일회용 토큰
7. 가입 제출. `useSignUp.signUp` → `POST /api/members/signup` (`phoneVerificationToken` 포함)
8. `SignUpService`가 토큰을 소비하고 `Member.markPhoneVerified()` 후 저장한다
9. 로그인 화면으로 이동. `LoginService`는 휴대폰 인증 완료 계정을 통과시킨다

이메일을 고르면 4~6단계만 `MailSender`와 `MAIL:{email}` 키로 바뀐다. 저장소·정책 객체는 같다.

---

## 7. 빈 연결 (DIP)

Spring이 인터페이스에 구현을 꽂는다. Application 코드에 `new RedisPhoneVerificationStore`가 없다.

| 포트 | 구현 | 선택 |
|---|---|---|
| `PhoneVerificationStore` | Redis 또는 InMemory | `app.phone-verify.store` |
| `SmsSender` | `SolapiSmsSender` | Solapi 키 |
| `MailSender` | `SpringMailSender` | SMTP |
| `MemberRepository` | JPA Impl | — |
| 게시판 Repository | MyBatis Impl | — |
| `FileStorage` | `LocalFileStorage` | 로컬 디스크 |

회원은 JPA, 게시판은 MyBatis다. Domain은 둘 다 모른다.

---

## 8. 나중에 손보면 좋은 순서

규칙상 즉시 리팩토링하지 않는다. 우선순위만 적는다.

1. 세션 바인딩·OAuth 성공 처리를 Application 포트 뒤로 옮긴다. Infrastructure가 Application DTO를 직접 쓰지 않게 한다.
2. 조회수·게스트 좋아요 쿠키 판단을 컨트롤러에서 Application(또는 Presentation 어댑터)으로 옮긴다.
3. 이메일/휴대폰 공통 이름을 `VerificationStore`, `SendVerificationResult`처럼 채널 중립으로 바꾼다.
4. `LikeService`의 게시판 switch를 전략(게시판별 카운트 증가 포트)으로 바꾼다.

프론트는 fetch 격리를 이미 지킨다. 남는 것은 `SignUpPage` 로직을 훅으로 더 내리는 일과, 규칙에 적힌 `useViewCount`를 둘지 여부다.
