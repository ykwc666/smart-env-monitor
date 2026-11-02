<script setup>
import { ref, onBeforeUnmount, onMounted } from 'vue'
import { sp } from '@/stores'
import GuanganChart from '@/views/gg/components/GuanganChart.vue';
import { ggcahnge,ggList,ggGet } from '@/api/gg.js';
import { Client } from '@stomp/stompjs';
import { computed } from 'vue'
import { formatTime } from '@/utils/format'

const spStore = sp()
const switchValue = ref(false)
const light = ref(0); // 光照强度值（单位：lux）
const client = ref(null); // STOMP客户端引用
const lightHistory = ref([]);
const MAX_HISTORY = 12; // 保留最近12个数据点（1分钟）
const Gglist = ref([])
const params = ref({
  id: spStore.id,
  min: 0,
  max: 10000,
  timeRange: []
});
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 初始化STOMP连接
const initWebSocket = () => {
  client.value = new Client({
    brokerURL: 'ws://localhost:8080/ws',
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,

    onConnect: () => {
       const subscriptionPath = `/topic/light/${spStore.id}`;
       client.value.subscribe(subscriptionPath, (message) => {
        const data = JSON.parse(message.body);
        light.value = data.gg; // 更新光照强度值
        console.log(light)
        lightHistory.value.push({
          value: data.gg,
          time: new Date().toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
          })
        });

        if (lightHistory.value.length > MAX_HISTORY) {
          lightHistory.value.shift();
        }
      });
    },

    onStompError: (frame) => {
      console.error('STOMP协议错误:', frame.headers.message);
    },

    onWebSocketError: (event) => {
      console.error('WebSocket连接错误', event);
    }
  });

  client.value.activate();
};

// 开关状态变化处理
const handleSwitchChange = async (value) => {
  switchValue.value = value;
  spStore.setGgstatus(value ? 1 : 0);
  if (value) {
    // 开启时初始化WebSocket
    initWebSocket();
  } else {
    // 关闭时停用连接并重置所有数据
    if (client.value && client.value.active) {
      client.value.deactivate();
    }
    light.value = 0; // 重置光照值
    lightHistory.value = []; // 清空历史数据
  }

  await ggcahnge({  status: value ? 1 : 0,id: spStore.id });
}

// 组件卸载时断开连接
onBeforeUnmount(() => {
  if (client.value && client.value.active) {
    client.value.deactivate();
  }
});

// 检查是否需要高亮显示（警戒值设为500lux）
const isHighLight = computed(() => light.value > 5000);

onMounted(() => {
  switchValue.value = spStore.getGgstatus() === 1;
  GgList()
})
const GgList = async () => {
  const res = await ggList({
    id: spStore.id,
    pageSize: pageSize.value,
    page: currentPage.value
  })
  console.log(res)
  Gglist.value =res.data.data.records
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
    min: 0,
    max: 10000,
    timeRange: []
  };
  currentPage.value = 1  // 重置到第一页
  onSearch()
}
const onSearch = async () => {
  const res = await ggGet({
    ...params.value,
    page: currentPage.value,
    pageSize: pageSize.value
  })
  Gglist.value = res.data.data.records
  total.value = res.data.data.total
}
</script>

<template>
   <div v-if="spStore.getStatus(spStore.$id) === 0" class="loading-overlay">
  <span>总开关关闭中</span>
</div>
  <div class="switch-container" v-else>
    <h3>{{spStore.id}}机房光照强度监控系统</h3>

    <div class="switch-item">
      <label class="switch-label">光照强度数据获取：</label>
      <el-switch
        v-if="spStore.status !== 0"
        v-model="switchValue"
        @change="handleSwitchChange"
        active-text="开启"
        inactive-text="关闭"
      />
    </div>

    <GuanganChart
      v-if="switchValue"
      :light-data="lightHistory"
      :current-light="light" />

    <div v-if="switchValue" class="light-display">
      当前光照强度:
      <span :class="{ 'high-light': isHighLight }">{{ light }} lux</span>
    </div>

  <el-form inline>
  <!-- 温度范围应使用范围选择控件 -->
  <el-form-item label="光照强度范围:">
    <el-input-number
      v-model="params.min"
      :min="0"
      :max="100"
      placeholder="最低光照"
      style="width: 150px"
    ></el-input-number>
    <span class="range-separator">——</span>
    <el-input-number
      v-model="params.max"
      :min="0"
      :max="10000"
      placeholder="最高光照"
      style="width: 150px"
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
  :data="Gglist"
  stripe
  highlight-current-row
  style="width: 100%"
  empty-text="暂无光照强度记录"
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
    prop="number"
    label="光照强度(lux)"
    align="center"
    sortable
  >
    <template #default="{ row }">
      <span :class="['temp-cell', { 'cell-alert': row.number > isHighLight }]">
        {{ row.number }}
        <el-icon v-if="row.number > isHighLight" class="cell-icon" color="#f56c6c">
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
</template>

<style scoped lang="scss">
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
.switch-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', Arial, sans-serif;
}

.switch-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.switch-label {
  margin-right: 10px;
  font-weight: 500;
}

h3 {
  margin-bottom: 20px;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.light-display {
  margin-top: 20px;
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;

  span {
    font-size: 24px;
    margin-left: 10px;
    transition: color 0.3s;
  }

  .high-light {
    color: #ff0000;
    font-weight: bold;
    animation: blink 1s infinite;
  }
}

@keyframes blink {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}
</style>
