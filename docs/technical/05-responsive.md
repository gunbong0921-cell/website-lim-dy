# 05. 반응형 (폰 · 태블릿)

| 항목 | 내용 |
|---|---|
| 목적 | 1180px 이하에서 햄버거 내비와 홈 레이아웃을 데스크톱 Header와 분리한다 |
| 적용일 | 2026-09-10 |
| 관련 | [기술종합](./overview.md), [아키텍처](./01-architecture.md), [기술사양서](./02-technical-specification.md) |

Landed 원본은 햄버거를 **736px 미만**만 연다. iPad 세로(768–1024)는 데스크톱 메뉴가 줄어 쓸 수 없다. Hexaq는 **1180px 이하**를 컴팩트 뷰로 본다.

---

## 1. 객체

| 계층 | 객체 | 책임 |
|---|---|---|
| Hooks | `useCompactNav` | `(max-width: 1180px)` 여부. 마크업 없음 |
| Components | `Header` | 데스크톱 `#header` + 로고. 컴팩트에서 마운트하지 않음 |
| Components | `CompactNav` | `#titleBar` 햄버거 + `#navPanel`. 로고 없음 |
| Components | `Layout` | `useCompactNav`로 Header / CompactNav 조립 |
| 스타일 | `hexaq-compact.css` | 컴팩트 오버레이. Landed `main.css`는 수정하지 않음 |

페이지는 이 객체를 직접 부르지 않는다. `Layout`만 조립한다. `fetch` 없음.

---

## 2. 브레이크포인트

| 구간 | px | 내비 | 홈 |
|---|---|---|---|
| 폰 | ≤736 | `CompactNav` | 스포트라이트 스택. Landed 736 규칙을 Hexaq가 덮음 |
| 좁은 태블릿 | 737–900 | `CompactNav` | 오버레이 유지, 카피 축소 |
| 태블릿 | 737–1180 | `CompactNav` | 배너 flex, 스포트라이트 오버레이. `100vh` :before 스페이서 끔 |
| 좁은 데스크톱 | 1181–1440 | `Header` flex | Landed + `hexaq.css` |
| 데스크톱 | ≥1181 | `Header` | Landed |

JS 상수 `COMPACT_NAV_MAX_PX`와 CSS `max-width: 1180px`를 같이 맞춘다. 숫자만 바꾸지 않는다.

---

## 3. 하지 않는 것

- 고정 `#titleBar`에 HEXAQ 로고를 넣지 않는다. 스크롤 시 본문을 덮는다
- `Header`와 `CompactNav`를 한 파일에 두지 않는다
- Landed `theme/landed/css/main.css`를 직접 고치지 않는다. 덮는 파일은 `hexaq-compact.css`
- 컴팩트에서 `Header`를 `display:none`만 하고 DOM에 로고를 남기지 않는다. `Layout`이 마운트하지 않는다

확인: 폰에서 홈을 아래로 내린다. 따라오는 것은 48px 햄버거 바뿐이다.
