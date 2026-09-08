import { Link } from 'react-router-dom'
import { scrollToId } from '../../hooks/useLandedScroll'
import MotionImage from './MotionImage'

export default function Spotlight({
  id,
  nextId,
  className,
  image,
  imageClassName = '',
  variant = 'b',
  children,
  to,
  action,
}) {
  return (
    <section id={id} className={className} style={{ backgroundImage: `url(${image})` }}>
      <span className={`image fit main hx-motion ${imageClassName}`.trim()}>
        <MotionImage src={image} variant={variant} />
      </span>
      <div className="content">
        {children}
        {to && action ? (
          <ul className="actions">
            <li>
              <Link to={to} className="button">
                {action}
              </Link>
            </li>
          </ul>
        ) : null}
      </div>
      {nextId ? (
        <a href={`#${nextId}`} className="goto-next" onClick={scrollToId}>
          Next
        </a>
      ) : null}
    </section>
  )
}
