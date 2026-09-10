# 09. 자료실 첨부

| 항목 | 내용 |
|---|---|
| 관련 | [기능종합](./overview.md), [07 게시판](./07-boards.md) |

**구현:** 타입이 늘어도 switch를 고치지 않는다. 전략 맵.

```
업로드  ArchiveBoardService
        → FileStorage.store
        → FileTypeClassifier.classify   (image / video / audio / download)

화면    getFileType.js
        → FileViewer.strategies[type]
```

다운로드: `GET /api/files/{id}` → `FileDownloadService`

multipart 자료실 업로드는 HMAC 대상이 아니다. [HMAC](../security/01-hmac-request-signing.md).
