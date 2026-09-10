# Hexaq 문서

구현 문서를 **features · security · technical**로 나눈다. 번호 파일은 항목당 하나다. 폴더·파일명은 영문이다.

| 폴더 | 역할 | 입구 |
|---|---|---|
| [features/](./features/overview.md) | 회원·게시판 등 사용자 기능 | [기능종합](./features/overview.md) |
| [security/](./security/overview.md) | 7겹 방어와 기술별 상세 | [보안종합](./security/overview.md) |
| [technical/](./technical/overview.md) | 아키텍처·기술 사양 | [기술종합](./technical/overview.md) |

Cursor 규칙은 `.cursor/rules/`에 있다. 이 폴더의 md와 역할을 섞지 않는다. 1~2분 대본은 [발표](./presentation.md).

2026-09-10 객체 정리는 [아키텍처 §8](./technical/01-architecture.md). 같은 날 터널·CORS·소셜 콜백은 [Cloudflare Tunnel](./technical/03-cloudflare-tunnel.md), [04 소셜](./features/04-social-login.md).

---

## 기능

| 문서 | 한 줄 |
|---|---|
| [기능종합](./features/overview.md) | 구조, DIP, 기능 목차 |
| [01 회원가입 · 본인 인증](./features/01-signup-verification.md) | 이메일 DI. 가입 전 이메일 또는 휴대폰 인증 |
| [02 기업 회원 · 사업자 확인](./features/02-business-registration.md) | 국세청 확인 후 기업 가입 |
| [03 로그인 · 로그아웃](./features/03-login-logout.md) | 세션 쿠키 |
| [04 소셜 로그인](./features/04-social-login.md) | Google / Kakao / GitHub. 터널이면 콘솔 콜백 추가 |
| [05 아이디 · 비밀번호 찾기](./features/05-find-id-password.md) | 안내 메일 |
| [06 마이페이지](./features/06-mypage.md) | 프로필·비밀번호 |
| [07 게시판](./features/07-boards.md) | 자유 / QnA / 자료실 |
| [08 조회수 · 좋아요 · 댓글](./features/08-views-likes-comments.md) | 쿠키·LikePolicy·QnA 댓글 |
| [09 자료실 첨부](./features/09-archive-attachments.md) | 파일 타입 전략 |

---

## 보안

| 문서 | 한 줄 |
|---|---|
| [보안종합](./security/overview.md) | 7겹 방어, 시너지, 잔여 리스크 |
| [01 HMAC](./security/01-hmac-request-signing.md) | 일회용 티켓 서명 |
| [02 레이트 리밋](./security/02-rate-limiting.md) | IP+경로 분당 한도 |
| [03 reCAPTCHA v3](./security/03-recaptcha-v3.md) | 점수 기반 봇 차단. SMS 전 게이트 |
| [04 허니팟](./security/04-honeypot-field.md) | 가입 `website` 필드 |
| [05 일회용 메일](./security/05-disposable-email.md) | 도메인 블랙리스트, DB 전 |
| [06 본인인증](./security/06-identity-verification.md) | 이메일 DI. 소유 확인은 이메일 또는 휴대폰 6자리 |
| [07 이상 탐지](./security/07-anomaly-guard.md) | 가입 직후 TTFA·시퀀스 |

---

## 기술

| 문서 | 한 줄 |
|---|---|
| [기술종합](./technical/overview.md) | 기술 문서 안내 |
| [01 아키텍처](./technical/01-architecture.md) | 계층·객체 연결, 규칙 점검 |
| [02 기술사양서](./technical/02-technical-specification.md) | API·환경·보안 사양 |
| [03 Cloudflare Tunnel](./technical/03-cloudflare-tunnel.md) | Quick Tunnel, CORS, 흰 화면, OAuth `{baseUrl}` |
| [04 Redis](./technical/04-redis.md) | 인증 코드·HMAC 티켓·이상 탐지. 구축 근거와 요건. Compose는 루트 `docker-compose.redis.yml` |
| [05 반응형](./technical/05-responsive.md) | 폰·태블릿 1180px. `Header` / `CompactNav` |

환경 변수 키만 `.env.example`. 실제 비밀값은 `.env`이며 문서에 적지 않는다.
