import { Link } from 'react-router-dom'

export default function BusinessCard({ kicker, title, body, to, action }) {
  return (
    <Link to={to} className="hx-business-card">
      <span className="hx-kicker">{kicker}</span>
      <h3>{title}</h3>
      <p>{body}</p>
      {action ? <span className="hx-card-action">{action}</span> : null}
    </Link>
  )
}
