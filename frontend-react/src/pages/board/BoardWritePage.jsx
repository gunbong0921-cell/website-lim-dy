/**
 * Hexaq
 * 계층: Pages
 * 객체: BoardWritePage
 * 책임: 화면 조립. fetch 금지. hooks 만 호출
 * 문서: [docs/features/07-boards.md](../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../docs/features/08-views-likes-comments.md)
 */
import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { qnaBoardPath, qnaSolutionLabel, resolveQnaSolution } from '../../board/qnaSolutions'
import PageHeader from '../../components/common/PageHeader'
import { useAuth } from '../../hooks/useAuth'
import { useBoardCommand } from '../../hooks/useBoardCommand'
import { useBoardDetail } from '../../hooks/useBoardDetail'

export default function BoardWritePage({ type, mode }) {
  const { id, solution: rawSolution } = useParams()
  const solution = type === 'qna' ? resolveQnaSolution(rawSolution) : undefined
  const { member } = useAuth()
  const navigate = useNavigate()
  const isEdit = mode === 'edit'
  const { post } = useBoardDetail(isEdit ? type : null, isEdit ? id : null, solution)
  const { write, update } = useBoardCommand(type, solution)
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [writer, setWriter] = useState('')
  const [password, setPassword] = useState('')
  const [files, setFiles] = useState([])
  const [error, setError] = useState('')
  const listPath = type === 'qna' ? qnaBoardPath(solution) : `/board/${type}`
  const viewPath = type === 'qna' ? qnaBoardPath(solution, id) : `/board/${type}/${id}`

  useEffect(() => {
    if (post) {
      setTitle(post.title)
      setContent(post.content)
      setWriter(post.writer)
    }
  }, [post])

  async function onSubmit(event) {
    event.preventDefault()
    setError('')
    try {
      if (type === 'archive') {
        const formData = new FormData()
        formData.append('title', title)
        formData.append('content', content)
        ;[...files].forEach((file) => formData.append('files', file))
        if (isEdit) await update(id, formData)
        else await write(formData)
      } else {
        const payload = type === 'free' ? { title, content, writer: writer || 'guest', password } : { title, content }
        if (isEdit) await update(id, payload)
        else await write(payload)
      }
      navigate(isEdit ? viewPath : listPath)
    } catch (err) {
      setError(err.message)
    }
  }

  if (type !== 'free' && !member) {
    return <p className="hx-empty">회원 전용 게시판입니다.</p>
  }

  return (
    <>
      <PageHeader
        kicker={type === 'free' ? 'Community · 비회원' : type === 'archive' ? 'Community · 회원' : 'Community'}
        title={type === 'qna' ? `${isEdit ? '글 수정' : '글쓰기'} · ${qnaSolutionLabel(solution)}` : isEdit ? '글 수정' : '글쓰기'}
      />
      <section id="content">
        <div className="hx-panel hx-panel-wide">
          <form onSubmit={onSubmit}>
            {type === 'free' && (
              <>
                <div className="hx-field">
                  <label htmlFor="writer">작성자</label>
                  <input
                    id="writer"
                    type="text"
                    value={writer}
                    onChange={(e) => setWriter(e.target.value)}
                    required={!isEdit}
                  />
                </div>
                <div className="hx-field">
                  <label htmlFor="password">글 비밀번호</label>
                  <input
                    id="password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>
              </>
            )}
            <div className="hx-field">
              <label htmlFor="title">제목</label>
              <input id="title" type="text" value={title} onChange={(e) => setTitle(e.target.value)} required />
            </div>
            <div className="hx-field">
              <label htmlFor="content">내용</label>
              <textarea id="content" rows={10} value={content} onChange={(e) => setContent(e.target.value)} required />
            </div>
            {type === 'archive' && (
              <div className="hx-field">
                <label htmlFor="files">첨부파일 {isEdit ? '(선택 시 교체)' : '(필수)'}</label>
                <input
                  id="files"
                  type="file"
                  multiple
                  onChange={(e) => setFiles(e.target.files)}
                  required={!isEdit}
                />
              </div>
            )}
            {error && <p className="hx-error">{error}</p>}
            <ul className="actions">
              <li>
                <input type="submit" className="primary" value="저장" />
              </li>
            </ul>
          </form>
        </div>
      </section>
    </>
  )
}
