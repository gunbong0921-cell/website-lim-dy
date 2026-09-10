# 04. 소셜 로그인

| 항목 | 내용 |
|---|---|
| 관련 | [기능종합](./overview.md), [03 로그인](./03-login-logout.md), [Cloudflare Tunnel](../technical/03-cloudflare-tunnel.md) |

**구현:** Google / Kakao / GitHub. 프론트는 fetch가 아니라 OAuth 리다이렉트.

| 구분 | 내용 |
|---|---|
| 화면 | `SocialLoginButtons` |
| 진입 | `/oauth2/authorization/{google\|kakao\|github}` |
| 콜백 | `/login/oauth2/code/{registrationId}` |
| 로컬 URI | `http://localhost:8282/login/oauth2/code/{id}` (`application.properties`) |
| 터널·운영 URI | `{baseUrl}/login/oauth2/code/{id}` (`application-tunnel.properties` / `application-prod.properties`) |
| 처리 | `SocialOAuthSuccessHandler` → `SocialLoginService` → `SessionPrincipal` → `MemberSessionBinder.bind` |
| 부가 | GitHub 이메일 보강 · 카카오 친구 동기화 |

`SocialOAuthSuccessHandler`와 `KakaoFriendsClient`는 아직 Application 서비스·DTO를 직접 쓴다. 남은 위반은 [아키텍처](../technical/01-architecture.md) §2.

돌아온 뒤 `GET /api/members/me`로 세션 사용자를 화면에 올린다.

### 콘솔 불일치

공개 URL로 열면 앱은 그 호스트를 `redirect_uri`로 보낸다. 콘솔에 `localhost`만 있으면 아래가 난다.

| 제공자 | 증상 | 콘솔에 넣을 것 |
|---|---|---|
| Google | `redirect_uri_mismatch` | 리디렉션 URI **추가**. 자바스크립트 원본에 공개 호스트 |
| GitHub | `redirect_uri is not associated` | 콜백 URL **교체**(앱당 하나) |
| Kakao | `KOE006` | Redirect URI **추가** + Web 사이트 도메인 |

로컬과 터널을 같이 쓰려면 Google·Kakao는 localhost를 남기고 공개 주소를 더한다. GitHub는 한 시점에 콜백 하나다.

일회용 메일 차단과 HMAC은 소셜 리다이렉트에 걸지 않는다. 제공자 이메일이 DI(`Member.email` unique)다. 같은 메일이면 기존 계정에 붙는다. 신규 소셜 회원은 [이상 탐지](../security/07-anomaly-guard.md) 가입 시각을 기록한다.
