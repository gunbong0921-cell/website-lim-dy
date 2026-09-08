import { Link } from 'react-router-dom'

const features = [
  {
    icon: 'fa-globe',
    title: 'Web Platform',
    body: '고성능 웹 플랫폼과 내부 운영 콘솔을 하나의 일관된 디자인 시스템으로 일치시킵니다.',
  },
  {
    icon: 'fa-mobile-alt',
    title: 'Mobile',
    body: '복잡한 핵심 사용자 여정(User Journey)을 최상의 네이티브 퍼포먼스로 설계합니다.',
  },
  {
    icon: 'fa-flask',
    title: 'Applied AI',
    body: '이론적 모델을 넘어 핵심 비즈니스에 유용한 실용 AI. 검색·심사·운영 효율을 극대화합니다.',
  },
  {
    icon: 'fa-lock',
    title: 'Fintech Infra',
    body: '트랜잭션 결제부터 원장 처리, 컴플라이언스까지 단일 아키텍처로 통합합니다.',
  },
  {
    icon: 'fa-file',
    title: 'Operational Assets',
    body: '프롬프트, 벤치마크 평가 세트, 도메인 이벤트 파이프라인을 지속 가능한 엔지니어링 자산으로 구축합니다.',
  },
  {
    icon: 'fa-paper-plane',
    title: 'Delivery',
    body: '일회성 구축을 넘어 지속 운용 가능한 프로덕트를 원활한(Seamless) 인터페이스로 전달합니다.',
  },
]

export default function FeatureGrid() {
  return (
    <section id="four" className="wrapper style1 special fade-up">
      <div className="container">
        <header className="major">
          <h2>Multi-Domain, Single Operational Layer</h2>
          <p>사업 영역이 확장되어도 제품이 파편화되지 않도록, 통일된 인터페이스와 일관된 운영 원칙을 적용합니다.</p>
        </header>
        <div className="box alt">
          <div className="row gtr-uniform">
            {features.map((item) => (
              <section key={item.title} className="col-4 col-6-medium col-12-xsmall">
                <span className={`icon solid alt major ${item.icon}`} />
                <h3>{item.title}</h3>
                <p>{item.body}</p>
              </section>
            ))}
          </div>
        </div>
        <footer className="major">
          <ul className="actions special">
            <li>
              <Link to="/solutions" className="button">
                솔루션 자세히 보기
              </Link>
            </li>
          </ul>
        </footer>
      </div>
    </section>
  )
}
