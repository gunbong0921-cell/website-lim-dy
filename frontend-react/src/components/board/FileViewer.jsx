/**
 * Hexaq
 * 계층: Components
 * 객체: FileViewer
 * 책임: FileViewer 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/features/overview.md](../../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../../docs/technical/01-architecture.md)
 */
import { getFileType } from '../../utils/getFileType'

const strategies = {
  image: (file) => (
    <figure key={file.id} className="file-card">
      <img src={file.url} alt={file.originalName} />
      <figcaption>{file.originalName}</figcaption>
    </figure>
  ),
  video: (file) => (
    <figure key={file.id} className="file-card">
      <video src={file.url} controls />
      <figcaption>{file.originalName}</figcaption>
    </figure>
  ),
  audio: (file) => (
    <figure key={file.id} className="file-card">
      <audio src={file.url} controls />
      <figcaption>{file.originalName}</figcaption>
    </figure>
  ),
  download: (file) => (
    <a key={file.id} className="file-download" href={file.url} download={file.originalName}>
      {file.originalName} 다운로드
    </a>
  ),
}

export default function FileViewer({ files = [] }) {
  if (!files.length) return null
  return (
    <div className="file-viewer">
      {files.map((file) => {
        const type = getFileType(file.originalName, file.fileType)
        const render = strategies[type] || strategies.download
        return render(file)
      })}
    </div>
  )
}
