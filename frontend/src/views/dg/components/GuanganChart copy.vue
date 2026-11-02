<script setup>
import { watch, ref, onMounted, onUnmounted, nextTick } from 'vue';
import * as echarts from 'echarts';
import {useStore} from '@/store/index.js';

const props = defineProps({
  lightData: Array,
  currentLight: Number
});

const chartRef = ref(null);
let myChart = null;

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return;

  nextTick(() => {
    myChart = echarts.init(chartRef.value);
    updateChart();
  });
};

// 更新图表数据
const updateChart = () => {
  if (!myChart) return;

  // 处理空数据情况 ⭐️ 新增空数据判断
  if (!props.lightData || props.lightData.length === 0) {
    myChart.setOption({
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: {
          color: '#999',
          fontSize: 16
        }
      }
    }, true);
    return;
  }

  const times = props.lightData.map(item => item.time);
  const values = props.lightData.map(item => item.value);
  const option = {
    xAxis: {
      type: 'category',
      data: times,
      axisLabel: {
        interval: 0, // 强制显示所有标签
        rotate: 30  // 标签旋转防止重叠
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: Math.max(100, ...values) , // 动态调整Y轴最大值
      axisLabel: {
        formatter: '{value} mA'
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>{c} mA'
    },
    series: [{
      name: '湿度',
      type: 'line',
      smooth: true,
      data: values,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.6)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.01)' }
        ])
      },
      markPoint: {
        data: [
          { type: 'max', name: '最大值' },
          { type: 'min', name: '最小值' }
        ]
      }
    }]
  };

  myChart.setOption(option, true); // true表示不合并直接替换
};

// 监听数据变化 (包含初始数据)
watch(() => props.lightData, () => {
  updateChart();
}, { deep: true, immediate: true });

onMounted(() => {
  initChart();
  window.addEventListener('resize', () => myChart?.resize());
});

onUnmounted(() => {
  window.removeEventListener('resize', () => myChart?.resize());
  myChart?.dispose();
});
</script>

<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<style scoped>
.chart-container {
  width: 100%;
  height: 400px;
  min-width: 300px;
}
</style>
