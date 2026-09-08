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
