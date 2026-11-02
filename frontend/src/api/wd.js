import requestGK from '@/utils/requestGK';
export const wdcahnge = (data) => requestGK.post('/wd/change', data)
export const wdlist = (id) => requestGK.get(`/wd/list/${id}`)

export const getwd = (data) => requestGK.post('/wd/get',data)
