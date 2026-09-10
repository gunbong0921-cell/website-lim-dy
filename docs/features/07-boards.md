# 07. 게시판 (자유 / QnA / 자료실)

| 항목 | 내용 |
|---|---|
| 관련 | [기능종합](./overview.md), [08 조회수·좋아요·댓글](./08-views-likes-comments.md), [09 자료실 첨부](./09-archive-attachments.md) |

**구현:** 게시판마다 서비스를 따로 둔다. 하나로 합치지 않음 (SRP).

| 게시판 | 쓰기 조건 | 서비스 |
|---|---|---|
| 자유 | 비회원 가능 · 글 비밀번호 · 쓰기 reCAPTCHA | `FreeBoardService` |
| QnA | 로그인 · 솔루션 영역 | `QnaBoardService` |
| 자료실 | 로그인 · 첨부 필수 | `ArchiveBoardService` |

공통 화면은 훅으로만 갈린다.

```
목록  useBoardList
읽기  useBoardDetail
쓰기  useBoardCommand   ← 타입별 API를 맵으로 선택
```

회원은 JPA, 게시판은 MyBatis. Domain은 둘 다 모른다.

조회수·게스트 좋아요 쿠키 이름·만료는 컨트롤러가 Policy를 보지 않고 `BoardCookieService`만 호출한다. 상세는 [08](./08-views-likes-comments.md).

비회원 자유게시판 쓰기는 캡차·HMAC·레이트 리밋을 탄다. 계정이 없어 [이상 탐지](../security/07-anomaly-guard.md)는 타지 않는다.
