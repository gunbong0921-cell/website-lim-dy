/**
 * Hexaq
 * 계층: Utils
 * 객체: getFileType
 * 책임: getFileType 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/overview.md](../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../docs/technical/01-architecture.md)
 */
const IMAGE = new Set(['png', 'gif', 'jpg', 'jpeg', 'webp', 'bmp'])
const VIDEO = new Set(['mp4', 'webm', 'ogg', 'avi'])
const AUDIO = new Set(['mp3', 'wav', 'aac', 'm4a'])
const NAMED = new Set(['image', 'video', 'audio', 'download'])

export function getFileType(filename = '', fileType) {
  const named = String(fileType || '').trim().toLowerCase()
  if (NAMED.has(named)) {
    return named
  }
  const ext = filename.includes('.') ? filename.split('.').pop().toLowerCase() : ''
  if (IMAGE.has(ext)) return 'image'
  if (VIDEO.has(ext)) return 'video'
  if (AUDIO.has(ext)) return 'audio'
  return 'download'
}
