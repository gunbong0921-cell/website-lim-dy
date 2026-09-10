# 08. 조회수 · 좋아요 · 댓글

| 항목 | 내용 |
|---|---|
| 관련 | [기능종합](./overview.md), [07 게시판](./07-boards.md) |

### 조회수

하루 1회만 올린다.

| 계층 | 객체 | 책임 |
|---|---|---|
| Domain | `ViewCountPolicy` | 쿠키 이름·만료, 하루 1회 규칙 |
| Application | `BoardCookieService` | `CookieInstruction` 반환. 컨트롤러가 Policy를 보지 않음 |
| Presentation | 게시판 컨트롤러 | 쿠키 존재만 보고 `read(id, alreadyViewedToday)` |

프론트 전용 `useViewCount`는 없다. 조회수는 서버 쿠키 부수효과다.

### 좋아요

`useLike` → `LikeService` → `BoardLikeCounter` 맵 + `LikePolicy`

| 계층 | 객체 | 책임 |
|---|---|---|
| Domain | `LikePolicy` | 게스트 허용(자유게시판), 쿠키 이름 |
| Domain | `BoardLikeCounter` | 게시판별 카운트 증가 포트 |
| Infrastructure | `FreeBoardLikeCounter` 등 | MyBatis 카운트 증가 |
| Application | `BoardCookieService` | 게스트 좋아요 쿠키 |

- 로그인은 DB로 중복 방지
- 자유게시판만 게스트 좋아요 (쿠키)
- 게시판을 늘릴 때는 `LikeService` switch가 아니라 카운터 빈을 추가한다

### 댓글

QnA만. 게시판 서비스에 넣지 않고 `CommentService`로 분리.

```
useComment → /api/comments
```

로그인 회원의 게시글·댓글·좋아요 쓰기는 [이상 탐지](../security/07-anomaly-guard.md)의 핵심 API다.
