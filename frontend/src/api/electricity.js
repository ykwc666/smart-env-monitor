import requestGK from '@/utils/requestGK';
export const electricityChange = (data) => requestGK.post('/electricity/change', data)
export const electricityList = (data) => requestGK.post('/electricity/list',data)
export const getelectricity = (data) => requestGK.post('/electricity/get',data)
