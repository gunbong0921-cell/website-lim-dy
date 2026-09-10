# 02. 기업 회원 · 사업자 확인

| 항목 | 내용 |
|---|---|
| 관련 | [기능종합](./overview.md), [01 회원가입](./01-signup-verification.md) |

**구현:** 국세청 API로 사업자번호·상호·대표·개업일을 확인한 뒤에만 기업 가입.

| 구분 | 내용 |
|---|---|
| 화면 | 기업 탭 · 사업자 확인 버튼 |
| API | `POST /api/members/business/verify` |
| 유스케이스 | `VerifyBusinessRegistrationService` |
| 포트 | `BusinessRegistrationGateway` |
| 구현 | `NtsOpendataBusinessRegistrationGateway` |

개인 회원은 이 단계를 타지 않는다.
