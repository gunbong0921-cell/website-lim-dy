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
