import kakaoIcon from '../../assets/social/kakao.png'
import githubIcon from '../../assets/social/github.png'

function GoogleMark() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path
        fill="#4285F4"
        d="M21.6 12.2c0-.8-.1-1.6-.2-2.3H12v4.4h5.4c-.2 1.3-.9 2.4-2 3.1v2.6h3.2c1.9-1.8 3-4.4 3-7.8z"
      />
      <path
        fill="#34A853"
        d="M12 22c2.7 0 5-.9 6.6-2.4l-3.2-2.6c-.9.6-2 1-3.4 1-2.6 0-4.8-1.8-5.6-4.1H3.1v2.6C4.8 19.7 8.1 22 12 22z"
      />
      <path
        fill="#FBBC05"
        d="M6.4 13.9c-.2-.6-.3-1.2-.3-1.9s.1-1.3.3-1.9V7.5H3.1C2.4 8.9 2 10.4 2 12s.4 3.1 1.1 4.5l3.3-2.6z"
      />
      <path
        fill="#EA4335"
        d="M12 5.9c1.5 0 2.8.5 3.8 1.5l2.9-2.9C16.9 2.9 14.7 2 12 2 8.1 2 4.8 4.3 3.1 7.5l3.3 2.6C7.2 7.7 9.4 5.9 12 5.9z"
      />
    </svg>
  )
}

const PROVIDERS = [
  { id: 'google', label: 'Google', href: '/oauth2/authorization/google', mark: <GoogleMark /> },
  { id: 'kakao', label: '카카오톡', href: '/oauth2/authorization/kakao', src: kakaoIcon },
  { id: 'github', label: 'GitHub', href: '/oauth2/authorization/github', src: githubIcon },
]

export default function SocialLoginButtons() {
  return (
    <div className="hx-social">
      <p className="hx-social-title">소셜로 계속하기</p>
      <div className="hx-social-icons" role="group" aria-label="소셜로 계속하기">
        {PROVIDERS.map((provider) => (
          <a
            key={provider.id}
            className={`hx-social-icon is-${provider.id}`}
            href={provider.href}
            aria-label={provider.label}
          >
            {provider.mark || <img src={provider.src} alt="" />}
          </a>
        ))}
      </div>
    </div>
  )
}
