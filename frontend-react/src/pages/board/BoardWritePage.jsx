import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { qnaBoardPath, qnaSolutionLabel, resolveQnaSolution } from '../../board/qnaSolutions'
import PageHeader from '../../components/common/PageHeader'
import { useAuth } from '../../hooks/useAuth'
import { useBoardDetail } from '../../hooks/useBoardDetail'
import { boardApi } from '../../services/api/boardApi'

export default function BoardWritePage({ type, mode }) {
  const { id, solution: rawSolution } = useParams()
  const solution = type === 'qna' ? resolveQnaSolution(rawSolution) : undefined
  const { member } = useAuth()
  const navigate = useNavigate()
  const isEdit = mode === 'edit'
  const { post } = useBoardDetail(isEdit ? type : null, isEdit ? id : null, solution)
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
      if (type === 'free') {
        const payload = { title, content, writer: writer || 'guest', password }
        if (isEdit) await boardApi.updateFree(id, payload)
        else await boardApi.writeFree(payload)
      } else if (type === 'qna') {
        const payload = { title, content, solution }
        if (isEdit) await boardApi.updateQna(id, payload)
        else await boardApi.writeQna(payload)
      } else {
        const formData = new FormData()
        formData.append('title', title)
        formData.append('content', content)
        ;[...files].forEach((file) => formData.append('files', file))
        if (isEdit) await boardApi.updateArchive(id, formData)
        else await boardApi.writeArchive(formData)
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
        kicker="Community"
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
