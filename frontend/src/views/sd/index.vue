<script setup>
import { onMounted, ref} from 'vue'
import { sp } from '@/stores'
import GaugeChart from '@/views/sd/components/GaugeChart.vue';
import { sdChange,sdList,sdGet} from '@/api/sd.js';
import { Client } from '@stomp/stompjs';

const spStore = sp()
const switchValue = ref(false)
const params = ref({
  id:spStore.id,
  min: 0,
  max: 100,
  timeRange: []
});
const humidity = ref(0);
const humidityList = ref([]);
const client = new Client({
  brokerURL: 'ws://localhost:8080/ws',
  onConnect: () => {
    const subscriptionPath = `/topic/humidity/${spStore.id}`;
    client.subscribe(subscriptionPath, (message) => {
      const data = JSON.parse(message.body);
      humidity.value = data.sd;
      getSdList();
    });
  }
});
client.activate();
const formatTime = (row) => {
  const date = new Date(row.time)
  return date.toLocaleString('zh-CN', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).replace(/\//g, '-')
}
const getSdList = async () => {
   const res = await sdList(spStore.id)
  console.log(res);
  humidityList.value = res;
  console.log(humidityList.value);
}
const onReset = () => {
  params.value = {
    id:spStore.id,
    min: 0,
    max: 100,
    timeRange: []
  };
}
const onSearch = async () => {
  const res = await sdGet(params.value)
  console.log(res);
  humidityList.value = res.map(item => ({
    ...item,
    time: new Date(item.time)
  })).sort((a, b) => new Date(b.time) - new Date(a.time))
}
const handleSwitchChange = async (value) => {
  switchValue.value = value;
  spStore.setSdstatus(value ? 1 : 0);
  await sdChange({  status: value ? 1 : 0,id: spStore.id});
}
onMounted(() => {
  // 初始化开关状态
 switchValue.value = spStore.getSdstatus() === 1;
 getSdList();
})
</script>
<template>
  <div class="dashboard-container">
    <!-- 头部增加环境状态指示 -->
    <div class="status-bar" :class="{ 'status-warning': humidity > 70 }">
      <el-icon class="status-icon"><humidity-icon /></el-icon>
      <span>{{ spStore.id }}号机房环境监控</span>
      <div class="status-light" :class="{ 'light-active': switchValue }"></div>
    </div>

    <!-- 控制面板增加投影和悬浮效果 -->
    <div class="control-panel">
      <div class="switch-item">
        <label class="control-label">实时数据采集</label>
        <el-switch
          v-model="switchValue"
          @change="handleSwitchChange"
          inline-prompt
          active-text="运行中"
          inactive-text="已暂停"
          class="custom-switch"
        />
      </div>

      <!-- 实时数据卡片增加动态效果 -->
      <transition name="humidity-card">
        <div v-if="switchValue" class="data-card">
          <div class="card-header">
            <el-icon><timer-icon /></el-icon>
            <span>当前湿度</span>
          </div>
          <div class="card-body">
            <span class="humidity-value">{{ humidity }}<sup>%</sup></span>
            <div class="trend-indicator"
                 :class="{ 'rising': humidity > previousHumidity }">
              <el-icon v-if="humidity > previousHumidity"><top /></el-icon>
              <el-icon v-else><bottom /></el-icon>
            </div>
          </div>
        </div>
      </transition>
    </div>

    <!-- 图表容器增加加载状态 -->
    <div class="chart-container">
      <GaugeChart
        v-if="switchValue"
        :humidity="humidity"
        class="gauge-chart"
      />
      <el-skeleton v-else :rows="3" animated />
    </div>
<el-form inline>
     <el-form-item label="湿度范围:">
    <el-input-number
      v-model="params.min"
      :min="0"
      :max="100"
      placeholder="最低湿度"
      style="width: 120px"
    ></el-input-number>
       <span class="unit">%</span>
    <span class="range-separator">——</span>
    <el-input-number
      v-model="params.max"
      :min="0"
      :max="100"
      placeholder="最高湿度"
      style="width: 120px"
    ></el-input-number>
    <span class="unit">%</span>
  </el-form-item>

  <!-- 时间范围应使用日期选择器 -->
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
    <el-button type="primary" @click="onSearch">搜索</el-button>
    <el-button @click="onReset">重置</el-button>
  </el-form-item>
</el-form>

    <!-- 数据表格优化 -->
    <el-table
      :data="humidityList"
      stripe
      style="width: 100%"
      class="data-table"
      row-class-name="table-row"
    >
      <el-table-column
        prop="time"
        label="记录时间"
        width="200"
        :formatter="formatTime"
      />
      <el-table-column prop="id" label="机房编号" width="220" />
      <el-table-column prop="number" label="湿度值(%)">
        <template #default="{ row }">
          <span :class="{ 'high-value': row.number > 70 }">
            {{ row.number }}
          </span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped lang="scss">
.dashboard-container {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 2rem;
  background: linear-gradient(145deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.12);
}

.status-bar {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.5rem;
  background: #2c3e50;
  color: white;
  border-radius: 8px;
  margin-bottom: 2rem;
  transition: all 0.3s ease;

  .status-icon {
    font-size: 1.5rem;
    color: #409EFF;
  }

  .status-light {
    width: 12px;
    height: 12px;
    background: #ff4757;
    border-radius: 50%;
    margin-left: auto;
    transition: all 0.3s ease;

    &.light-active {
      background: #2ed573;
      box-shadow: 0 0 12px #2ed573;
    }
  }
}

.control-panel {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  margin-bottom: 2rem;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);

  .switch-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 1.5rem;

    .control-label {
      font-weight: 600;
      color: #2c3e50;
      font-size: 1.1rem;
    }
  }
}

.data-card {
  background: linear-gradient(135deg, #409EFF 0%, #58a6ff 100%);
  color: white;
  padding: 1.5rem;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  .card-header {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    opacity: 0.9;
    margin-bottom: 1rem;
  }

  .card-body {
    display: flex;
    align-items: baseline;
    gap: 1rem;

    .humidity-value {
      font-size: 2.5rem;
      font-weight: 700;
      letter-spacing: -0.5px;

      sup {
        font-size: 1.2rem;
        opacity: 0.8;
      }
    }

    .trend-indicator {
      padding: 0.3rem 0.5rem;
      border-radius: 6px;
      background: rgba(255,255,255,0.2);

      &.rising {
        color: #ff6b6b;
      }
    }
  }
}

.data-table {
  margin-top: 2rem;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);

  :deep(.el-table__header) {
    background: #f8f9fa;
    th {
      font-weight: 600;
      color: #2c3e50;
    }
  }

  .high-value {
    color: #ff4757;
    font-weight: 500;
  }
}

/* 动画效果 */
.humidity-card-enter-active,
.humidity-card-leave-active {
  transition: all 0.5s ease;
}

.humidity-card-enter-from,
.humidity-card-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

@keyframes trend-pulse {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}

.trend-indicator {
  animation: trend-pulse 2s infinite;
}
</style>
