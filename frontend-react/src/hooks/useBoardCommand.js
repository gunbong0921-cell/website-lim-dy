/**
 * Hexaq
 * 계층: Hooks
 * 객체: useBoardCommand
 * 책임: Application/Presentation DTO. Domain 엔티티를 API 에 직접 노출하지 않음
 * 문서: [docs/features/overview.md](../../../docs/features/overview.md) · [docs/technical/01-architecture.md](../../../docs/technical/01-architecture.md)
 */
import { useCallback } from 'react'
import { boardApi } from '../services/api/boardApi'
import { useRecaptcha } from './useRecaptcha'

const writeByType = {
  free: (payload) => boardApi.writeFree(payload),
  qna: (payload, solution) => boardApi.writeQna({ ...payload, solution }),
  archive: (payload) => boardApi.writeArchive(payload),
}

const updateByType = {
  free: (id, payload) => boardApi.updateFree(id, payload),
  qna: (id, payload) => boardApi.updateQna(id, payload),
  archive: (id, payload) => boardApi.updateArchive(id, payload),
}

const deleteByType = {
  free: (id, password) => boardApi.deleteFree(id, password),
  qna: (id) => boardApi.deleteQna(id),
  archive: (id) => boardApi.deleteArchive(id),
}

export function useBoardCommand(type, solution) {
  const { execute } = useRecaptcha()
  const write = useCallback(async (payload) => {
    if (type === 'free') {
      const recaptchaToken = await execute('board_write_free')
      return writeByType.free({ ...payload, recaptchaToken })
    }
    return writeByType[type](payload, solution)
  }, [execute, type, solution])
  const update = useCallback((id, payload) => updateByType[type](id, payload), [type])
  const remove = useCallback((id, password) => deleteByType[type](id, password), [type])
  return { write, update, remove }
}
