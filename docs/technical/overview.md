# Hexaq 기술 종합

현재 구현 기준의 스택·계층·사양 문서 안내.

| 항목 | 내용 |
|---|---|
| 갱신일 | 2026-09-10 |
| 관련 | [docs/README.md](../README.md), [기능종합](../features/overview.md), [보안종합](../security/overview.md) |

---

## 문서

| 번호 | 문서 | 역할 |
|---|---|---|
| 01 | [아키텍처](./01-architecture.md) | 계층·객체 연결, 규칙 준수 점검. 2026-09-10 객체 정리 반영. 남은 위반은 보고만 |
| 02 | [기술사양서](./02-technical-specification.md) | 무엇을 어떤 조건으로 제공하는지. API·환경·보안 사양 |
| 03 | [Cloudflare Tunnel](./03-cloudflare-tunnel.md) | Quick Tunnel 공개. CORS·흰 화면·소셜 `{baseUrl}` |
| 04 | [Redis](./04-redis.md) | 인증 코드·HMAC 티켓·이상 탐지. 구축 근거와 요건. Compose는 루트 `docker-compose.redis.yml` |
| 05 | [반응형](./05-responsive.md) | 폰·태블릿 1180px. `useCompactNav` · `CompactNav` · `hexaq-compact.css` |

비밀값 키 목록은 프로젝트 루트 `.env.example`. 값은 `.env`에만 두고 문서에 적지 않는다.

---

## 한 줄

- 의존성은 **바깥 → 안쪽**. Domain은 Spring Web / MyBatis / SecurityContext를 쓰지 않는다
- React SPA를 Spring Boot가 정적 파일로 함께 서빙한다. 기본 포트 `8282`
- API JSON 봉투는 `{ success, data, message }`
- 페이지는 `fetch`를 하지 않는다. `pages → hooks → services/api`
- 로컬 공개는 프로필 `tunnel` + Cloudflare Quick Tunnel. 상세는 [Cloudflare Tunnel](./03-cloudflare-tunnel.md)
