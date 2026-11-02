<script setup>
import { ref, onMounted } from 'vue'
import { sp } from '@/stores'
import { Client } from '@stomp/stompjs'
import { wsdlistservice,wsdcahnge,getwsd,wsdadd,wsdbyid,wsdupdate,wsddelete } from '@/api/wsd'
import { formatTime } from '@/utils/format'
import GuanganChart from './GuanganChart.vue'
import GuanganChartCopy from './GuanganChart copy.vue'

const dialogVisible = ref(false)
const xgdialogVisible = ref(false)
const wdHistory =ref([])
const humiHistory=ref([])

const form = ref({
  temperature: '',
  humidity: '',
  deviceId: '',
  recordTime: '',
})
const wsd = ref({
  id: '',
  temp: '',
  humi: '',
  time: '',
  byid: '',
})

const rules = {
  temperature: [
    { required: true, message: '请输入温度值', trigger: 'blur' },
    { pattern: /^-?\d+(\.\d{1,2})?$/, message: '请输入有效的温度值', trigger: 'blur' }
  ],
  humidity: [
    { required: true, message: '请输入湿度值', trigger: 'blur' },
    { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入有效的湿度值', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (value < 0 || value > 100) {
          callback(new Error('湿度值必须在0-100之间'));
        } else {
          callback();
        }
      }, trigger: 'blur' }
  ],
  deviceId: [
    { required: true, message: '请选择设备', trigger: 'change' }
  ],
  recordTime: [
    { required: true, message: '请选择记录时间', trigger: 'change' }
  ]
}

const formRef = ref(null) // 用于表单验证的ref
const addwsd = async () => {
  await wsdadd(
    {
      temp: form.value.temperature,
      humi: form.value.humidity,
      id: form.value.deviceId,
      time: form.value.recordTime,
    }
  )
}
const submitForm = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      // 验证通过，提交表单
      console.log('表单数据:', form.value)
      // 这里可以添加提交数据的逻辑
      dialogVisible.value = false
      addwsd()
    } else {
      console.log('表单验证失败')
    }
  })
}

const handleClose = (done) => {
  formRef.value.resetFields()
  done()
}
const Close = () => {
  formRef.value.resetFields()
  dialogVisible.value = false
}

const spStore = sp()
const switchValue = ref(false)
const environmentData = ref({ temp: 0, humi: 0 }) // 修改数据结构
const environmentDatalist = ref([])
const thresholds = ref({
  temp: 30,  // 温度阈值
  humi: 70   // 湿度阈值
})
const params = ref({
  id: spStore.id,
  minTemp: 0,
  maxTemp: 100,
  minHumi: 0,
  maxHumi: 100,
  timeRange: []
})
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// WebSocket连接修改
const client = new Client({
  brokerURL: 'ws://localhost:8080/ws',
  onConnect: () => {
    const subscriptionPath = `/topic/environment/${spStore.id}`;
    client.subscribe(subscriptionPath, (message) => {
      const data = JSON.parse(message.body);
      getEnvList();
      environmentData.value = {
        temp: data.temp,
        humi: data.humi
      }
      wdHistory.value.push({
        value:data.temp,
         time: new Date().toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
          })
      })
      humiHistory.value.push({
        value:data.humi,
         time: new Date().toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
          })
      })
      console.log('环境数据更新：', data)
    });
  }
});
client.activate();
const handleSwitchChange = async (newvalue) => {
    switchValue.value = newvalue;
    spStore.setWsdstatus(newvalue ? 1 : 0);
    console.log(spStore.wsdstatus);
    await wsdcahnge({ status: newvalue ? 1 : 0 ,id: spStore.id});

};
// 获取环境数据列表
const getEnvList = async () => {
  const res = await wsdlistservice({
    id: spStore.id,
    page: currentPage.value,
    pageSize: pageSize.value
  });
  console.log(res);
  environmentDatalist.value = res.data.data.records
  console.log(environmentDatalist.value)
  total.value = res.data.data.total
  console.log(environmentDatalist)
}

const onSearch = async () => {
  const res = await getwsd({
    ...params.value,
    page: currentPage.value,
    pageSize: pageSize.value
  });

  environmentDatalist.value = res.data.data.records

  // 更新总记录数
  total.value = res.data.data.total
}

// 新增分页变化处理
const handlePageChange = (newPage) => {
  currentPage.value = newPage
  onSearch()
}

// 修改重置功能（重置分页参数）
const onReset = () => {
  params.value = {
    id: spStore.id,
    minTemp: 0,
    maxTemp: 100,
    minHumi: 0,
    maxHumi: 100,
    timeRange: []
  }
  currentPage.value = 1  // 重置到第一页
  onSearch()
}
onMounted(() => {
  switchValue.value = spStore.getWsdstatus() === 1;
  getEnvList();
})
const wsdFormRef = ref(null) // 修改表单的ref

const wsdRules = {
  temp: [
    { required: true, message: '请输入温度值', trigger: 'blur' },
    { pattern: /^-?\d+(\.\d{1,2})?$/, message: '请输入有效的温度值', trigger: 'blur' }
  ],
  humi: [
    { required: true, message: '请输入湿度值', trigger: 'blur' },
    { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入有效的湿度值', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (value < 0 || value > 100) {
          callback(new Error('湿度值必须在0-100之间'));
        } else {
          callback();
        }
      }, trigger: 'blur' }
  ],
  id: [
    { required: true, message: '请选择机房', trigger: 'change' }
  ],
  time: [
    { required: true, message: '请选择记录时间', trigger: 'change' }
  ]
}
const selectwsd = async(id) => {
   const res = await wsdbyid({id:id})
   console.log(res)
   wsd.value = res
   wsd.value.byid = id
   console.log(wsd.value.byid)
}
const xg = (id) => {
  selectwsd(id)
  xgdialogVisible.value = true
}
const xgClose = () => {
  xgdialogVisible.value = false
  wsdFormRef.value.resetFields() // 注意：这里重置的是表单，而不是wsd对象
}

const xghandleClose = (done) => {
  wsdFormRef.value.resetFields() // 同样，重置表单
  done()
}
const updatewsd = async () => {
  await wsdupdate(
    {
      byid: wsd.value.byid,
      temp: wsd.value.temp,
      humi: wsd.value.humi,
      id: wsd.value.id,
      time: wsd.value.time,
    }
  )
}
const xgsubmitForm = () => {
   wsdFormRef.value.validate((valid) => {
    if (valid) {
      // 验证通过，提交表单
      console.log('表单数据:', wsd.value)
      // 这里可以添加提交数据的逻辑
      xgdialogVisible.value = false
      updatewsd()
      getEnvList()
    } else {
      console.log('表单验证失败')
    }
  })
}
const handleDelete = async (id) => {
  await wsddelete({id:id})
  getEnvList()
}
</script>

<template>
  <div v-if="spStore.getStatus(spStore.$id) === 0" class="loading-overlay">
  <span>总开关关闭中</span>
</div>
  <div class="dashboard-container" v-else >
    <!-- 标题修改 -->
    <div class="header-section">
      <h2 class="dashboard-title">
        <el-icon class="title-icon"><OfficeBuilding /></el-icon>
        {{ spStore.id }} 机房智能温湿度监控系统
        <span class="status-badge" :class="spStore.status === 0 ? 'offline' : 'online'">
          {{ spStore.status === 0 ? '离线' : '在线' }}
        </span>
      </h2>
    </div>

    <!-- 温湿度状态卡片 -->
    <div class="status-cards">
      <div class="status-card temp-card">
        <div class="current-value">
          <div class="value-container">
            <span class="value-label">实时温度</span>
            <span :class="['value', { 'critical': environmentData.temp > thresholds.temp }]">
              {{ environmentData.temp || '--' }}°C
              <el-icon v-if="environmentData.temp > thresholds.temp" class="alert-icon"><Warning /></el-icon>
            </span>
          </div>
          <div class="threshold-info">
            安全阈值：{{ thresholds.temp }}°C
          </div>
        </div>
      </div>

      <div class="status-card humi-card">
        <div class="current-value">
          <div class="value-container">
            <span class="value-label">实时湿度</span>
            <span :class="['value', { 'critical': environmentData.humi > thresholds.humi }]">
              {{ environmentData.humi || '--' }}%
              <el-icon v-if="environmentData.humi > thresholds.humi" class="alert-icon"><Warning /></el-icon>
            </span>
          </div>
          <div class="threshold-info">
            安全阈值：{{ thresholds.humi }}%
          </div>
        </div>
      </div>
    </div>

    <!-- 控制面板 -->
    <div class="control-panel">
      <div class="control-group">
        <label class="control-label">监控系统：</label>
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
    <GuanganChart
      v-if="switchValue"
      :light-data="wdHistory"
      :current-light="environmentData.temp" />
      <GuanganChartCopy
      v-if="switchValue"
      :light-data="humiHistory"
      :current-light="environmentData.humi" />
      <div>
      温度阀值:<el-input v-model="thresholds.temp" placeholder="请输入温度值阀值" style="width: 90px">
                    <template slot="append">°C</template>
                </el-input></div>
      湿度阀值:<el-input v-model="thresholds.humi" placeholder="请输入湿度值阀值" style="width: 90px">
                    <template slot="append">°C</template>
                </el-input>

    <!-- 搜索表单 -->
    <el-form inline>
      <el-form-item label="温度范围:">
        <el-input-number v-model="params.minTemp" :min="0" :max="100" />
        <span class="range-separator">-</span>
        <el-input-number v-model="params.maxTemp" :min="0" :max="100" />
      </el-form-item>

      <el-form-item label="湿度范围:">
        <el-input-number v-model="params.minHumi" :min="0" :max="100" />
        <span class="range-separator">-</span>
        <el-input-number v-model="params.maxHumi" :min="0" :max="100" />
      </el-form-item>

      <el-form-item label="时间范围:">
        <el-date-picker
          v-model="params.timeRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="onSearch">搜索</el-button>
        <el-button @click="onReset">重置</el-button>
        <el-button type="primary" @click="dialogVisible = true">新增温湿度数据</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table :data="environmentDatalist" stripe>
      <el-table-column prop="time" label="记录时间" width="220">
        <template #default="{ row }">
          {{ formatTime(row.time) }}
        </template>
      </el-table-column>

      <el-table-column prop="id" label="灯杆编号" width="220" align="center" />

      <el-table-column prop="temp" label="温度(°C)" align="center" sortable>
        <template #default="{ row }">
          <span :class="['value-cell', { 'alert': row.temp > thresholds.temp }]">
            {{ row.temp }}
            <el-icon v-if="row.temp > thresholds.temp" class="cell-icon" color="#f56c6c">
              <Top />
            </el-icon>
          </span>
        </template>
      </el-table-column>

      <el-table-column prop="humi" label="湿度(%)" align="center" sortable>
        <template #default="{ row }">
          <span :class="['value-cell', { 'alert': row.humi > thresholds.humi }]">
            {{ row.humi }}
            <el-icon v-if="row.humi > thresholds.humi" class="cell-icon" color="#f56c6c">
              <Top />
            </el-icon>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center" prop="byid">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="xg(row.byid)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row.byid)">删除</el-button>
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
   <el-dialog
        title="新增温湿度数据"
        v-model="dialogVisible"
        width="500px"
        :before-close="handleClose">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
            <el-form-item label="温度" prop="temperature">
                <el-input v-model="form.temperature" placeholder="请输入温度值" style="width: 300px">
                    <template slot="append">°C</template>
                </el-input>
            </el-form-item>
            <el-form-item label="湿度" prop="humidity">
                <el-input v-model="form.humidity" placeholder="请输入湿度值" style="width: 300px">
                    <template slot="append">% RH</template>
                </el-input>
            </el-form-item>
            <el-form-item label="灯杆编号" prop="deviceId">
                <el-select v-model="form.deviceId" placeholder="请选择灯杆" style="width: 300px">
                    <el-option label="灯杆1" value="1"></el-option>
                    <el-option label="灯杆2" value="2"></el-option>
                    <el-option label="灯杆3" value="3"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="记录时间" prop="recordTime">
                <el-date-picker
                    v-model="form.recordTime"
                    type="datetime"
                    placeholder="选择日期时间"
                    style="width: 300px">
                </el-date-picker>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button @click="Close">取 消</el-button>
            <el-button type="primary" @click="submitForm">确 定</el-button>
        </span>
    </el-dialog>
   <el-dialog
        title="修改温湿度数据"
        v-model="xgdialogVisible"
        width="500px"
        :before-close="xghandleClose">
        <el-form :model="wsd" :rules="wsdRules" ref="wsdFormRef" label-width="100px">
            <el-form-item label="温度" prop="temp">
                <el-input v-model="wsd.temp" placeholder="请输入新的温度值" style="width: 300px">
                    <template slot="append">°C</template>
                </el-input>
            </el-form-item>
            <el-form-item label="湿度" prop="humi">
                <el-input v-model="wsd.humi" placeholder="请输入新的湿度值" style="width: 300px">
                    <template slot="append">% RH</template>
                </el-input>
            </el-form-item>
            <el-form-item label="机房编号" prop="id">
                <el-select v-model="wsd.id" placeholder="请选择机房" style="width: 300px">
                    <el-option label="机房1" value="1"></el-option>
                    <el-option label="机房2" value="2"></el-option>
                    <el-option label="机房3" value="3"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="记录时间" prop="time">
                <el-date-picker
                    v-model="wsd.time"
                    type="datetime"
                    placeholder="选择日期时间"
                    style="width: 300px">
                </el-date-picker>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button @click="xgClose">取 消</el-button>
            <el-button type="primary" @click="xgsubmitForm">确 定</el-button>
        </span>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
/* 新增双卡片布局 */
.status-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.status-card {
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

  &.temp-card {
    background: linear-gradient(135deg, #fff5f5, #fff);
    border-left: 4px solid #f56c6c;
  }

  &.humi-card {
    background: linear-gradient(135deg, #f0f9ff, #fff);
    border-left: 4px solid #409eff;
  }

  .current-value {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .value {
      font-size: 2.2rem;
      font-weight: 700;
      transition: all 0.3s ease;

      &.critical {
        color: #f56c6c;
        animation: critical-pulse 0.8s infinite;
      }
    }

    .threshold-info {
      background: rgba(255, 255, 255, 0.8);
      padding: 0.5rem 1rem;
      border-radius: 8px;
      font-size: 0.9rem;
    }
  }
}

/* 调整表格样式 */
.value-cell {
  &.alert {
    color: #f56c6c;
    font-weight: 600;
  }

  .cell-icon {
    margin-left: 0.3rem;
    animation: bounce 1s infinite;
  }
}

/* 新增湿度相关动画 */
@keyframes critical-pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
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

/* 调整表格底部间距 */
.el-table {
  margin-bottom: 2rem;
}
</style>
