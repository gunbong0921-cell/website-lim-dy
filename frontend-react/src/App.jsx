/**
 * Hexaq
 * 계층: App
 * 객체: App
 * 책임: App 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/overview.md](../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../docs/technical/01-architecture.md)
 */
import { useEffect } from 'react'
import { Navigate, Route, Routes, useParams } from 'react-router-dom'
import { qnaBoardPath, resolveQnaSolution } from './board/qnaSolutions'
import Layout from './components/common/Layout'
import { useAuth } from './hooks/useAuth'
import BoardListPage from './pages/board/BoardListPage'
import BoardViewPage from './pages/board/BoardViewPage'
import BoardWritePage from './pages/board/BoardWritePage'
import HomePage from './pages/HomePage'
import InsightsPage from './pages/InsightsPage'
import FindIdPage from './pages/member/FindIdPage'
import ForgotPasswordPage from './pages/member/ForgotPasswordPage'
import LoginPage from './pages/member/LoginPage'
import MyPage from './pages/member/MyPage'
import SignUpPage from './pages/member/SignUpPage'
import SolutionsPage from './pages/SolutionsPage'

function QnaLegacyEditRedirect() {
  const { postId } = useParams()
  if (/^\d+$/.test(postId || '')) {
    return <Navigate to={qnaBoardPath('general', postId, 'edit')} replace />
  }
  return <Navigate to={qnaBoardPath(resolveQnaSolution(postId))} replace />
}

function PrivatePage({ children }) {
  const { member, ready } = useAuth()
  if (!ready) return <p className="hx-empty">세션 확인 중...</p>
  if (!member) return <Navigate to="/login" replace />
  return children
}

export default function App() {
  const { bootstrap } = useAuth()

  useEffect(() => {
    bootstrap()
  }, [bootstrap])

  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/solutions" element={<SolutionsPage />} />
        <Route path="/insights" element={<InsightsPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignUpPage />} />
        <Route path="/forgot-id" element={<FindIdPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route
          path="/mypage"
          element={
            <PrivatePage>
              <MyPage />
            </PrivatePage>
          }
        />
        <Route path="/board/free" element={<BoardListPage type="free" />} />
        <Route path="/board/free/write" element={<BoardWritePage type="free" />} />
        <Route path="/board/free/:id" element={<BoardViewPage type="free" />} />
        <Route path="/board/free/:id/edit" element={<BoardWritePage type="free" mode="edit" />} />
        <Route path="/board/qna" element={<Navigate to="/board/qna/general" replace />} />
        <Route path="/board/qna/write" element={<Navigate to="/board/qna/general/write" replace />} />
        <Route path="/board/qna/:solution/write" element={<BoardWritePage type="qna" />} />
        <Route path="/board/qna/:postId/edit" element={<QnaLegacyEditRedirect />} />
        <Route path="/board/qna/:solution/:id/edit" element={<BoardWritePage type="qna" mode="edit" />} />
        <Route path="/board/qna/:solution/:id" element={<BoardViewPage type="qna" />} />
        <Route path="/board/qna/:solution" element={<BoardListPage type="qna" />} />
        <Route path="/board/archive" element={<BoardListPage type="archive" />} />
        <Route path="/board/archive/write" element={<BoardWritePage type="archive" />} />
        <Route path="/board/archive/:id" element={<BoardViewPage type="archive" />} />
        <Route path="/board/archive/:id/edit" element={<BoardWritePage type="archive" mode="edit" />} />
      </Route>
    </Routes>
  )
}
