/**
 * Hexaq
 * 계층: Components
 * 객체: SignUpTypeTabs
 * 책임: SignUpTypeTabs 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/overview.md](../../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../../docs/technical/01-architecture.md)
 */
export default function SignUpTypeTabs({ memberType, onChange }) {
  return (
    <div className="hx-member-tabs" role="tablist" aria-label="가입 유형">
      <button
        type="button"
        role="tab"
        aria-selected={memberType === 'INDIVIDUAL'}
        className={memberType === 'INDIVIDUAL' ? 'is-active' : ''}
        onClick={() => onChange('INDIVIDUAL')}
      >
        개인 회원
      </button>
      <button
        type="button"
        role="tab"
        aria-selected={memberType === 'CORPORATE'}
        className={memberType === 'CORPORATE' ? 'is-active' : ''}
        onClick={() => onChange('CORPORATE')}
      >
        기업/법인 회원
      </button>
    </div>
  )
}
