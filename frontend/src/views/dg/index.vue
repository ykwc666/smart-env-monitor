<script  setup>
import { onMounted, ref} from 'vue'
import { sp } from '@/stores'
import currentChart from '@/views/dg/components/currentChart.vue'
import voltageChart from '@/views/dg/components/voltageChart.vue'
import { electricityChange,electricityList,getelectricity} from '@/api/electricity.js';
import { Client } from '@stomp/stompjs';
import {reactive} from 'vue'
import { formatTime } from '@/utils/format'
import GuanganChartCopy from './components/GuanganChart copy.vue';
import GuanganChartCopy2 from './components/GuanganChart copy 2.vue';

const spStore = sp()
const switchValue = ref(false)
const electricity = reactive({
  current: 0,
  voltage: 0
})
const electricitylist = ref([])
const params = ref({
  id: spStore.id,
  mincurrent: 0,
  maxcurrent: 1000,
  minvoltage: 0,
  maxvoltage: 1000,
  timeRange: []
})
const maxcurrent = ref(800)
const maxvoltage = ref(500)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dlhis=ref([])
const dyhis=ref([])

const client = new Client({
  brokerURL: 'ws://localhost:8080/ws',
   onConnect: () => {
    // 使用模板字符串动态拼接 ID
    const subscriptionPath = `/topic/electricity/${spStore.id}`; // 从 store 获取 ID

    client.subscribe(subscriptionPath, (message) => {
      const data = JSON.parse(message.body);
      electricity.current = data.current*1000;
      electricity.voltage = data.voltage;
      dlhis.value.push({
          value:data.current*1000,
         time: new Date().toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
          })
      })
      dyhis.value.push({
          value:data.voltage,
         time: new Date().toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
          })
      })
      console.log('Received data:', electricity);
      GetEleList()
    });
  }
});
client.activate();
const handleSwitchChange = async (value) => {
  switchValue.value = value;
  spStore.getDgstatus(value ? 1 : 0)
  await electricityChange({ status: value ? 1 : 0,id: spStore.id });
}
const GetEleList = async () => {
  const res = await electricityList({
    id: spStore.id,
    page: currentPage.value,
    pageSize: pageSize.value
  });
  electricitylist.value =  res.records
  total.value = res.total
}

onMounted(() => {
  switchValue.value = spStore.getDgstatus() === 1;
  GetEleList()
})
const onSearch = async () => {
  const res = await getelectricity({
    ...params.value,
    page: currentPage.value,
    pageSize: pageSize.value
  })
  electricitylist.value =  res.data.data.records
  total.value = res.data.data.total
}
const handlePageChange = (newPage) => {
  currentPage.value = newPage
  onSearch()
}

// 修改重置功能（重置分页参数）
const onReset = () => {
  params.value = {
    id: spStore.id,
    mincurrent: 0,
    maxcurrent: 1000,
    minvoltage: 0,
    maxvoltage: 230,
    timeRange: []
  };
  currentPage.value = 1  // 重置到第一页
  onSearch()
}
</script>

<template>
    <div v-if="spStore.getStatus(spStore.$id) === 0" class="loading-overlay">
  <span>总开关关闭中</span>
</div>
<div id="app" v-else>
    <div class="dashboard">
      <div class="header">
        <h1>{{spStore.id}}机房电感监控系统</h1>
        <p>实时监测电感参数变化</p>
        <div class="status">
          <div class="status-dot" :class="switchValue && spStore.status ? 'active' : 'inactive'"></div>
          <span v-if="switchValue && spStore.status">实时数据更新中</span>
          <span v-else>实时数据已停止</span>
        </div>
      </div>

      <div class="content">
        <div class="control-panel">
          <div class="control-label">电感数据获取：</div>
          <el-switch
            v-if="spStore.status !== 0"
            v-model="switchValue"
            @change="handleSwitchChange"
            active-text="开启"
            inactive-text="关闭"
            active-color="#409EFF"
            inactive-color="#dcdfe6"
            style="--el-switch-on-color: #409EFF;"
          />
        </div>
        <GuanganChartCopy
         v-if="switchValue"
      :light-data="dlhis"
      :current-light="electricity.current " />
      <GuanganChartCopy2
       v-if="switchValue"
      :light-data="dyhis"
      :current-light="electricity.voltage " />

        <div v-if="switchValue" class="charts-container">
          <div class="chart-card">
            <div class="chart-title">电压监测</div>
              <voltage-chart :voltage="electricity.voltage" />
          <div class="data-display">
  当前电压:
  <span :class="{ 'warning': electricity.voltage > maxvoltage }">
    {{ electricity.voltage }}
  </span>
  <span class="unit">V</span>
</div>
          </div>

          <div class="chart-card">
            <div class="chart-title">电流监测</div>
            <div class="chart-container current-chart-container">  <!-- 添加容器 -->
            <current-chart :current="electricity.current" />
            </div>
            <div class="data-display">
            当前电流:
            <span :class="{ 'warning': electricity.current > maxcurrent }">
            {{ electricity.current }}
            </span>
            <span class="unit">mA</span>
</div>
          </div>
        </div>

        <div v-else class="info">
          <div class="info-title">系统提示</div>
          <div class="info-content">
            电感监控系统当前已关闭。请开启系统以开始监测电压和电流数据。
            系统开启后将实时显示电感参数变化并提供可视化图表。
          </div>
        </div>
      </div>
    <el-form inline>
  <el-form-item label="电流范围:">
    <el-input-number
      v-model="params.mincurrent"
      :min="0"
      :max="200"
      placeholder="最低电流"
      style="width: 120px"
    ></el-input-number>
    <span class="range-separator">-</span>
    <el-input-number
      v-model="params.maxcurrent"
      :min="0"
      :max="2000"
      placeholder="最高电流"
      style="width: 120px"
    ></el-input-number>
  </el-form-item>

    <el-form-item label="电压范围:">
    <el-input-number
      v-model="params.minvoltage"
      :min="0"
      :max="100"
      placeholder="最低电压"
      style="width: 120px"
    ></el-input-number>
    <span class="range-separator">-</span>
    <el-input-number
      v-model="params.maxvoltage"
      :min="0"
      :max="100"
      placeholder="最高电压"
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
    <el-button type="primary" @click="onSearch" style="width: 60px;">搜索</el-button>
    <el-button @click="onReset" style="width: 60px;">重置</el-button>
  </el-form-item>
</el-form>

<!-- 优化后的数据表格 -->
<el-table
  :data="electricitylist"
  stripe
  highlight-current-row
  style="width: 100%"
  empty-text="暂无电流电压记录"
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
    prop="current"
    label="电流值(mA)"
    align="center"
    sortable
  >
    <template #default="{ row }">
      <span :class="['temp-cell', { 'cell-alert': row.current > maxcurrent }]">
        {{ row.current }}
        <el-icon v-if="row.current > maxcurrent" class="cell-icon" color="#f56c6c">
          <Top />
        </el-icon>
      </span>
    </template>
  </el-table-column>
  <el-table-column
    prop="voltage"
    label="电压值(V)"
    align="center"
    sortable
  >
    <template #default="{ row }">
      <span :class="['temp-cell', { 'cell-alert': row.voltage > maxvoltage }]">
        {{ row.voltage }}
        <el-icon v-if="row.voltage > maxvoltage" class="cell-icon" color="#f56c6c">
          <Top />
        </el-icon>
      </span>
    </template>
  </el-table-column>
</el-table>
 <div class="pagination-container">
    <el-pagination
      background
      layout="prev, pager, next"
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      @current-change="handlePageChange"
    />
  </div>
    </div>

  </div>
</template>

<style scoped lang="scss">
  * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }

    body {
      font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
                   'Microsoft YaHei', Arial, sans-serif;
      background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);
      min-height: 100vh;
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 20px;
    }

    .dashboard {
      width: 100%;
      max-width: 1200px;
      background: #fff;
      border-radius: 16px;
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
      overflow: hidden;
    }

    .header {
      background: linear-gradient(90deg, #409EFF 0%, #64b5ff 100%);
      color: white;
      padding: 25px 30px;
      text-align: center;
      position: relative;
    }

    .header h1 {
      font-size: 28px;
      font-weight: 600;
      margin-bottom: 5px;
      letter-spacing: 1px;
    }

    .status {
      position: absolute;
      top: 20px;
      right: 20px;
      background: rgba(255, 255, 255, 0.2);
      padding: 5px 15px;
      border-radius: 20px;
      font-size: 14px;
      display: flex;
      align-items: center;
    }

    .status-dot {
      width: 10px;
      height: 10px;
      background: #4caf50;
      border-radius: 50%;
      margin-right: 8px;
      animation: pulse 1.5s infinite;
    }

    @keyframes pulse {
      0% { opacity: 0.5; }
      50% { opacity: 1; }
      100% { opacity: 0.5; }
    }

    .content {
      padding: 30px;
    }

    .control-panel {
      background: #f8fafc;
      border-radius: 12px;
      padding: 20px;
      margin-bottom: 30px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .control-label {
      font-size: 18px;
      font-weight: 500;
      color: #2c3e50;
    }

    .charts-container {
      display: flex;
      gap: 30px;
      margin-top: 20px;
    }

    @media (max-width: 600px) {
      .charts-container {
        flex-direction: column;
      }
    }

    .chart-card {
      flex: 1;
      min-width: 500px;
      max-width: 600px;
      background: #fff;
      border-radius: 12px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
      padding: 20px;
      transition: transform 0.3s ease, box-shadow 0.3s ease;
      display: flex;
      flex-direction: column;
    }

    .chart-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 6px 25px rgba(64, 158, 255, 0.15);
    }

    .chart-title {
      text-align: center;
      font-size: 18px;
      font-weight: 500;
      color: #409EFF;
      margin-bottom: 20px;
      padding-bottom: 15px;
      border-bottom: 1px solid #eee;
    }



    .data-display {
      text-align: center;
      margin-top: -100px;
      font-size: 18px;
      background: #f8fafc;
      padding: 12px;
      border-radius: 8px;
      border-left: 4px solid #409EFF;
    }



    .unit {
      font-size: 16px;
      color: #7f8c8d;
      margin-left: 2px;
    }

    .info {
      background: #e3f2fd;
      border-radius: 10px;
      padding: 20px;
      margin-top: 30px;
      border-left: 4px solid #409EFF;
    }

    .info-title {
      font-weight: 600;
      color: #409EFF;
      margin-bottom: 10px;
    }

    .info-content {
      color: #546e7a;
      line-height: 1.6;
    }

    .footer {
      text-align: center;
      padding: 20px;
      color: #7f8c8d;
      font-size: 14px;
      border-top: 1px solid #eee;
    }
.status-dot.active {
  background: #4caf50;
}

.status-dot.inactive {
  background: #ff5252;
  animation: none;
}
.temp-cell {
  display: inline-block;
  padding: 5px 10px;
  border-radius: 4px;
  transition: all 0.3s;

  &.cell-alert {
    background-color: #fef0f0;
    color: #f56c6c;
    font-weight: bold;
    position: relative;
    padding-right: 25px;

    .cell-icon {
      position: absolute;
      right: 5px;
      top: 50%;
      transform: translateY(-50%);
    }
  }
}
.pagination-container {
  margin-top: 2rem;
  display: flex;
  justify-content: center;

  :deep(.el-pagination) {
    .el-pager li {
      border-radius: 8px;
      margin: 0 4px;
    }

    .btn-prev, .btn-next {
      border-radius: 8px;
    }
  }
}
.data-display span {
  font-size: 20px;
  font-weight: bold;
  transition: color 0.3s ease;
}

.data-display .warning {
  color: #f56c6c;
  font-weight: bold;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}
</style>
