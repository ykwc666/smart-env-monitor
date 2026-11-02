import requestGK from '@/utils/requestGK';
export const kgcahnge = (data) => requestGK.post('/kg/change', data)
export const kgList = (id) => requestGK.get(`/kg/list/${id}`)
export const kgget = (data) => requestGK.post('/kg/get',data)
