/**
 * Hexaq
 * 계층: Components
 * 객체: SignUpVerifyTabs
 * 책임: SignUpVerifyTabs 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/overview.md](../../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../../docs/technical/01-architecture.md)
 */
export default function SignUpVerifyTabs({ channel, onChange }) {
  return (
    <div className="hx-member-tabs" role="tablist" aria-label="본인 인증 수단">
      <button
        type="button"
        role="tab"
        aria-selected={channel === 'EMAIL'}
        className={channel === 'EMAIL' ? 'is-active' : ''}
        onClick={() => onChange('EMAIL')}
      >
        이메일 인증
      </button>
      <button
        type="button"
        role="tab"
        aria-selected={channel === 'PHONE'}
        className={channel === 'PHONE' ? 'is-active' : ''}
        onClick={() => onChange('PHONE')}
      >
        휴대폰 인증
      </button>
    </div>
  )
}
