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
