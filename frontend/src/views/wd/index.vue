<script setup>
import { ref, onMounted } from 'vue'
import { wdcahnge,wdlist,getwd } from '@/api/wd'
import { sp } from '@/stores'
import { Client } from '@stomp/stompjs' // 添加缺失的导入
import {formatTime} from '@/utils/format'
const spStore = sp()
const switchValue = ref(false)
const temperatureList = ref([])
const tempThreshold = ref(30) // 修改为更合理的阈值(摄氏度)
const wd=ref(0)
const params = ref({
  id:spStore.id,
  minTemp: 0,
  maxTemp: 100,
  timeRange: []
})

const client = new Client({
  brokerURL: 'ws://localhost:8080/ws',
  onConnect: () => {
     const subscriptionPath = `/topic/temperature/${spStore.id}`;
      client.subscribe(subscriptionPath, (message) => {
      const data = JSON.parse(message.body);
      getWdList();
      wd.value=data.wd
       console.log('温度数据：', data)
    });
  }
});
client.activate();

const getWdList = async () => {
  const res = await wdlist(spStore.id);
  temperatureList.value = res.sort((a, b) => new Date(b.time) - new Date(a.time))
}
const handleSwitchChange = async (newValue) => {

  switchValue.value = newValue;
  spStore.setWdstatus(newValue ? 1 : 0);
  await wdcahnge({  status: newValue ? 1 : 0,id: spStore.id });
}
const onSearch = async () => {
  const res = await getwd(params.value);
  temperatureList.value = res.map(item => ({
    ...item,
    time: new Date(item.time)
  })).sort((a, b) => new Date(b.time) - new Date(a.time))
}
const onReset = () => {
  params.value = {
    id:spStore.id,
    minTemp: 0,
    maxTemp: 100,
    timeRange: []
  }
}

// 修复6：添加生命周期管理
onMounted(() => {
  console.log(spStore.id);
  console.log(spStore.status);
  // 初始化开关状态
  switchValue.value = spStore.getWdstatus() === 1;
  getWdList();
})
</script>
<template>
  <div class="dashboard-container">
    <!-- 头部增加机房状态标识 -->
    <div class="header-section">
      <h2 class="dashboard-title">
        <el-icon class="title-icon"><OfficeBuilding /></el-icon>
        {{ spStore.id }} 机房智能温控系统
        <span class="status-badge" :class="spStore.status === 0 ? 'offline' : 'online'">
          {{ spStore.status === 0 ? '离线' : '在线' }}
        </span>
      </h2>
    </div>

    <!-- 温度状态卡片 -->
    <div class="status-card">
      <div class="current-temp">
        <div class="temp-value-container">
          <span class="temp-label">实时温度</span>
          <span
            :class="[
              'temp-value',
              { 'critical': wd > tempThreshold },
              { 'warning': wd > tempThreshold * 0.8 && wd <= tempThreshold }
            ]"
          >
            {{ wd || '--' }}°C
            <el-icon v-if="wd > tempThreshold" class="temp-icon"><Warning /></el-icon>
          </span>
        </div>
        <div class="threshold-info">
          安全阈值：{{ tempThreshold }}°C
          <el-tooltip content="当温度超过阈值时将触发警报">
            <el-icon class="info-icon"><QuestionFilled /></el-icon>
          </el-tooltip>
        </div>
      </div>
    </div>

    <!-- 增强型控制面板 -->
    <div class="control-panel">
      <div class="control-group">
        <label class="control-label">温度监控：</label>
        <el-switch
          v-model="switchValue"
          @change="handleSwitchChange"
          inline-prompt
          active-text="运行中"
          inactive-text="已停止"
          :disabled="spStore.status === 0"
        />
        <span v-if="spStore.status === 0" class="offline-hint">
          <el-icon><Warning /></el-icon>
          系统处于离线状态
        </span>
      </div>
    </div>

    <el-form inline>
  <!-- 温度范围应使用范围选择控件 -->
  <el-form-item label="温度范围:">
    <el-input-number
      v-model="params.minTemp"
      :min="0"
      :max="100"
      placeholder="最低温度"
      style="width: 120px"
    ></el-input-number>
    <span class="range-separator">-</span>
    <el-input-number
      v-model="params.maxTemp"
      :min="0"
      :max="100"
      placeholder="最高温度"
      style="width: 120px"
    ></el-input-number>
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

<!-- 优化后的数据表格 -->
<el-table
  :data="temperatureList"
  stripe
  highlight-current-row
  style="width: 100%"
  empty-text="暂无温度记录"
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
    label="机房编号"
    width="220"
    align="center"
  />

  <el-table-column
    prop="number"
    label="温度值(°C)"
    align="center"
    sortable
  >
    <template #default="{ row }">
      <span :class="['temp-cell', { 'cell-alert': row.number > tempThreshold }]">
        {{ row.number }}
        <el-icon v-if="row.number > tempThreshold" class="cell-icon" color="#f56c6c">
          <Top />
        </el-icon>
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
  background: #f8f9fa;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
}

.header-section {
  margin-bottom: 2rem;

  .dashboard-title {
    color: #2c3e50;
    font-size: 1.8rem;
    display: flex;
    align-items: center;
    gap: 1rem;

    .title-icon {
      font-size: 2rem;
      color: #3498db;
    }

    .status-badge {
      padding: 0.3rem 0.8rem;
      border-radius: 12px;
      font-size: 0.9rem;

      &.online {
        background: #e8f5e9;
        color: #4caf50;
      }

      &.offline {
        background: #ffebee;
        color: #f44336;
      }
    }
  }
}

.status-card {
  background: linear-gradient(135deg, #ffffff, #f8f9ff);
  padding: 1.5rem;
  border-radius: 12px;
  margin-bottom: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

  .current-temp {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .temp-value-container {
      .temp-label {
        font-size: 1rem;
        color: #7f8c8d;
        margin-right: 1rem;
      }

      .temp-value {
        font-size: 2.5rem;
        font-weight: 700;
        color: #27ae60;
        transition: all 0.3s ease;

        &.warning {
          color: #f39c12;
          animation: pulse 1.5s infinite;
        }

        &.critical {
          color: #e74c3c;
          animation: critical-pulse 0.8s infinite;
          text-shadow: 0 0 8px rgba(231, 76, 60, 0.3);
        }

        .temp-icon {
          margin-left: 0.5rem;
          animation: bounce 1s infinite;
        }
      }
    }

    .threshold-info {
      background: #f1f3f5;
      padding: 0.5rem 1rem;
      border-radius: 8px;
      font-size: 0.9rem;
      color: #495057;

      .info-icon {
        margin-left: 0.5rem;
        color: #868e96;
        cursor: help;
      }
    }
  }
}

.control-panel {
  margin-bottom: 2rem;

  .control-group {
    display: flex;
    align-items: center;
    gap: 1rem;

    .control-label {
      font-weight: 500;
      color: #2c3e50;
    }

    .offline-hint {
      color: #e67e22;
      font-size: 0.9rem;
      display: flex;
      align-items: center;
      gap: 0.3rem;
    }
  }
}

.data-table {
  :deep(.el-table) {
    border-radius: 12px;
    overflow: hidden;

    th {
      background-color: #f8f9fa !important;
      font-weight: 600;
      color: #495057;
    }

    tr:hover td {
      background-color: #f8f9ff !important;
    }
  }

  .temp-cell {
    display: inline-flex;
    align-items: center;
    gap: 0.3rem;

    &.cell-alert {
      color: #e74c3c;
      font-weight: 600;
    }

    .cell-icon {
      animation: bounce 1s infinite;
    }
  }
}

@keyframes critical-pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

@keyframes pulse {
  0% { opacity: 0.8; }
  50% { opacity: 1; }
  100% { opacity: 0.8; }
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}
</style>
