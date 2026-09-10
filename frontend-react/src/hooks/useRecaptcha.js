import { useCallback } from 'react'
import { publicApi } from '../services/api/publicApi'

let configPromise
let scriptPromise

function loadConfig() {
  if (!configPromise) {
    configPromise = publicApi.config().then((res) => res.data || {})
  }
  return configPromise
}

function readyGrecaptcha() {
  return new Promise((resolve) => {
    window.grecaptcha.ready(() => resolve(window.grecaptcha))
  })
}

function loadScript(siteKey) {
  if (window.grecaptcha) {
    return readyGrecaptcha()
  }
  if (!scriptPromise) {
    scriptPromise = new Promise((resolve, reject) => {
      const script = document.createElement('script')
      script.src = `https://www.google.com/recaptcha/api.js?render=${encodeURIComponent(siteKey)}`
      script.async = true
      script.dataset.recaptcha = 'v3'
      script.onload = () => window.grecaptcha.ready(() => resolve(window.grecaptcha))
      script.onerror = () => reject(new Error('로봇 확인에 실패했습니다. 다시 시도해 주세요.'))
      document.head.appendChild(script)
    })
  }
  return scriptPromise
}

function publicTunnelHost() {
  return typeof window !== 'undefined'
    && String(window.location.hostname || '').toLowerCase().endsWith('.trycloudflare.com')
}

export function useRecaptcha() {
  const execute = useCallback(async (action) => {
    if (publicTunnelHost()) {
      return ''
    }
    const config = await loadConfig()
    if (!config.recaptchaEnabled || !config.recaptchaSiteKey) {
      return ''
    }
    const grecaptcha = await loadScript(config.recaptchaSiteKey)
    try {
      return await grecaptcha.execute(config.recaptchaSiteKey, { action })
    } catch (err) {
      const text = String(err?.message || err || '')
      if (text.includes('Invalid site key') || text.includes('not loaded in api.js')) {
        throw new Error('이 주소는 reCAPTCHA에 등록되지 않았습니다. localhost:8282에서 열거나 Google reCAPTCHA 콘솔에 현재 도메인을 추가하세요.')
      }
      throw new Error('로봇 확인에 실패했습니다. 다시 시도해 주세요.')
    }
  }, [])

  return { execute }
}
