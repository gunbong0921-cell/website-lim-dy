import { useCallback } from 'react'
import { boardApi } from '../services/api/boardApi'

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
  const write = useCallback((payload) => writeByType[type](payload, solution), [type, solution])
  const update = useCallback((id, payload) => updateByType[type](id, payload), [type])
  const remove = useCallback((id, password) => deleteByType[type](id, password), [type])
  return { write, update, remove }
}
