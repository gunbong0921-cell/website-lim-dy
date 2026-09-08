import { Link } from 'react-router-dom'

export default function StoryCard({ tag, title, body, to }) {
  const inner = (
    <>
      <span className="hx-kicker">{tag}</span>
      <h3>{title}</h3>
      {body ? <p>{body}</p> : null}
    </>
  )

  if (to) {
    return (
      <Link to={to} className="hx-story-card">
        {inner}
      </Link>
    )
  }

  return <article className="hx-story-card">{inner}</article>
}
