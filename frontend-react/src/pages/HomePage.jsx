/**
 * Hexaq
 * 계층: Pages
 * 객체: HomePage
 * 책임: 화면 조립. fetch 금지. hooks 만 호출
 * 문서: [docs/features/overview.md](../../../docs/features/overview.md) · [docs/technical/02-technical-specification.md](../../../docs/technical/02-technical-specification.md)
 */
import Banner from '../components/common/Banner'
import CtaSection from '../components/common/CtaSection'
import FeatureGrid from '../components/common/FeatureGrid'
import Spotlight from '../components/common/Spotlight'
import pic02 from '../assets/landed/pic02.jpg'
import pic03 from '../assets/landed/pic03.jpg'
import pic04 from '../assets/landed/pic04.jpg'

export default function HomePage() {
  return (
    <>
      <Banner />

      <Spotlight id="one" nextId="two" className="spotlight style1 bottom" image={pic02} variant="b">
        <div className="container">
          <div className="row">
            <div className="col-4 col-12-medium">
              <header>
                <h2>Product, Data, Security, &amp; Settlement — Unified by Design</h2>
                <p>파편화된 외주 개발 대신, 엄격한 자체 설계 원칙 기반의 단일 오케스트레이션을 제공합니다.</p>
              </header>
            </div>
            <div className="col-4 col-12-medium">
              <p>
                웹·모바일·AI·핀테크 전 영역이 하나의 통합 운영 레이어에서 결합하여 지속 가능하고 강력한 플랫폼 생태계를
                완성합니다.
              </p>
            </div>
            <div className="col-4 col-12-medium">
              <p>
                <strong>1 Single Stack</strong>
                <br />
                단일 스택 구축 (웹·모바일·AI·핀테크를 하나로 잇는 아키텍처)
                <br />
                <strong>99.9% Target SLA</strong>
                <br />
                엔터프라이즈급 가용성 (안정적인 서비스 운영 목표)
                <br />
                <strong>100% Seamless UX</strong>
                <br />
                일관된 운영 레이어 (파편화 없는 인터페이스 설계)
              </p>
            </div>
          </div>
        </div>
      </Spotlight>

      <Spotlight
        id="two"
        nextId="three"
        className="spotlight style2 right"
        image={pic03}
        variant="c"
        to="/solutions"
        action="자세히 보기"
      >
        <header>
          <h2>Web Platform / Mobile</h2>
          <p>고성능 웹 플랫폼과 내부 운영 콘솔을 하나의 디자인 시스템으로</p>
        </header>
        <p>
          고성능 웹 플랫폼과 내부 운영 콘솔을 하나의 일관된 디자인 시스템으로 일치시킵니다. 복잡한 핵심 사용자 여정(User
          Journey)을 최상의 네이티브 퍼포먼스로 설계합니다.
        </p>
      </Spotlight>

      <Spotlight
        id="three"
        nextId="four"
        className="spotlight style3 left"
        image={pic04}
        imageClassName="bottom"
        variant="d"
        to="/solutions"
        action="자세히 보기"
      >
        <header>
          <h2>Applied AI / Fintech Infra</h2>
          <p>실용 AI와 단일 아키텍처 핀테크 인프라</p>
        </header>
        <p>
          이론적 모델을 넘어 핵심 비즈니스에 유용한 실용 AI. 검색·심사·운영 효율을 극대화합니다. 트랜잭션 결제부터 원장
          처리, 컴플라이언스까지 단일 아키텍처로 통합합니다.
        </p>
      </Spotlight>

      <FeatureGrid />
      <CtaSection />
    </>
  )
}
