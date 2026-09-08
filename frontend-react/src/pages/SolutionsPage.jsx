import { qnaBoardPath } from '../board/qnaSolutions'
import BusinessCard from '../components/common/BusinessCard'
import MotionImage from '../components/common/MotionImage'
import PageHeader from '../components/common/PageHeader'
import pic05 from '../assets/landed/pic05.jpg'
import pic06 from '../assets/landed/pic06.jpg'
import pic07 from '../assets/landed/pic07.jpg'
import pic08 from '../assets/landed/pic08.jpg'

const items = [
  {
    slug: 'web',
    kicker: 'Web',
    title: 'Web Platform',
    body: '고성능 웹 플랫폼과 내부 운영 콘솔을 하나의 일관된 디자인 시스템으로 일치시킵니다.',
    image: pic05,
  },
  {
    slug: 'mobile',
    kicker: 'Mobile',
    title: 'Mobile',
    body: '복잡한 핵심 사용자 여정(User Journey)을 최상의 네이티브 퍼포먼스로 설계합니다.',
    image: pic06,
  },
  {
    slug: 'ai',
    kicker: 'AI',
    title: 'Applied AI',
    body: '이론적 모델을 넘어 핵심 비즈니스에 유용한 실용 AI. 검색·심사·운영 효율을 극대화합니다.',
    image: pic07,
  },
  {
    slug: 'fintech',
    kicker: 'Fintech',
    title: 'Fintech Infra',
    body: '트랜잭션 결제부터 원장 처리, 컴플라이언스까지 단일 아키텍처로 통합합니다.',
    image: pic08,
  },
]

export default function SolutionsPage() {
  return (
    <>
      <PageHeader
        eyebrow="Unified Ecosystem (통합 생태계)"
        title="하나의 아키텍처로 완성되는 지속 가능한 플랫폼"
        kicker="사업 영역이 확장되어도 제품이 파편화되지 않도록, 통일된 인터페이스와 일관된 운영 원칙을 적용합니다."
      />
      <section id="content">
        <div className="hx-grid-4">
          {items.map((item, index) => (
            <div key={item.kicker}>
              <span className="image fit hx-solution-image hx-motion">
                <MotionImage src={item.image} alt={item.title} variant={['a', 'b', 'c', 'd'][index]} />
              </span>
              <BusinessCard
                kicker={item.kicker}
                title={item.title}
                body={item.body}
                to={qnaBoardPath(item.slug)}
                action="Q&A로 질문"
              />
            </div>
          ))}
        </div>
      </section>
    </>
  )
}
