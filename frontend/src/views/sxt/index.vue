<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { sp } from '@/stores'
import { sxtChange, AiSxt,loadSxt,sxtlist } from '@/api/sxt';
import { formatTime } from '@/utils/format'

const spStore = sp()
const switchValue = ref(false)
const cameraActive = ref(false)
const AiImage = ref('')
const videoFrameRef = ref(null) // 用于模板ref绑定
const valueAi = ref(false)
const intervalId = ref(null);
const listSxt = ref([])
const params =ref({
   timeRange: []
})
const person=ref(0)

const isGettingAi = ref(false); // 防止并发请求

const handAi = async (value) => {
  valueAi.value = value;

  if (value) {
    if (!intervalId.value) {
      intervalId.value = setInterval(async () => {
        if (isGettingAi.value) return; // 防止重复执行
        isGettingAi.value = true;
        try {
          await getAiSxt();
        } catch (err) {

        } finally {
          isGettingAi.value = false;
        }
      }, 500); // 建议改为 500ms 或 1000ms，避免太频繁
    }
  } else {
    if (intervalId.value) {
      clearInterval(intervalId.value);
      intervalId.value = null;
    }
    AiImage.value = '';
  }
};
// 切换开关状态
const handleSwitchChange = async (value) => {
  switchValue.value = value;
  cameraActive.value = value;
  spStore.setSxtstatus(value ? 1 : 0);
  await sxtChange({ status: value ? 1 : 0, id: spStore.id });
  // 注意：我们不再使用WebSocket，所以不需要连接和断开
  if (!value) {
    // 关闭摄像头时，可以做一些清理工作
    // 比如：重置AI图像等
    AiImage.value = '';
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
    person.value=res.data.inference_results.filter(item  => item.label==="person").length
    AiImage.value = res.data.processed_image;
    let processedBase64 = res.data.processed_image;
if (processedBase64.includes(',')) {
  processedBase64 = processedBase64.split(',')[1];
}
    await loadSxt({
      image:processedBase64,
      room:spStore.id,
      person:person.value
    })
  } catch (error) {
    console.error('捕获图像失败', error);
  }
};
const getsxtlist = async() =>{
  const res =await sxtlist({
    ...params.value,
    id:spStore.id
  })
  console.log(res)
  listSxt.value=res.data.data
}

// 初始化
onMounted(() => {
  // 根据存储状态初始化开关
  getsxtlist()
  switchValue.value = spStore.getSxtstatus() === 1;
  cameraActive.value = spStore.getSxtstatus() === 1;
});
onUnmounted(() => {
  // 销毁WebSocket连接
 if (intervalId.value) {
    clearInterval(intervalId.value);
    intervalId.value = null;
  }
});
</script>

<template>
   <div v-if="spStore.getStatus(spStore.$id) === 0" class="loading-overlay">
  <span>总开关关闭中</span>
</div>
  <div class="monitor-container" v-else>
    <h3>{{spStore.id}}机房视频监控系统</h3>

    <div class="control-panel">
      <div class="switch-item">
        <label class="switch-label">视频监控开关：</label>
        <el-switch
          v-model="switchValue"
          @change="handleSwitchChange"
          active-text="开启"
          inactive-text="关闭"
        />
      </div>

      <div class="status-info" v-if="cameraActive">
        <span>视频流已开启</span>
      </div>
    </div>

    <div class="video-container" v-show="cameraActive">
      <div class="video-placeholder" v-if="!cameraActive">
        <div class="loading-spinner"></div>
        <p>正在连接摄像头...</p>
      </div>

      <img
        ref="videoFrameRef"
        crossOrigin="anonymous"
        :src="spStore.getsrc()"
        alt="实时监控画面"
        class="video-frame"
      />
    </div>
    <br>
    <div class="control-panel">
      <div class="switch-item">
        <label class="switch-label">AI识别：</label>
        <el-button @click="getAiSxt">AI识别</el-button>
         <el-switch v-model="valueAi"  @change="handAi" />
         当前人数：{{ person }}
      </div>
    </div>
    <div class="video-container" v-show="AiImage">
      <img
        v-if="AiImage"
        :src="AiImage"
        alt="AI识别结果"
        class="video-frame"
      />
    </div>
     <el-form inline>
  <el-form-item label="时间范围:">
    <el-date-picker
      v-model="params.timeRange"
      type="datetimerange"
      range-separator="至"
      start-placeholder="开始时间"
      end-placeholder="结束时间"
      value-format="YYYY-MM-DD HH:mm:ss"
      style="width: 360px"
    />
  </el-form-item>

  <el-form-item>
    <el-button type="primary" @click="onSearch" style="width: 60px;">搜索</el-button>
    <el-button @click="onReset" style="width: 60px;">重置</el-button>
  </el-form-item>
</el-form>

<!-- 优化后的数据表格 -->
<el-table
  :data="listSxt"
  stripe
  highlight-current-row
  style="width: 100%"
  empty-text="暂无摄像头记录"
  class="data-table"
  v-loading="loading"
>
  <el-table-column
    prop="time"
    label="记录时间"
    width="220"
  >
    <template #default="{ row }">
      {{ formatTime(row.time) }}
    </template>
  </el-table-column>

  <el-table-column
    prop="id"
    label="灯杆编号"
    width="220"
    align="center"
  />
    <el-table-column
    prop="person"
    label="识别人数"
    width="220"
    align="center"
  />

  <el-table-column
    prop="address"
    label="图像地址"
    align="center"
    sortable
  >
  </el-table-column>
</el-table>
  </div>
</template>

<style scoped lang="scss">
.monitor-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 25px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', Arial, sans-serif;
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

.status-info {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;

  span {
    padding: 6px 12px;
    background: #f5f7fa;
    border-radius: 4px;
    font-weight: 500;
  }
}

.video-container {
  position: relative;
  width: 100%;
  height: 70vh;
  min-height: 500px;
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

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.video-frame {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: opacity 0.3s;
}

h3 {
  margin-bottom: 25px;
  color: #2c3e50;
  font-size: 24px;
  text-align: center;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}
</style>
