# Hexaq 기능 구현 발표

클린 아키텍처 · 기능별로 무엇이 어떻게 구현됐는지

---

## 한 장으로 보는 구조

의존성은 바깥에서 안쪽으로만 흐른다.

```
화면 (React)
  → Presentation (Controller)
    → Application (유스케이스)
      → Domain (규칙 · 인터페이스)
        ← Infrastructure (JPA / MyBatis / Redis / SMS / Mail)
```

프론트도 같다. **페이지는 fetch를 하지 않는다.**

```
pages → hooks → services/api → 서버
```

응답은 항상 `{ success, data, message }`

---

## 1. 회원가입 · 본인 인증

**구현:** 가입 전에 이메일 또는 휴대폰 중 **하나**를 골라 6자리 인증을 끝낸다.

| 구분 | 내용 |
|---|---|
| 화면 | `SignUpPage` · 개인/기업 탭 · 이메일/휴대폰 탭 |
| 훅 | `useSignUp` · `useEmailVerification` · `usePhoneVerification` |
| API | `POST /api/members/email|phone/send-code` · `/verify` · `/signup` |
| 유스케이스 | `Send*VerificationService` → `Verify*CodeService` → `SignUpService` |
| 규칙 | `PhoneVerificationPolicy` (TTL 180초, 재전송 60초, 일 10회) |
| 구현 | Solapi SMS · SMTP 메일 · Redis(또는 메모리) 코드 저장 |

**흐름**

1. 탭으로 인증 수단 선택
2. 코드 발송 → Redis에 TTL 저장
3. 코드 확인 → 일회용 토큰 발급
4. 가입 시 토큰을 한 번 쓰고 버림
5. 이메일 인증 또는 휴대폰 인증 완료 계정만 로그인 가능

---

## 2. 기업 회원 · 사업자 확인

**구현:** 국세청 API로 사업자번호·상호·대표·개업일을 확인한 뒤에만 기업 가입.

| 구분 | 내용 |
|---|---|
| 화면 | 기업 탭 · 사업자 확인 버튼 |
| API | `POST /api/members/business/verify` |
| 유스케이스 | `VerifyBusinessRegistrationService` |
| 포트 | `BusinessRegistrationGateway` |
| 구현 | `NtsOpendataBusinessRegistrationGateway` |

개인 회원은 이 단계를 타지 않는다.

---

## 3. 로그인 · 로그아웃

**구현:** 이메일(아이디) + 비밀번호. 세션 쿠키. 화면 상태는 zustand.

| 구분 | 내용 |
|---|---|
| 화면 | `LoginPage` · 아이디 저장(쿠키) |
| 훅 | `useAuth.login` |
| API | `POST /api/auth/login` · `/logout` |
| 유스케이스 | `LoginService` |
| 보안 | `MemberSessionBinder` · BCrypt · `RateLimitFilter`(로그인 분당 10회) |

로그인 조건: **이메일 인증 완료 또는 휴대폰 인증 완료**

---

## 4. 소셜 로그인

**구현:** Google / Kakao / GitHub. 프론트는 fetch가 아니라 OAuth 리다이렉트.

| 구분 | 내용 |
|---|---|
| 화면 | `SocialLoginButtons` |
| 진입 | `/oauth2/authorization/{google\|kakao\|github}` |
| 처리 | `SocialOAuthSuccessHandler` → `SocialLoginService` |
| 부가 | GitHub 이메일 보강 · 카카오 친구 동기화 |

돌아온 뒤 `GET /api/members/me`로 세션 사용자를 화면에 올린다.

---

## 5. 아이디 · 비밀번호 찾기

**구현:** 가입 이메일로 안내 메일 발송.

| 기능 | 화면 | 유스케이스 | 결과 |
|---|---|---|---|
| 아이디 찾기 | `FindIdPage` | `FindLoginIdService` | 로그인 아이디 메일 |
| 비밀번호 찾기 | `ForgotPasswordPage` | `PasswordResetService` | 임시 비밀번호 메일 |

메일 포트는 `MailSender` → `SpringMailSender` 한 곳만 탄다.

---

## 6. 마이페이지

**구현:** 로그인 필수. 프로필·비밀번호 변경. 카카오 친구는 표시만.

| 구분 | 내용 |
|---|---|
| 화면 | `MyPage` (`PrivatePage` 가드) |
| API | `GET /api/members/me` · `PUT /profile` · `PUT /password` |
| 유스케이스 | `MemberProfileService` |

카카오 API를 프론트가 다시 치지 않는다. `/me` 응답을 보여준다.

---

## 7. 게시판 (자유 / QnA / 자료실)

**구현:** 게시판마다 서비스를 따로 둔다. 하나로 합치지 않음 (SRP).

| 게시판 | 쓰기 조건 | 서비스 |
|---|---|---|
| 자유 | 비회원 가능 · 글 비밀번호 | `FreeBoardService` |
| QnA | 로그인 · 솔루션 영역 | `QnaBoardService` |
| 자료실 | 로그인 · 첨부 필수 | `ArchiveBoardService` |

공통 화면은 훅으로만 갈린다.

```
목록  useBoardList
읽기  useBoardDetail
쓰기  useBoardCommand   ← 타입별 API를 맵으로 선택
```

회원은 JPA, 게시판은 MyBatis. Domain은 둘 다 모른다.

---

## 8. 조회수 · 좋아요 · 댓글

### 조회수

하루 1회만 올린다. `ViewCountPolicy` + 쿠키.

### 좋아요

`useLike` → `LikeService` → `LikePolicy`

- 로그인은 DB로 중복 방지
- 자유게시판만 게스트 좋아요 (쿠키)

### 댓글

QnA만. 게시판 서비스에 넣지 않고 `CommentService`로 분리.

```
useComment → /api/comments
```

---

## 9. 자료실 첨부

**구현:** 타입이 늘어도 switch를 고치지 않는다. 전략 맵.

```
업로드  ArchiveBoardService
        → FileStorage.store
        → FileTypeClassifier.classify   (image / video / audio / download)

화면    getFileType.js
        → FileViewer.strategies[type]
```

다운로드: `GET /api/files/{id}` → `FileDownloadService`

---

## 10. 설정 · 공격 대비

**구현:** 사이트는 열어 두고, 비밀값은 `.env`로 빼며, 반복 요청만 끊는다.

| 구분 | 내용 |
|---|---|
| 비밀값 | `.env` (Git 제외). 템플릿 `.env.example` |
| 설정 | `application.properties`는 `${환경변수}`만 참조 |
| CORS | `WebCorsConfig` ← `CORS_ALLOWED_ORIGINS` |
| IP | `ClientIpResolver`. 프록시 뒤에서만 `TRUSTED_PROXY=true` |
| 필터 | `RateLimitFilter` (정적 파일 제외) |

| 대상 | 분당 IP 한도 |
|---|---|
| 페이지 방문 | 300 |
| 일반 API | 60 |
| 로그인·가입·인증번호 | 10 |
| JS / CSS / 이미지 | 제한 없음 |

한도 초과 시 `429`와 `"요청이 너무 많습니다."`만 반환한다.

---

## 교체 가능한 구현 (DIP)

Application은 인터페이스만 본다. 구현은 설정의 문제다.

| 무엇을 | 인터페이스 | 지금 구현 |
|---|---|---|
| 회원 저장 | `MemberRepository` | JPA |
| 게시글 저장 | 게시판 Repository | MyBatis |
| 인증 코드 | `PhoneVerificationStore` | Redis / 메모리 |
| 문자 | `SmsSender` | Solapi |
| 메일 | `MailSender` | SMTP |
| 파일 | `FileStorage` | 로컬 디스크 |
| 사업자 | `BusinessRegistrationGateway` | 국세청 |
| 비밀값 | 환경 변수 | `.env` → `application.properties` |

---

## 발표 한 줄 요약

- **화면**은 조립만, **훅**이 유스케이스, **API**만 fetch
- **도메인**에 규칙, **애플리케이션**에 트랜잭션, **인프라**에 Spring·DB·외부 API
- 인증·메일·SMS·파일·사업자 확인은 **포트에 구현을 꽂는** 방식
- 게시판·댓글은 **역할이 다른 서비스를 억지로 합치지 않음**
- 비밀값은 **`.env`**, 홈페이지는 열어 두고 **반복 요청만** `RateLimitFilter`로 제한
