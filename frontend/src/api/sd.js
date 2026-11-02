import requestGK from '@/utils/requestGK';
export const sdChange = (data) => requestGK.post('/sd/change', data)
export const sdList = (id) => requestGK.get(`/sd/list/${id}`)
export const sdGet = (data) => requestGK.post('/sd/get',data)
