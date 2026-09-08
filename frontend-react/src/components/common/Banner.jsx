import pic01 from '../../assets/landed/pic01.jpg'
import { scrollToId } from '../../hooks/useLandedScroll'
import MotionImage from './MotionImage'

export default function Banner() {
  return (
    <section id="banner">
      <div className="content">
        <header>
          <h2>Unified Stack Architecture</h2>
          <p>
            영역별로 분절된 개발이 아닌, 통일된 기술 스택 기반의 통합 프로덕트를 구축합니다.
            <br />
            복잡한 백엔드와 인프라를 유기적으로 묶어, 높은 시스템 안정성과 서비스 확장성을 동시에 실현합니다.
          </p>
        </header>
        <span className="image hx-motion">
          <MotionImage src={pic01} alt="Hexaq 디지털 코어" variant="a" />
        </span>
      </div>
      <a href="#one" className="goto-next" onClick={scrollToId}>
        Next
      </a>
    </section>
  )
}
