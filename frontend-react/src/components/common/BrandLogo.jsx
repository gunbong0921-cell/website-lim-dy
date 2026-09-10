/**
 * Hexaq
 * 계층: Components
 * 객체: BrandLogo
 * 책임: BrandLogo 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/overview.md](../../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../../docs/technical/01-architecture.md)
 */
import logo from '../../assets/brand/hexaq-logo.png'

export default function BrandLogo() {
  return <img className="brand-logo" src={logo} alt="HEXAQ" />
}
