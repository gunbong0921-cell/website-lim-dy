# Hexaq reCAPTCHA v3

| 항목 | 내용 |
|---|---|
| 목적 | 봇·스팸·자동화 요청을 점수 기반으로 사전 차단 |
| 적용일 | 2026-09-10 |
| 관련 문서 | [보안종합](./overview.md), [레이트 리밋](./02-rate-limiting.md), [허니팟](./04-honeypot-field.md), [일회용 메일](./05-disposable-email.md), [HMAC](./01-hmac-request-signing.md), [본인인증](./06-identity-verification.md), [이상 탐지](./07-anomaly-guard.md), [아키텍처](../technical/01-architecture.md) |

사이트 키는 프론트에 내려도 된다. 비밀 키는 `.env`에만 둔다. 값은 이 문서에 적지 않는다.

---

## 1. 왜 쓰는가

IP 레이트 리밋만으로는 분산 봇을 막기 어렵다. 특히 SMS 인증번호는 Solapi 비용이 나가므로, **문자를 보내기 전에** Google 점수를 본다.

- 점수 미달(기본 0.5 미만, 0.3 이하 봇 포함)이면 `SmsSender.send`를 호출하지 않는다.
- 일일 발송 한도·쿨다운 카운터도 소모하지 않는다.
- 회선 포화형 디도스는 이 기능으로 막지 못한다. CDN/WAF가 필요하다.

---

## 2. 적용 범위

캡차가 **켜진 경우** 아래 요청은 `recaptchaToken`이 필요하고, 점수가 컷오프 이상이며 action이 일치해야 한다.

| 엔드포인트 | action | 프론트 훅 |
|---|---|---|
| `POST /api/auth/login` | `login` | `useAuth.login` |
| `POST /api/members/signup` | `signup` | `useSignUp.signUp` |
| `POST /api/members/email/send-code` | `email_send_code` | `useEmailVerification.send` |
| `POST /api/members/phone/send-code` | `phone_send_code` | `usePhoneVerification.send` |
| `POST /api/auth/forgot-id` | `forgot_id` | `useAuth.findLoginId` |
| `POST /api/auth/forgot-password` | `forgot_password` | `useAuth.forgotPassword` |
| `POST /api/boards/free` | `board_write_free` | `useBoardCommand` (free write만) |

적용하지 않는 것: 인증코드 확인, 사업자 확인, 로그인 후 QnA/자료실/댓글, 소셜 로그인 리다이렉트, 자유게시판 수정·삭제.

---

## 3. 객체 연결

의존성은 바깥 → 안쪽만 허용한다. Application은 `GoogleRecaptchaV3Verifier`가 아니라 `CaptchaVerifier`만 본다.

```
pages (조립만)
  → hooks          useRecaptcha.execute(action)
  → services/api   body.recaptchaToken
        ↓
Presentation       토큰·IP·호스트만 전달. 점수 판정 없음
        ↓
VerifyCaptchaService.require(token, CaptchaAction, ip, host)
        ↓
CaptchaVerifier    포트
        ↑
GoogleRecaptchaV3Verifier   siteverify POST
        ↓
CaptchaPolicy.passed(result, expectedAction, minScore)
```

| 계층 | 객체 | 책임 |
|---|---|---|
| Domain | `CaptchaVerifier` | 검증 포트 |
| Domain | `CaptchaResult` | `success`, `score`, `action`. 네트워크 실패는 `success=false` |
| Domain | `CaptchaAction` | 위 7개 action 문자열. 검증기는 switch하지 않음 |
| Domain | `CaptchaPolicy` | 성공 + `score >= minScore` + action 일치. `requiredOnHost`는 `*.trycloudflare.com`만 false |
| Application | `VerifyCaptchaService` | `require`. 터널 호스트이거나 비활성이면 no-op |
| Application | `GetRecaptchaPublicConfigService` | `{ recaptchaEnabled, recaptchaSiteKey }`. 터널 호스트면 끔 |
| Infrastructure | `GoogleRecaptchaV3Verifier` | Google `siteverify`. 점수 판정 금지 |
| Presentation | `PublicConfigController` / `RequestHostname` | `GET /api/public/config`. 호스트만 전달 |
| Hooks | `useRecaptcha` | 터널 호스트면 빈 토큰. 아니면 설정 로드 → 스크립트 삽입 → `grecaptcha.execute` |
| API | `publicApi` | 설정 GET만 |

`CaptchaPolicy`는 `DomainBeanConfig`에 등록한다.

---

## 4. SMS 발송 전 게이트

`POST /api/members/phone/send-code` (`SendPhoneVerificationService.send`) 순서:

1. `VerifyCaptchaService.require(..., PHONE_SEND_CODE, ip, host)`
2. 번호 정규화 · 쿨다운 · 일 10회 (`PhoneVerificationPolicy`)
3. 그다음에만 `SmsSender.send`

실패 메시지: `"로봇 확인에 실패했습니다. 다시 시도해 주세요."`  
`GlobalExceptionHandler`가 400 + `{ success: false, data: null, message }`로 내려준다.

`RateLimitFilter`(인증 버킷 10/분)는 그대로 둔다. 캡차는 점수, 레이트리밋은 IP 반복이다.

---

## 5. 프론트

페이지는 `grecaptcha`와 `fetch`를 직접 호출하지 않는다.

1. 브라우저 호스트가 `*.trycloudflare.com`이면 스크립트를 넣지 않고 빈 토큰을 준다.
2. 아니면 `GET /api/public/config`로 사이트 키를 받는다. 서버도 같은 호스트 규칙으로 `recaptchaEnabled`를 내린다.
3. 캡차가 꺼져 있으면 빈 토큰을 주고 끝낸다.
4. 켜져 있으면 `https://www.google.com/recaptcha/api.js?render={siteKey}`를 넣고 `execute(action)`한다.
5. `Invalid site key`는 한글로 안내한다. 기존 훅이 토큰을 API 바디에 붙인다.

사이트 키를 `VITE_*`로 굽지 않는다. 8282 정적 빌드도 백엔드 설정을 그대로 쓴다.

---

## 6. 설정

`.env` (값은 Git에 올리지 않음):

| 키 | 기본 | 의미 |
|---|---|---|
| `RECAPTCHA_ENABLED` | `true` | 기능 스위치 |
| `RECAPTCHA_SITE_KEY` | 빈 값 | 공개 사이트 키 |
| `RECAPTCHA_SECRET_KEY` | 빈 값 | 서버 비밀 키 |
| `RECAPTCHA_MIN_SCORE` | `0.5` | 이 점수 미만이면 거부 |

`application.properties`:

```
app.recaptcha.enabled=${RECAPTCHA_ENABLED:true}
app.recaptcha.site-key=${RECAPTCHA_SITE_KEY:}
app.recaptcha.secret-key=${RECAPTCHA_SECRET_KEY:}
app.recaptcha.min-score=${RECAPTCHA_MIN_SCORE:0.5}
app.recaptcha.verify-url=https://www.google.com/recaptcha/api/siteverify
```

사이트 키 또는 비밀 키가 비어 있으면 `enabled`와 관계없이 검증을 건너뛴다.

Google 콘솔에 사용할 도메인(`localhost` 및 고정 공개 호스트)을 등록해야 토큰이 나온다. Quick Tunnel(`*.trycloudflare.com`)은 호스트가 매번 바뀌므로 캡차를 건너뛴다. 같은 프로세스의 `localhost:8282`는 캡차가 그대로 돈다. 상세는 [Cloudflare Tunnel](../technical/03-cloudflare-tunnel.md).

---

## 8. 남은 한계와 보완

점수·action 검사로도 아래는 남는다.

- IP를 바꿔 가며 천천히 치는 분산 요청
- 마우스·타이밍을 흉내 내는 스텔스 봇(점수를 높게 받음)
- 캡차 해독 대행으로 토큰을 통과시키는 경우

앱 한도로 막지 못하는 회선 포화형 디도스는 CDN/WAF가 필요하다. 단순 HTML 봇·임시메일·URL만 아는 스크립트는 다음으로 한 겹 더 둔다.

| 한계 | 보완 | 문서 |
|---|---|---|
| 같은 IP 무차별 연타 | IP+경로 분당 한도 | [레이트 리밋](./02-rate-limiting.md) |
| HTML 자동 작성 봇 | 가입 폼 허니팟 | [허니팟](./04-honeypot-field.md) |
| 일회용 메일 대량 가입 | 도메인 블랙리스트 | [일회용 메일](./05-disposable-email.md) |
| Postman/cURL 직접 호출 | 일회용 티켓 HMAC | [HMAC](./01-hmac-request-signing.md) |
| 같은 이메일 재가입 | `email` unique (DI) | [본인인증](./06-identity-verification.md) |
| 미인증 계정 로그인 | 가입 전 이메일 또는 휴대폰 6자리 | [본인인증](./06-identity-verification.md) |
| 가입 직후 API 연타 | TTFA·신규 한도·Zero-Nav·시퀀스 | [이상 탐지](./07-anomaly-guard.md) |

스텔스 봇과 캡차 대행 후 **시간을 두고** 사람처럼 쓰는 경우는 이상 탐지의 즉시 직행 규칙으로도 남는다. 종합은 [보안종합](./overview.md).

---

## 9. 공개 설정 API

`GET /api/public/config` — 인증 없음.

```json
{
  "success": true,
  "data": {
    "recaptchaEnabled": true,
    "recaptchaSiteKey": "(사이트 키)"
  },
  "message": null
}
```

비밀 키는 응답에 넣지 않는다.
