function hex(buffer) {
  return Array.from(new Uint8Array(buffer))
    .map((byte) => byte.toString(16).padStart(2, '0'))
    .join('')
}

export async function sha256Hex(text) {
  const bytes = new TextEncoder().encode(text ?? '')
  const hash = await crypto.subtle.digest('SHA-256', bytes)
  return hex(hash)
}

export async function hmacSha256Hex(key, message) {
  const cryptoKey = await crypto.subtle.importKey(
    'raw',
    new TextEncoder().encode(key),
    { name: 'HMAC', hash: 'SHA-256' },
    false,
    ['sign']
  )
  const signature = await crypto.subtle.sign('HMAC', cryptoKey, new TextEncoder().encode(message))
  return hex(signature)
}

export function canonicalString(timestamp, method, path, bodyHash) {
  return `${timestamp}\n${method}\n${path}\n${bodyHash}`
}
