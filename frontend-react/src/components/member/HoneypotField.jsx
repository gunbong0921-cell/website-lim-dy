/**
 * Hexaq
 * 계층: Components
 * 객체: HoneypotField
 * 책임: HoneypotField 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/security/04-honeypot-field.md](../../../../docs/security/04-honeypot-field.md)
 */
export default function HoneypotField({ value, onChange }) {
  return (
    <div className="hx-hp" aria-hidden="true">
      <label htmlFor="website">Website</label>
      <input
        id="website"
        type="text"
        name="website"
        autoComplete="off"
        tabIndex={-1}
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  )
}
