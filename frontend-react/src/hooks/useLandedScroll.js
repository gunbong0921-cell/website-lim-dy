import { useEffect } from 'react'

export function useLandedScroll(pathname) {
  useEffect(() => {
    const timeout = setTimeout(() => {
      document.body.classList.remove('is-preload')
    }, 0)

    if ('ontouchstart' in window || navigator.maxTouchPoints > 0) {
      document.body.classList.add('is-touch')
    }

    const mq = window.matchMedia('(min-width: 981px)')
    const animated = () => document.querySelectorAll('.spotlight, .wrapper.fade-up, .wrapper.fade')

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) entry.target.classList.remove('inactive')
        })
      },
      { threshold: 0.12 },
    )

    function sync() {
      observer.disconnect()
      animated().forEach((el) => {
        if (mq.matches) {
          el.classList.add('inactive')
          observer.observe(el)
        } else {
          el.classList.remove('inactive')
        }
      })
    }

    sync()
    mq.addEventListener('change', sync)

    return () => {
      clearTimeout(timeout)
      observer.disconnect()
      mq.removeEventListener('change', sync)
    }
  }, [pathname])
}

export function scrollToId(event) {
  const href = event.currentTarget.getAttribute('href')
  if (!href || !href.startsWith('#')) return
  const target = document.querySelector(href)
  if (!target) return
  event.preventDefault()
  target.scrollIntoView({ behavior: 'smooth' })
}
