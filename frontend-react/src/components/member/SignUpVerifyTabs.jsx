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
