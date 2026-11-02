import requestGK from '@/utils/requestGK';
export const LEdchange = (data) => requestGK.post('/deng/LED/change', data)
export const dpchange = (data) => requestGK.post('/deng/dp/change', data)
