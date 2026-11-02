import requestGK from '@/utils/requestGK';
export const ggcahnge = (data) => requestGK.post('/gg/change', data)
export const ggList = (data) => requestGK.post('/gg/list',data)
export const ggGet = (data) => requestGK.post('/gg/get',data)
