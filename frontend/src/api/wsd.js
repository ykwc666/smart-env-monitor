import requestGK from '@/utils/requestGK';
export const wsdcahnge = (data) => requestGK.post('/wsd/change', data)
export const wsdlistservice = (data) => requestGK.post('/wsd/list',data)
export const getwsd = (data) => requestGK.post('/wsd/get',data)
export const wsdadd = (data) => requestGK.post('/wsd/add',data)
export const wsdbyid = (data) => requestGK.post('/wsd/byid',data)
export const wsdupdate = (data) => requestGK.post('/wsd/update',data)
export const wsddelete = (data) => requestGK.post('/wsd/delete',data)
