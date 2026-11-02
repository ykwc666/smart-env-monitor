<script setup>
import { onMounted, ref,onUnmounted,watch } from 'vue'
import { kgcahnge } from '@/api/kg'
import { sp } from '@/stores'
import { wsdcahnge } from '@/api/wsd'
import { ggcahnge } from '@/api/gg'
import { electricityChange } from '@/api/electricity'
import { Client } from '@stomp/stompjs';
import { sxtChange,AiSxt,loadSxt } from '@/api/sxt'
import {LEdchange} from "@/api/deng"
const spStore = sp()
const client = ref(null);
const valuewsd = ref(false)
const valuegg = ref(false)
const valuedg= ref(false)
const valuesxt = ref(false)
const switchValue = ref(false)
const cameraActive = ref(false)
const AiImage = ref('')
const videoFrameRef = ref(null) // 用于模板ref绑定
const valueAi = ref(false)
const props = defineProps({
  id: Number
});
const personnumber = ref(0)
const valuedp = ref(false)
const valueled = ref(false)
// 可从 props、API 或 store 中动态加载
const thresholds = {
  temperature: 30,     // 温度超过30℃变红
  humidity: 80,        // 湿度超过80%变红
  current: 500,         // 电流超过10A变红
  voltage: 300,        // 电压超过250V变红
  light: 1000          // 光照超过1000 lux（示例）
};

const sensorData = ref({
  temperature: "--",
  humidity: "--",
  light: "--",
  current: "--",
  voltage: "--"
})
const subscriptions = {
  wsd: {
    path: `/topic/environment/${props.id}`,
    handler: (message) => {
      const data = JSON.parse(message.body);
      sensorData.value.temperature = data.temp;
      sensorData.value.humidity = data.humi;
    }
  },
  light: {
    path: `/topic/light/${props.id}`,
    handler: (message) => {
      const data = JSON.parse(message.body);
      sensorData.value.light = data.gg;
    }
  },
  electricity: {
    path: `/topic/electricity/${props.id}`,
    handler: (message) => {
      const data = JSON.parse(message.body);
      sensorData.value.current = data.current*1000;
      sensorData.value.voltage = data.voltage;
    }
  }
};
const activeSubscriptions = new Map(); // 存储活跃的订阅对象
const initWebSocket = () => {
  client.value = new Client({
    brokerURL: 'ws://localhost:8080/ws',
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  });
  client.value.onConnect = () => {
    // 初始根据开关状态激活订阅
    updateSubscriptions();
  };
  client.value.activate();
};
// 更新订阅状态的核心方法
const updateSubscriptions = () => {
  // 先取消所有已存在的订阅
  activeSubscriptions.forEach(subscription => {
    subscription.unsubscribe();
  });
  activeSubscriptions.clear();
  // 根据当前开关状态重新订阅
  if (valuewsd.value) {
    addSubscription('wsd');
  }
  if (valuegg.value) {
    addSubscription('light');
  }
  if(valuedg.value){
    addSubscription('electricity');
  }
  // 可扩展其他开关状态判断...
};
// 添加单个订阅
const addSubscription = (key) => {
  if (subscriptions[key] && client.value.connected) {
    const subscription = client.value.subscribe(
      subscriptions[key].path,
      subscriptions[key].handler
    );
    activeSubscriptions.set(key, subscription);
  }
};
const getAiSxt = async () => {
  if (!videoFrameRef.value) {
    console.error('视频帧元素未找到');
    return;
  }

  const img = videoFrameRef.value;
  if (img.naturalWidth === 0 || img.naturalHeight === 0) {
    console.error('图像未加载完成');
    return;
  }

  const canvas = document.createElement('canvas');
  canvas.width = img.naturalWidth;
  canvas.height = img.naturalHeight;
  const ctx = canvas.getContext('2d');

  try {
    ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
    const dataURL = canvas.toDataURL('image/jpeg');
    const res = await AiSxt({ image: dataURL });
    console.log(res);
    // personnumber.value=res.inference_results.forEach(label,number,item =>item.label==="person"?nummber+1:0)
   AiImage.value = res.data.processed_image;
    let processedBase64 = res.data.processed_image;
if (processedBase64.includes(',')) {
  processedBase64 = processedBase64.split(',')[1];
}
    await loadSxt({
      image:processedBase64,
      room:props.id
    })
  } catch (error) {
    console.error('捕获图像失败', error);
  }
};

const intervalId = ref(null);

const isGettingAi = ref(false); // 防止并发请求

const handleSwitchChangeAi = (value) => {
  valueAi.value = value;

  if (value) {
    if (!intervalId.value) {
      intervalId.value = setInterval(async () => {
        if (isGettingAi.value) return;
        isGettingAi.value = true;
        await getAiSxt().catch(err => console.error('AI 识别失败:', err));
        isGettingAi.value = false;
      }, 1000);
    }
  } else {
    if (intervalId.value) {
      clearInterval(intervalId.value);
      intervalId.value = null;
    }
    AiImage.value = '';
  }
};
// 在组件挂载时初始化
onMounted(() => {
  // 初始化开关状态
  switchValue.value = spStore.getStatusbyid(props.id) === 1;
  valuewsd.value=spStore.getWsdstatusbyid(props.id) === 1;
  valuegg.value=spStore.getGgstatusbyid(props.id) === 1;
  valuedg.value=spStore.getDgstatusbyid(props.id) === 1;
  valuesxt.value=spStore.getSxtstatusbyid(props.id) === 1;
  cameraActive.value = spStore.getSxtstatusbyid(props.id) === 1;
  valuedp.value = spStore.getdpstatusbyid(props.id) === 1;
  valueled.value = spStore.getLEDstatusbyid(props.id) === 1;
  initWebSocket();
})
onUnmounted(() => {
  // 取消所有订阅
  activeSubscriptions.forEach(sub => sub.unsubscribe());
  activeSubscriptions.clear();

  // 关闭WebSocket连接
  if (client.value) {
    client.value.deactivate();
  }
  if (intervalId.value) {
    clearInterval(intervalId.value);
    intervalId.value = null;
  }
});
const handleSwitchChangewsd = async (value) => {
  valuewsd.value = value;
  spStore.setWsdstatusbyid(value ? 1 : 0, props.id)
  console.log(spStore.wsdstatus)
  // 立即更新订阅状态
  if (value) {
    addSubscription('wsd');
  } else {
    // 移除订阅
    sensorData.value.temperature="--";
    sensorData.value.humidity="--";
    const sub = activeSubscriptions.get('wsd');
    if (sub) {
      sub.unsubscribe();
      activeSubscriptions.delete('wsd');
    }
  }
  await wsdcahnge({ status: value ? 1 : 0, id: props.id });
};
const handleSwitchChangegg = async (value) => {
  valuegg.value = value;
  spStore.setGgstatusbyid(value?1:0, props.id);
  if (value) {
    addSubscription('light');
  } else {
    // 移除订阅
    sensorData.value.light="--";
    const sub = activeSubscriptions.get('light');
    if (sub) {
      sub.unsubscribe();
      activeSubscriptions.delete('light');
    }
  }
  await ggcahnge({
    status: value ? 1 : 0,
    id: props.id
  })

}
const handleSwitchChangedg = async (value) => {
  valuedg.value = value;
  spStore.setDgstatusbyid(value?1:0, props.id);
  if(value){
    addSubscription('electricity');
  }else{
    sensorData.value.current="--";
    sensorData.value.voltage="--";
    const sub = activeSubscriptions.get('electricity');
    if (sub) {
      sub.unsubscribe();
      activeSubscriptions.delete('electricity');
    }
  }
  await electricityChange({
    status: value ? 1 : 0,
    id: props.id
  })

}
const handleSwitchChangesxt = async (value) => {
  valuesxt.value = value;
  cameraActive.value = value;
  spStore.setSxtstatusbyid=(value?1:0, props.id);
  await sxtChange({
    status: value ? 1 : 0,
    id: props.id
  })
 if (!value) {
    // 关闭摄像头时，可以做一些清理工作
    // 比如：重置AI图像等
    AiImage.value = '';
  }
}
// const handleSwitchChangeAi
const handleSwitchChangedp = async (value) => {
  valuedp.value = value;
  spStore.setdpstatusbyid(value?1:0, props.id);
  await dpChange({
    status: value ? 1 : 0,
    id: props.id
  })
}
const handleSwitchChangeLED = async (value) => {
  valueled.value = value;
  spStore.setLEDstatusbyid(value?1:0, props.id);
  await LEdchange({
    status: value ? 1 : 0,
    id: props.id
  })
}


// 监听store状态变化
watch(() => spStore.status.value?.[props.id] ?? null, (newStatus) => {
  switchValue.value = newStatus === 1;
});

const change = async (value) => {
  // 更新开关状态
  spStore.setStatusbyid(value ? 1 : 0, props.id)
   if (!value) {
    switchValue.value = false;
  valuewsd.value=false;
  valuegg.value=false;
  valuedg.value=false;
  valuesxt.value=false;
  cameraActive.value = false;
  valuedp.value = false;
  valueled.value = false;
  spStore.setWsdstatusbyid(0, props.id)
  spStore.setGgstatusbyid(0, props.id)
  spStore.setDgstatusbyid(0, props.id)
  spStore.setSxtstatusbyid(0, props.id)
  spStore.setdpstatusbyid(0, props.id)
  spStore.setLEDstatusbyid(0, props.id)
  valueAi.value = false;
  AiImage.value = '';
  }

  // 发送API请求
  await kgcahnge({ status: value ? 1 : 0, id: props.id })
}
const getTempColor = () => {
  return sensorData.value.temperature > thresholds.temperature ? '#ff4d4f' : '#333';
};

const getHumidityColor = () => {
  return sensorData.value.humidity > thresholds.humidity ? '#ff4d4f' : '#333';
};

const getCurrentColor = () => {
  return sensorData.value.current > thresholds.current ? '#ff4d4f' : '#333';
};

const getVoltageColor = () => {
  return sensorData.value.voltage > thresholds.voltage ? '#ff4d4f' : '#333';
};

const getLightColor = () => {
  return sensorData.value.light > thresholds.light ? '#ff4d4f' : '#333';
};
</script>

<template>
   <div class="switch-container">
    <h3>{{props.id}}号灯杆</h3>

    <!-- 开关控制 -->
    <div class="switch-item">
      <label class="switch-label">{{ props.id }}号灯杆设备开关：</label>
      <el-switch v-model="switchValue" @change="change(switchValue)" />
      <span class="switch-status">{{ switchValue ? '开启' : '关闭' }}</span>
    </div>

    <!-- 传感器数据展示 -->
    <div class="sensor-data">
       <!-- <div class="sensor-item">
        <span class="sensor-label">灯光控制：</span>
         <el-switch v-model="valuedp"  @change="handleSwitchChangedp" />
      </div> -->
        <div class="sensor-item">
        <span class="sensor-label">LED灯：</span>
         <el-switch v-model="valueled"  @change="handleSwitchChangeLED" />
      </div>
 <!-- 温湿度 -->
<div class="sensor-item">
  <span class="sensor-label">温湿度：</span>
  <el-switch v-model="valuewsd" @change="handleSwitchChangewsd"/>
  <div style="flex: 1; margin-left: 10px;">
    <span :style="{ color: getTempColor() }">
      温度: {{ sensorData.temperature }}℃
    </span>
    |
    <span :style="{ color: getHumidityColor() }">
      湿度: {{ sensorData.humidity }}%
    </span>
  </div>
</div>

<!-- 光感 -->
<div class="sensor-item">
  <span class="sensor-label">光感：</span>
  <el-switch v-model="valuegg" @change="handleSwitchChangegg" />
  <span
    :style="{ color: getLightColor() }"
    class="sensor-value">
    光照强度: {{ sensorData.light }} lux
  </span>
</div>

<!-- 电感 -->
<div class="sensor-item">
  <span class="sensor-label">电感：</span>
  <el-switch v-model="valuedg" @change="handleSwitchChangedg" />
  <div style="flex: 1; margin-left: 10px;">
    <span :style="{ color: getCurrentColor() }">
      电流: {{ sensorData.current }}mA
    </span>
    |
    <span :style="{ color: getVoltageColor() }">
      电压: {{ sensorData.voltage }}V
    </span>
  </div>
</div>
      <div class="sensor-item">
        <span class="sensor-label">摄像头：</span>
       <el-switch v-model="valuesxt"  @change="handleSwitchChangesxt" /><br>
      <div class="video-container" v-show="cameraActive">
      <div class="video-placeholder" v-if="!cameraActive">
        <div class="loading-spinner"></div>
        <p>正在连接摄像头...</p>
      </div>
  <img
        ref="videoFrameRef"
        crossOrigin="anonymous"
        :src="spStore.getsrcbyid(props.id)"
        alt="实时监控画面"
        class="video-frame"
      />
    </div>
      </div>
        <div class="sensor-item">
        <span class="sensor-label">AI识别：</span>
       <el-switch v-model="valueAi"  @change="handleSwitchChangeAi" />
    <div class="video-container" v-show="AiImage">
      <img
        v-if="AiImage"
        :src="AiImage"
        alt="AI识别结果"
        class="video-frame"
      />
    </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.switch-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', Arial, sans-serif;
}
.video-container {
  position: relative;
  width: 100%;
  height: 50vh;
  min-height: 300px;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.video-placeholder {
  text-align: center;
  color: #aaa;

  p {
    margin-top: 15px;
    font-size: 18px;
  }
}
.loading-spinner {
  width: 50px;
  height: 50px;
  margin: 0 auto;
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  border-top: 4px solid #409EFF;
  animation: spin 1s linear infinite;
}
.video-frame {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: opacity 0.3s;
}
.control-panel {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.switch-item {
  display: flex;
  align-items: center;
}

.switch-label {
  margin-right: 15px;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.sensor-data {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.sensor-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;

  &:last-child {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
  }
}

.sensor-label {
  font-weight: bold;
  width: 100px;
}

.sensor-value {
  flex: 1;
  font-weight: 500;
}

.switch-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.switch-label {
  margin-right: 10px;
}

.switch-status {
  margin-left: 15px;
  font-weight: bold;
  color: #67c23a;
}
</style>
