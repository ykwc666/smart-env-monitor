import { defineStore } from "pinia";
import{ref} from "vue"
export const sp = defineStore("store", () => {
  const id = ref(0);
  const LEDstatus = ref([0,0,0,0]);
  const dpstatus = ref([0,0,0,0])
  const status = ref([0,0,0,0,]);
  const wsdstatus = ref([0,0,0,0]);
  const dgstatus = ref([0,0,0,0]);
  const ggstatus = ref([0,0,0,0]);
  const sdstatus = ref([0,0,0,0]);
  const wdstatus = ref([0,0,0,0]);
  const sxtstatus = ref([0,0,0,0]);
  const maxtemp = ref([0,0,0,0])
  const humi =ref([0,0,0,0])
  const maxgg=ref([0,0,0,0])
  const maxdl=ref([0,0,0,0])
  const maxdy=ref[0,0,0,0]
  const src = ref(["http://127.0.0.1:81/stream","http://127.0.0.1:81/stream","http://127.0.0.1:81/stream","http://127.0.0.1:81/stream"])

  const setLEDstatus = (newStatus) => {
    LEDstatus.value[id.value] = newStatus;
  };
  const getLEDstatus = () => {
    return LEDstatus.value[id.value];
  };
  const setLEDstatusbyid = (newStatus,id) => {
    LEDstatus.value[id] = newStatus;
  };
  const getLEDstatusbyid = (id) => {
    return LEDstatus.value[id];
  };
  const setdpstatus = (newStatus) => {
    dpstatus.value[id.value] = newStatus;
  };
  const getdpstatus = () => {
    return dpstatus.value[id.value];
  };
  const setdpstatusbyid = (newStatus,id) => {
    dpstatus.value[id] = newStatus;
  };
  const getdpstatusbyid = (id) => {
    return dpstatus.value[id];
  };
  const getsrc = () => {
    return src.value[id.value];
  };
  const setStatus = (newStatus) => {
    status.value[id.value] = newStatus;
  };
  const getStatus = () => {
    return status.value[id.value];
  };
  const setStatusbyid = (newStatus,id) => {
    status.value[id] = newStatus;
  };
  const getStatusbyid = (id) => {
    return status.value[id];
  };

  const setWsdstatus = (newStatus) => {
    wsdstatus.value[id.value] = newStatus;
  };
  const getWsdstatus = () => {
    return wsdstatus.value[id.value];
  };
  const getWsdstatusbyid = (id) => {
    return wsdstatus.value[id];
  };
  const setWsdstatusbyid = (newStatus,id) => {
    wsdstatus.value[id] = newStatus;
  };
  const setDgstatus = (newStatus) => {
    dgstatus.value[id.value] = newStatus;
  };
  const getDgstatus = () => {
    return dgstatus.value[id.value];
  };
  const setDgstatusbyid = (newStatus,id) => {
    dgstatus.value[id] = newStatus;
  };
  const getDgstatusbyid = (id) => {
    return dgstatus.value[id];
  };
  const setGgstatus = (newStatus) => {
    ggstatus.value[id.value] = newStatus;
  };
  const getGgstatus = () => {
    return ggstatus.value[id.value];
  };
  const getGgstatusbyid = (id) => {
    return ggstatus.value[id];
  };
  const setGgstatusbyid = (newStatus,id) => {
    ggstatus.value[id] = newStatus;
  };
  const setSdstatus = (newStatus) => {
    sdstatus.value[id.value] = newStatus;
  };
  const getSdstatus = () => {
    return sdstatus.value[id.value];
  };
  const setWdstatus = (newStatus) => {
    wdstatus.value[id.value] = newStatus;
  };
  const getWdstatus = () => {
    return wdstatus.value[id.value];
  };
  const setSxtstatus = (newStatus) => {
    sxtstatus.value[id.value] = newStatus;
  };
  const getSxtstatus = () => {
    return sxtstatus.value[id.value];
  };
  const getSxtstatusbyid = (id) => {
    return sxtstatus.value[id];
  };
  const setSxtstatusbyid = (newStatus,id) => {
    sxtstatus.value[id] = newStatus;
  };
  const setId = (newId) => {
    id.value = newId;
  };
  const getId = () => {
    return id.value;
  };
  const setsrcbyid = (newSrc,id) => {
    src.value[id] = newSrc;
  };
  const getsrcbyid = (id) => {
    return src.value[id];
  };
  return { status,setStatus,getStatus, setStatusbyid, getStatusbyid,
    wsdstatus,setWsdstatus,getWsdstatus,setWsdstatusbyid, getWsdstatusbyid,
    dgstatus,setDgstatus,getDgstatus, setDgstatusbyid,  getDgstatusbyid,
    ggstatus,setGgstatus,getGgstatus, setGgstatusbyid, getGgstatusbyid,
    sdstatus,setSdstatus,getSdstatus,
    wdstatus,setWdstatus,getWdstatus,
    sxtstatus,setSxtstatus,getSxtstatus, setSxtstatusbyid, getSxtstatusbyid,
    id,setId,getId,
    src,getsrc, setsrcbyid, getsrcbyid,
    LEDstatus,setLEDstatus,getLEDstatus, setLEDstatusbyid, getLEDstatusbyid,
    dpstatus,setdpstatus,getdpstatus,setdpstatusbyid,getdpstatusbyid,
   };
},{
  persist:true,
});
