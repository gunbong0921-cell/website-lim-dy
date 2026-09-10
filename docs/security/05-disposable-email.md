# Hexaq 일회용 이메일 차단

| 항목 | 내용 |
|---|---|
| 목적 | Temp-mail·Guerrilla Mail 등 일회용 도메인으로 대량 가입·메일 인증 연타를 DB 조회 전에 거절 |
| 적용일 | 2026-09-10 |
| 관련 문서 | [보안종합](./overview.md), [본인인증](./06-identity-verification.md), [아키텍처](../technical/01-architecture.md), [기술사양서](../technical/02-technical-specification.md) |

목록 파일은 클래스패스에 둔다. 런타임에 GitHub를 치지 않는다.

---

## 1. 왜 쓰는가

레이트 리밋은 IP가 바뀌면 약하다. 일회용 메일로 가입·인증번호 메일을 반복하면 SMTP와 Redis만 소모되고, 이메일 DI를 무한히 만들 수 있다. 도메인 블랙리스트와 형식 검사를 **회원 테이블 조회 전**에 둔다.

막는 것: 공개 임시메일 도메인, 잘못된 형식(`..`, 길이 초과).  
막지 못하는 것: 목록에 없는 신규 도메인, 실제 Gmail/Naver를 대량으로 쓰는 경우.

소셜 로그인 이메일은 제공자가 확인한 값이므로 이 검사를 타지 않는다. 아이디·비밀번호 찾기는 이미 가입된 주소만 조회하므로 블랙리스트를 적용하지 않는다.

---

## 2. 적용 범위

DB 조회 전:

| 엔드포인트 | 유스케이스 |
|---|---|
| `GET /api/members/check-email` | `SignUpService.isEmailAvailable` |
| `POST /api/members/email/send-code` | `SendEmailVerificationService.send` |
| `POST /api/members/signup` | `SignUpService.signUp` / `validate` |

실패 메시지:

- 형식: `"이메일 형식이 올바르지 않습니다."`
- 일회용 도메인: `"일회용 이메일은 사용할 수 없습니다."`

---

## 3. 객체 연결

Application은 파일 경로를 모른다. 목록 구현체 대신 `DisposableEmailCatalog`만 본다.

```
Presentation
  → SignUpService / SendEmailVerificationService
      → RejectDisposableEmailService.requireAllowed
          → EmailPolicy.validFormat / domainOf
          → DisposableEmailCatalog.blocked
                ↑
          ClasspathDisposableEmailCatalog
          classpath:security/disposable-email-domains.txt
```

| 계층 | 객체 | 책임 |
|---|---|---|
| Domain | `EmailPolicy` | 길이 100, 로컬@도메인 패턴. 일회용 여부는 모름 |
| Domain | `DisposableEmailCatalog` | `blocked(domain)` 포트. 하위 도메인은 상위 도메인으로 재검사 |
| Application | `RejectDisposableEmailService` | 소문자 정규화 → 형식 → 카탈로그 |
| Infrastructure | `ClasspathDisposableEmailCatalog` | 기동 시 txt를 Set으로 로드 |
| 프론트 | `isValidEmailFormat` | 화면 힌트. 차단의 정본은 서버 |

`EmailPolicy`는 `DomainBeanConfig`에 등록한다.

---

## 4. 목록 파일

`src/main/resources/security/disposable-email-domains.txt`

한 줄에 도메인 하나. `#` 줄과 빈 줄은 무시. 스냅샷 출처: [disposable-email-domains](https://github.com/disposable-email-domains/disposable-email-domains). 목록을 갱신할 때는 이 파일만 교체한다.

TLD(`com`)는 넣지 않는다. 넣으면 모든 메일이 막힌다.
