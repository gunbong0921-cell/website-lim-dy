/**
 * Hexaq
 * 계층: Pages
 * 객체: BoardListPage
 * 책임: 화면 조립. fetch 금지. hooks 만 호출
 * 문서: [docs/features/07-boards.md](../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../docs/features/08-views-likes-comments.md)
 */
import { Link, NavLink, Navigate, useLocation, useNavigate, useParams } from 'react-router-dom'
import { QNA_SOLUTIONS, qnaBoardPath, qnaSolutionLabel, resolveQnaSolution } from '../../board/qnaSolutions'
import PageHeader from '../../components/common/PageHeader'
import Pagination from '../../components/common/Pagination'
import { useAuth } from '../../hooks/useAuth'
import { useBoardList } from '../../hooks/useBoardList'

const meta = {
  free: { title: '자유게시판 (비회원)', guest: true, writeHint: '비회원제 게시판입니다. 로그인 없이 글을 쓸 수 있습니다.' },
  qna: { title: 'Q&A', guest: false, writeHint: '회원만 질문과 답변을 남길 수 있습니다.' },
  archive: { title: '자료실 (회원)', guest: false, writeHint: '회원제 게시판입니다. 로그인 후 첨부파일을 공유할 수 있습니다.' },
}

export default function BoardListPage({ type }) {
  const { solution: rawSolution } = useParams()
  if (type === 'qna' && /^\d+$/.test(rawSolution || '')) {
    return <Navigate to={qnaBoardPath('general', rawSolution)} replace />
  }
  const solution = type === 'qna' ? resolveQnaSolution(rawSolution) : undefined
  if (type === 'qna' && rawSolution && rawSolution !== solution) {
    return <Navigate to={qnaBoardPath(solution)} replace />
  }
  return <BoardListBody type={type} solution={solution} />
}

function BoardListBody({ type, solution }) {
  const info = meta[type]
  const { member } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const { data, loading, error, page, setPage, keyword, setKeyword, searchType, setSearchType, reload } =
    useBoardList(type, solution)

  function onSearch(event) {
    event.preventDefault()
    reload(1)
    setPage(1)
  }

  function goWrite() {
    if (!info.guest && !member) {
      navigate('/login')
      return
    }
    navigate(type === 'qna' ? qnaBoardPath(solution, 'write') : `/board/${type}/write`)
  }

  const rows = data?.content || []
  const title = type === 'qna' ? `Q&A · ${qnaSolutionLabel(solution)}` : info.title
  const qnaActive = location.pathname.startsWith('/board/qna')

  return (
    <>
      <PageHeader kicker={type === 'free' ? 'Community · 비회원' : type === 'archive' ? 'Community · 회원' : 'Community'} title={title} />
      <div className="hx-board">
        <nav className="hx-tabs">
          <NavLink to="/board/free">자유 (비회원)</NavLink>
          <NavLink to="/board/qna/general" className={() => (qnaActive ? 'active' : undefined)}>
            Q&amp;A
          </NavLink>
          <NavLink to="/board/archive">자료실 (회원)</NavLink>
        </nav>
        {type === 'qna' && (
          <nav className="hx-tabs hx-tabs-sub">
            {QNA_SOLUTIONS.map((item) => (
              <NavLink key={item.slug} to={qnaBoardPath(item.slug)}>
                {item.shortLabel}
              </NavLink>
            ))}
          </nav>
        )}
        <div className="hx-board-toolbar">
          <p className="hx-muted">{info.writeHint}</p>
          <ul className="actions">
            <li>
              <button type="button" className="button primary" onClick={goWrite}>
                글쓰기
              </button>
            </li>
          </ul>
        </div>
        <form className="hx-search" onSubmit={onSearch}>
          <select value={searchType} onChange={(e) => setSearchType(e.target.value)}>
            <option value="title">제목</option>
            <option value="content">내용</option>
            <option value="writer">작성자</option>
          </select>
          <input type="text" value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="검색어" />
          <input type="submit" value="검색" />
        </form>
        {loading && <p className="hx-muted">불러오는 중...</p>}
        {error && <p className="hx-error">{error}</p>}
        {!loading && !rows.length ? (
          <p className="hx-empty">아직 글이 없습니다.</p>
        ) : (
          <ul className="hx-board-list">
            {rows.map((row) => (
              <li key={row.id}>
                <Link
                  className="hx-board-row"
                  to={type === 'qna' ? qnaBoardPath(solution, row.id) : `/board/${type}/${row.id}`}
                >
                  <span className="hx-board-num">{row.id}</span>
                  <span className="hx-board-title">{row.title}</span>
                  <span className="hx-board-meta">
                    {row.writer} · 조회 {row.visitCount} · 좋아요 {row.likeCount}
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        )}
        <Pagination page={page} totalPages={data?.totalPages || 1} onChange={(n) => setPage(n)} />
      </div>
    </>
  )
}
