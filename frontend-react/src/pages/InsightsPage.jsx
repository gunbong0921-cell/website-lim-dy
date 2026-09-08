import { Link } from 'react-router-dom'
import MotionImage from '../components/common/MotionImage'
import PageHeader from '../components/common/PageHeader'
import StoryCard from '../components/common/StoryCard'
import pic05 from '../assets/landed/pic05.jpg'

const notes = [
  {
    tag: 'Architecture',
    title: '단일 스택(Unified Stack)을 유지하기 위한 백엔드 설계 원칙',
  },
  {
    tag: 'Performance',
    title: '고트래픽 및 핀테크 환경에서의 안정성 확보 방식',
  },
  {
    tag: 'AI Integration',
    title: '실무 워크플로우에 최적화된 Applied AI 모델 채택 가이드',
  },
]

export default function InsightsPage() {
  return (
    <>
      <PageHeader
        eyebrow="Tech Notes(기술노트)"
        title="서비스 나열을 넘어, Hexaq가 기술을 선택하고 제품을 완성하는 기준"
      />
      <section id="content">
        <span className="image fit hx-motion">
          <MotionImage src={pic05} variant="c" />
        </span>
        <div className="hx-grid-3" style={{ marginTop: '2em' }}>
          {notes.map((note) => (
            <StoryCard key={note.title} tag={note.tag} title={note.title} body={note.body} />
          ))}
        </div>
        <p className="hx-muted" style={{ textAlign: 'center', marginTop: '2.5em' }}>
          커뮤니티에서 이어서 토론하기 → <Link to="/board/qna">Q&amp;A 게시판</Link>
        </p>
      </section>
    </>
  )
}
