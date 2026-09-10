# 06. 마이페이지

| 항목 | 내용 |
|---|---|
| 관련 | [기능종합](./overview.md) |

**구현:** 로그인 필수. 프로필·비밀번호 변경. 카카오 친구는 표시만.

| 구분 | 내용 |
|---|---|
| 화면 | `MyPage` (`PrivatePage` 가드) |
| API | `GET /api/members/me` · `PUT /profile` · `PUT /password` |
| 유스케이스 | `MemberProfileService` |

카카오 API를 프론트가 다시 치지 않는다. `/me` 응답을 보여준다.

가입 직후 `/me` GET은 이상 탐지의 CONTEXT 단계다. [이상 탐지](../security/07-anomaly-guard.md).
