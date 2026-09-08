const IMAGE = new Set(['png', 'gif', 'jpg', 'jpeg', 'webp', 'bmp'])
const VIDEO = new Set(['mp4', 'webm', 'ogg', 'avi'])
const AUDIO = new Set(['mp3', 'wav', 'aac', 'm4a'])

export function getFileType(filename = '') {
  const ext = filename.includes('.') ? filename.split('.').pop().toLowerCase() : ''
  if (IMAGE.has(ext)) return 'image'
  if (VIDEO.has(ext)) return 'video'
  if (AUDIO.has(ext)) return 'audio'
  return 'download'
}
