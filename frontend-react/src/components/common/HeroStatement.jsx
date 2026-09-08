import { Link } from 'react-router-dom'

export default function HeroStatement() {
  return (
    <section className="hx-hero">
      <div className="hx-wrap">
        <span className="hx-kicker">Hexaq</span>
        <h1 className="hx-display">
          하나의 스택으로
          <br />
          연결합니다
        </h1>
        <p className="hx-lead">
          웹, 모바일, AI, 핀테크를 따로 납품하지 않습니다. 복잡한 기술을 운영 가능한 제품으로 묶어, 신뢰와 확장을 같은
          레이어에서 맞춥니다.
        </p>
        <div className="hx-hero-actions">
          <Link to="/solutions" className="hx-btn hx-btn-primary">
            솔루션 보기
          </Link>
          <Link to="/board/qna" className="hx-btn hx-btn-ghost">
            문의 · Q&amp;A
          </Link>
        </div>
      </div>
    </section>
  )
}
