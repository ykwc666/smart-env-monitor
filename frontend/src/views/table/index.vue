<script setup>
import { reactive, ref,onMounted } from 'vue'
import {searchtable} from '@/api/table'
import { formatTime } from '@/utils/format'
// 新增光照和电流电压的数据模型
const id = ref(0);

const lightParams = reactive({
  name: '光照',
  id: id,
  maxLight: 1000,
  timeRange: []
})

const currentVoltageParams = reactive({
  id: id,
  name: '电流电压',
  maxcurrent: 30,
  maxVoltage: 30,
  timeRange: []
})

const params = reactive({
  id: id,
  name: '温湿度',
  maxTemp: 20,
  maxHumi: 70,
  timeRange: []
})

// 新增数据列表
const environmentDatalist = ref([])
const lightDatalist = ref([])
const currentVoltageDatalist = ref([])



const environmentPage = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 新增分页参数
const lightPage = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const currentVoltagePage = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 新增阈值配置
const thresholds = {
  temp: 20,
  humi: 70,
  light: 500,
  voltage: 30,
  current: 30
}

const handleClear = () => {
  id.value = 0;
};

onMounted(() => {
  fetchLightData()
  fetchEnvData()
  fetchCurrentVoltageData()
});

// 新增光照和电流电压的获取方法
const fetchLightData = async () => {
  // 调用光照接口，示例参数
  const res = await searchtable({
    ...lightParams,
    page: lightPage.currentPage,
    pageSize: lightPage.pageSize
  })
  console.log(res);
  lightDatalist.value = res.data.data.records
  lightPage.total = res.data.data.total
}
const fetchEnvData = async () => {
  const res = await searchtable({
    ...params,
    page: environmentPage.currentPage,
    pageSize: environmentPage.pageSize
  })
  environmentDatalist.value = res.data.data.records
  environmentPage.total = res.data.data.total
  console.log(res);
}

const fetchCurrentVoltageData = async () => {
  // 调用电流电压接口
  const res = await searchtable({
    ...currentVoltageParams,
    page: currentVoltagePage.currentPage,
    pageSize: currentVoltagePage.pageSize
  })
  console.log(res);
  currentVoltageDatalist.value = res.data.data.records
  currentVoltagePage.total = res.data.data.total
}

// 修改后的搜索方法
const onSearch = () => {
  fetchEnvData()
  fetchLightData()
  fetchCurrentVoltageData()
}

// 重置方法需要扩展
const onReset = () => {
  Object.assign(params, {
    maxTemp: 100,
    maxHumi: 100,
    timeRange: []
  })
  Object.assign(lightParams, {
    maxLight: 1000,
    timeRange: []
  })
  Object.assign(currentVoltageParams, {
    maxcurrent: 100,
    maxVoltage: 240,
    timeRange: []
  })
  onSearch()
}
</script>

<template>
  <!-- 保持原有搜索表单，添加光照和电压范围 -->
   <el-form inline><!--
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

    <el-form-item label="光照范围:">
      <el-input-number v-model="lightParams.minLight" :min="0" :max="1000" />
      <span class="range-separator">-</span>
      <el-input-number v-model="lightParams.maxLight" :min="0" :max="1000" />
    </el-form-item>

    <el-form-item label="电流范围:">
      <el-input-number v-model="currentVoltageParams.mincurrent" :min="0" :max="100" />
      <span class="range-separator">-</span>
      <el-input-number v-model="currentVoltageParams.maxcurrent" :min="0" :max="100" />
    </el-form-item>

    <el-form-item label="电压范围:">
      <el-input-number v-model="currentVoltageParams.minVoltage" :min="180" :max="250" />
      <span class="range-separator">-</span>
      <el-input-number v-model="currentVoltageParams.maxVoltage" :min="180" :max="250" />
    </el-form-item>-->
     <el-form-item label="灯杆编号:">
    <el-select
      v-model="id"
      clearable
      placeholder="全部"
      style="width: 120px;"
      @clear="handleClear"
    >
      <el-option
        v-show="false"
        label="全部"
        :value="0"
      />
      <el-option
        label="1"
        :value="1"
      />
      <el-option
        label="2"
        :value="2"
      />
      <el-option
        label="3"
        :value="3"
      />
    </el-select>
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
      </el-form-item>
  </el-form>
  <!-- 三个表格使用el-tabs切换 -->
  <el-tabs type="border-card">
    <el-tab-pane label="温湿度监测">
      <el-table :data="environmentDatalist" stripe>
       <el-table-column prop="time" label="记录时间" width="220">
        <template #default="{ row }">
          {{ formatTime(row.time) }}
        </template>
      </el-table-column>

      <el-table-column prop="id" label="灯杆编号" width="220" align="center" />

      <el-table-column prop="temp" label="温度(°C)" align="center" sortable>
        <template #default="{ row }">
          <span :class="['value-cell', { 'alert': row.temp >= thresholds.temp }]">
            {{ row.temp }}
            <el-icon v-if="row.temp > thresholds.temp" class="cell-icon" color="#f56c6c">
              <Top />
            </el-icon>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="humi" label="湿度(%)" align="center" sortable>
        <template #default="{ row }">
          <span :class="['value-cell', { 'alert': row.humi >= thresholds.humi }]">
            {{ row.humi }}
            <el-icon v-if="row.humi > thresholds.humi" class="cell-icon" color="#f56c6c">
              <Top />
            </el-icon>
          </span>
        </template>
      </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="environmentPage.currentPage"
        :page-size="environmentPage.pageSize"
        :total="environmentPage.total"
        @current-change="handlePageChange"
      />
    </el-tab-pane>

    <el-tab-pane label="光照强度监测">
      <el-table :data="lightDatalist" stripe>
        <el-table-column prop="time" label="记录时间" width="220">
          <template #default="{ row }">{{ formatTime(row.time) }}</template>
        </el-table-column>
         <el-table-column prop="id" label="灯杆编号" width="220" align="center" />
        <el-table-column prop="number" label="光照强度(lx)" align="center" sortable>
          <template #default="{ row }">
            <span :class="['value-cell', { 'alert': row.number >= thresholds.light }]">
              {{ row.number }}
              <el-icon v-if="row.number > thresholds.number" color="#f56c6c"><Top /></el-icon>
            </span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="lightPage.currentPage"
        :page-size="lightPage.pageSize"
        :total="lightPage.total"
        @current-change="fetchLightData"
      />
    </el-tab-pane>

    <el-tab-pane label="电流电压监测">
      <el-table :data="currentVoltageDatalist" stripe>
        <el-table-column prop="time" label="记录时间" width="220">
          <template #default="{ row }">{{ formatTime(row.time) }}</template>
        </el-table-column>
         <el-table-column prop="id" label="灯杆编号" width="220" align="center" />
        <el-table-column prop="current" label="电流(A)" align="center" sortable >
          <template #default="{ row }">
      <span :class="['value-cell', { 'alert': row.current >= thresholds.current }]">
        {{ row.current }}
        <el-icon v-if="row.current >  thresholds.current" color="#f56c6c">
          <Top />
        </el-icon>
      </span>
    </template>
    </el-table-column>
        <el-table-column prop="voltage" label="电压(V)" align="center" sortable>
          <template #default="{ row }">
            <span :class="['value-cell', { 'alert': row.voltage >= thresholds.voltage }]">
              {{ row.voltage }}
              <el-icon v-if="row.voltage > thresholds.voltage" color="#f56c6c"><Top /></el-icon>
            </span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="currentVoltagePage.currentPage"
        :page-size="currentVoltagePage.pageSize"
        :total="currentVoltagePage.total"
        @current-change="fetchCurrentVoltageData"
      />
    </el-tab-pane>
  </el-tabs>
</template>

<style scoped lang='scss'>
.value-cell {
  display: inline-flex;
  align-items: center;
  gap: 4px;

  &.alert {
    color: #f56c6c;
    font-weight: bold;
  }
}

.cell-icon {
  vertical-align: middle;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
