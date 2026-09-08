export default function PageHeader({ eyebrow, kicker, title }) {
  return (
    <header className="major">
      {eyebrow ? <p className="hx-kicker">{eyebrow}</p> : null}
      <h2>{title}</h2>
      {kicker ? <p>{kicker}</p> : null}
    </header>
  )
}
