import requestGK from '@/utils/requestGK';
import request from '@/utils/request';
import requestsxt from '@/utils/requestsxt';

export const sxtChange = (data) => requestGK.post('/sxt/change',data)
export const AiSxt = (data) => request.post('/infer',data)
export const sxtvd = () => requestsxt.post('/upload')
export const loadSxt = (data) => requestGK.post('/image/load',data)
export const sxtlist = (data) => requestGK.post('sxt/list',data)
