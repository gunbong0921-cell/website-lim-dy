/**
 * Hexaq
 * 계층: Components
 * 객체: SignUpTerms
 * 책임: SignUpTerms 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/overview.md](../../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../../docs/technical/01-architecture.md)
 */
export default function SignUpTerms({ memberType, terms, onChange }) {
  const corporate = memberType === 'CORPORATE'
  return (
    <div className="hx-form-section">
      <h3>약관 동의 및 인증</h3>
      <div className="hx-check-list">
        <div className="hx-check">
          <input
            type="checkbox"
            id="termsService"
            checked={terms.termsService}
            onChange={(e) => onChange('termsService', e.target.checked)}
          />
          <label htmlFor="termsService">[필수] 이용약관 동의</label>
        </div>
        <div className="hx-check">
          <input
            type="checkbox"
            id="termsPrivacy"
            checked={terms.termsPrivacy}
            onChange={(e) => onChange('termsPrivacy', e.target.checked)}
          />
          <label htmlFor="termsPrivacy">[필수] 개인정보 수집 및 이용 동의</label>
        </div>
        <div className="hx-check">
          <input
            type="checkbox"
            id="termsMarketing"
            checked={terms.termsMarketing}
            onChange={(e) => onChange('termsMarketing', e.target.checked)}
          />
          <label htmlFor="termsMarketing">[선택] 마케팅 정보 수집 및 수신 동의 (이메일/SMS)</label>
        </div>
        {corporate && (
          <div className="hx-check">
            <input
              type="checkbox"
              id="termsCorporate"
              checked={terms.termsCorporate}
              onChange={(e) => onChange('termsCorporate', e.target.checked)}
            />
            <label htmlFor="termsCorporate">[필수] 법인 대표자 또는 위임받은 대리인 가입 확인 동의</label>
          </div>
        )}
      </div>
    </div>
  )
}
