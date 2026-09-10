/**
 * Hexaq
 * 계층: Components
 * 객체: MotionImage
 * 책임: MotionImage 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/overview.md](../../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../../docs/technical/01-architecture.md)
 */
export default function MotionImage({ src, alt = '', variant = 'a' }) {
  return (
    <>
      <img className={`hx-motion-base hx-motion-${variant}`} src={src} alt={alt} />
      <img className={`hx-motion-drift hx-motion-${variant}`} src={src} alt="" aria-hidden="true" />
      <img className={`hx-motion-lights hx-motion-${variant}`} src={src} alt="" aria-hidden="true" />
      <img className={`hx-motion-scan hx-motion-${variant}`} src={src} alt="" aria-hidden="true" />
    </>
  )
}
