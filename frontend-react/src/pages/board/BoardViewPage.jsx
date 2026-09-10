/**
 * Hexaq
 * 계층: Pages
 * 객체: BoardViewPage
 * 책임: 화면 조립. fetch 금지. hooks 만 호출
 * 문서: [docs/features/07-boards.md](../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../docs/features/08-views-likes-comments.md)
 */
import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { qnaBoardPath, qnaSolutionLabel, resolveQnaSolution } from '../../board/qnaSolutions'
import FileViewer from '../../components/board/FileViewer'
import PageHeader from '../../components/common/PageHeader'
import { useAuth } from '../../hooks/useAuth'
import { useBoardCommand } from '../../hooks/useBoardCommand'
import { useBoardDetail } from '../../hooks/useBoardDetail'
import { useComment } from '../../hooks/useComment'
import { useLike } from '../../hooks/useLike'

export default function BoardViewPage({ type }) {
  const { id, solution: rawSolution } = useParams()
  const solution = type === 'qna' ? resolveQnaSolution(rawSolution) : undefined
  const { member } = useAuth()
  const navigate = useNavigate()
  const { post, loading, error } = useBoardDetail(type, id, solution)
  const { remove } = useBoardCommand(type)
  const { count, message, like, setCount } = useLike(type, id, post?.likeCount || 0)
  const { comments, write, update, remove: removeComment } = useComment(type === 'qna' ? id : null)
  const [password, setPassword] = useState('')
  const [commentText, setCommentText] = useState('')
  const [editingId, setEditingId] = useState(null)
  const [editText, setEditText] = useState('')
  const [actionError, setActionError] = useState('')
  const listPath = type === 'qna' ? qnaBoardPath(solution) : `/board/${type}`
  const editPath = type === 'qna' ? qnaBoardPath(solution, id, 'edit') : `/board/${type}/${id}/edit`

  useEffect(() => {
    if (post) setCount(post.likeCount)
  }, [post, setCount])

  async function onDelete() {
    setActionError('')
    try {
      await remove(id, password)
      navigate(listPath)
    } catch (err) {
      setActionError(err.message)
    }
  }

  async function onComment(event) {
    event.preventDefault()
    await write(commentText)
    setCommentText('')
  }

  if (loading) return <p className="hx-empty">불러오는 중...</p>
  if (error) return <p className="hx-empty hx-error">{error}</p>
  if (!post) return null

  const canEdit = type === 'free' || member?.loginId === post.writer

  return (
    <>
      <PageHeader
        kicker={
          type === 'qna'
            ? `Q&A · ${qnaSolutionLabel(solution)}`
            : type === 'free'
              ? 'Community · 비회원'
              : type === 'archive'
                ? 'Community · 회원'
                : 'Community'
        }
        title={post.title}
      />
      <article className="hx-article">
        <p>
          <Link to={listPath}>← 목록</Link>
        </p>
        <p className="hx-article-meta">
          {post.writer} · 조회 {post.visitCount} · 좋아요 {count}
        </p>
        <div className="hx-article-body">{post.content}</div>
        {type === 'archive' && <FileViewer files={post.files} />}
        <div className="hx-article-actions">
          {member || type === 'free' ? (
            <button type="button" className="button primary" onClick={() => like()}>
              좋아요 {count}
            </button>
          ) : (
            <Link className="button primary" to="/login">
              로그인 후 좋아요 {count}
            </Link>
          )}
          {canEdit && (
            <>
              {type === 'free' && (
                <input
                  type="password"
                  placeholder="글 비밀번호"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  style={{ maxWidth: 200 }}
                />
              )}
              <Link className="button" to={editPath}>
                수정
              </Link>
              <button type="button" className="button" onClick={onDelete}>
                삭제
              </button>
            </>
          )}
        </div>
        {message && <p className="hx-notice">{message}</p>}
        {actionError && <p className="hx-error">{actionError}</p>}

        {type === 'qna' && (
          <section>
            <header>
              <h3>댓글</h3>
            </header>
            {comments.map((item) => (
              <div key={item.id} className="hx-comment">
                <strong>{item.writer}</strong>
                {editingId === item.id ? (
                  <>
                    <textarea value={editText} onChange={(e) => setEditText(e.target.value)} />
                    <button
                      type="button"
                      className="button primary"
                      onClick={async () => {
                        await update(item.id, editText)
                        setEditingId(null)
                      }}
                    >
                      저장
                    </button>
                  </>
                ) : (
                  <p>{item.content}</p>
                )}
                {member?.loginId === item.writer && (
                  <ul className="actions">
                    <li>
                      <button
                        type="button"
                        className="button small"
                        onClick={() => {
                          setEditingId(item.id)
                          setEditText(item.content)
                        }}
                      >
                        수정
                      </button>
                    </li>
                    <li>
                      <button type="button" className="button small" onClick={() => removeComment(item.id)}>
                        삭제
                      </button>
                    </li>
                  </ul>
                )}
              </div>
            ))}
            {member ? (
              <form onSubmit={onComment}>
                <div className="hx-field">
                  <textarea value={commentText} onChange={(e) => setCommentText(e.target.value)} required />
                </div>
                <ul className="actions">
                  <li>
                    <input type="submit" className="primary" value="댓글 등록" />
                  </li>
                </ul>
              </form>
            ) : (
              <p className="hx-muted">
                댓글은 로그인 후 작성할 수 있습니다. <Link to="/login">로그인</Link>
              </p>
            )}
          </section>
        )}
      </article>
    </>
  )
}
