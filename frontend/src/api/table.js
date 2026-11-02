import requestGK from '@/utils/requestGK';
export const searchtable = (data) => requestGK.post('/table/search', data)
