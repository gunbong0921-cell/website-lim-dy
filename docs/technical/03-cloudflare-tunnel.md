# 03. Cloudflare Tunnel (로컬 공개)

| 항목 | 내용 |
|---|---|
| 목적 | 로컬 `8282`(SPA+API 한 출처)를 HTTPS 공개 URL로 연다 |
| 적용일 | 2026-09-10 |
| 관련 | [기술종합](./overview.md), [기술사양서](./02-technical-specification.md), [아키텍처](./01-architecture.md), [04 소셜](../features/04-social-login.md), [reCAPTCHA](../security/03-recaptcha-v3.md) |

Vercel에는 Spring Boot를 올리지 않는다. Quick Tunnel은 Cloudflare 계정·고유 도메인이 없어도 된다.

---

## 1. 객체

| 계층 | 객체 | 책임 |
|---|---|---|
| 설정 | `application-tunnel.properties` | 포워드 헤더, `TRUSTED_PROXY`, OAuth `{baseUrl}` |
| Infrastructure | `WebCorsConfig` | `allowedOriginPatterns`. `https://*.trycloudflare.com` 기본 포함 |
| Domain | `CaptchaPolicy.requiredOnHost` | `*.trycloudflare.com`에서만 캡차 생략. localhost는 그대로 검증 |
| Presentation | `RequestHostname` | `Host` / `X-Forwarded-Host`를 정책에 넘김 |
| Domain | `ClientAddressPolicy` | 터널 뒤 클라이언트 IP·공개 호스트 |
| 프론트 빌드 | Vite `same-origin-assets` | 같은 출처 JS·CSS에서 `crossorigin` 제거 |
| 스크립트 | `scripts/cloudflare-tunnel.ps1` | `cloudflared tunnel --url http://127.0.0.1:8282` |

프로필 `tunnel`일 때 Spring은 `X-Forwarded-Proto` / `X-Forwarded-Host`로 공개 URL을 본다. 소셜 `redirect_uri`는 `{baseUrl}/login/oauth2/code/{registrationId}`다.

---

## 2. 기동

1. 로컬 Oracle·Redis 6+(또는 `PHONE_VERIFY_STORE=memory`)·`.env`. Redis 요건은 [04 Redis](./04-redis.md)
2. `winget install --id Cloudflare.cloudflared -e` (최초 1회)
3. 앱: `.\gradlew bootRun --args="--spring.profiles.active=tunnel"`
4. 터널: `.\scripts\cloudflare-tunnel.ps1`

로그의 `https://*.trycloudflare.com` 이 공개 주소다. **프로세스를 끄면 주소가 죽는다. Quick Tunnel은 켤 때마다 호스트가 바뀐다.**  
재시작 시 소셜 콜백을 유지하려면 **터널은 끄지 않고** 앱만 재기동한다.

---

## 3. 흰 화면

Vite는 JS·CSS에 `crossorigin`을 붙인다. 브라우저가 `Origin`을 보내면:

1. 허용 패턴에 터널 호스트가 없으면 Spring이 **403**
2. Cloudflare가 `Access-Control-Allow-Origin`을 빼면 모듈·CSS가 막힘

결과는 HTML만 보이는 **흰 화면**(CSS 미적용). 대응은 `WebCorsConfig` 패턴 + 빌드 HTML에서 같은 출처 `crossorigin` 제거. 브라우저에 예전 HTML이 남으면 Ctrl+F5.

---

## 4. 콘솔 (호스트가 바뀔 때마다)

`{공개호스트}` = 터널 로그의 `https://….trycloudflare.com` (끝 슬래시 없음).

| 대상 | 등록 |
|---|---|
| reCAPTCHA | 공개 터널 호스트(`*.trycloudflare.com`)에서만 캡차를 끈다. 같은 JVM의 `localhost:8282`는 그대로 켠다. 콘솔에 도메인을 넣은 고정 호스트는 캡차가 돈다 |
| Google | 승인된 자바스크립트 원본 `{공개호스트}`. 리디렉션 URI `{공개호스트}/login/oauth2/code/google` (**추가**, localhost는 유지) |
| Kakao | Web 사이트 도메인 `{공개호스트}`. Redirect URI `{공개호스트}/login/oauth2/code/kakao` (**추가**) |
| GitHub | Authorization callback URL은 앱당 하나. `{공개호스트}/login/oauth2/code/github`로 **교체** |

앱이 보내는 콜백이 콘솔과 다르면 Google `redirect_uri_mismatch`, GitHub `redirect_uri`, Kakao `KOE006`. 상세는 [04 소셜](../features/04-social-login.md).

이메일·비밀번호 로그인은 OAuth 콘솔과 무관하다. 공개 터널에서는 캡차를 건너뛰므로 가입·인증번호 발송이 막히지 않는다. `localhost:8282`는 Google 콘솔에 등록된 사이트 키로 캡차가 동작한다.

PC·DB·8282·터널 프로세스가 켜져 있어야 한다.
