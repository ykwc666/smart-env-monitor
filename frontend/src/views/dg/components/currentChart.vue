<template>
  <div ref="chartContainer" style="width: 500px; height: 500px;"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import * as echarts from 'echarts';
import { debounce } from 'lodash-es';

// 接收电流数据（单位：A）
const props = defineProps({
  current: Number
});

const chartContainer = ref(null);
let currentChart = null;

// 初始化电流仪表盘
const initChart = () => {
  if (!chartContainer.value) return;

  currentChart = echarts.init(chartContainer.value);

  const option = {
    series: [{
      type: 'gauge',
      startAngle: 180,
      endAngle: 0,
      min: 0,
      max: 1000,
      splitNumber: 10,
      itemStyle: {
        color: '#FF9E0D',  // 橙色指针
        shadowColor: 'rgba(255, 158, 13, 0.3)',
        shadowBlur: 100
      },
      progress: {
        show: true,
        roundCap: true,
        width: 18
      },
      pointer: {
        icon: 'path://M2090.36389,615.30999 L2090.36389,615.30999 C2091.48372,615.30999 2092.40383,616.194028 2092.44859,617.312956 L2096.90698,728.755929 C2097.05155,732.369577 2094.2393,735.416212 2090.62566,735.56078 C2090.53845,735.564269 2090.45117,735.566014 2090.36389,735.566014 L2090.36389,735.566014 C2086.74736,735.566014 2083.81557,732.63423 2083.81557,729.017692 C2083.81557,728.930412 2083.81732,728.84314 2083.82081,728.755929 L2088.2792,617.312956 C2088.32396,616.194028 2089.24407,615.30999 2090.36389,615.30999 Z',
        length: '75%',
        width: 16,
        offsetCenter: [0, '5%']
      },
      axisLine: {
        roundCap: true,
        lineStyle: {
          width: 18,
          color: [
            [0.3, '#67e0e3'],   // 0-3A 绿色安全区
            [0.7, '#37a2da'],   // 3-7A 蓝色工作区
            [1, '#fd666d']      // 7-10A 红色警告区
          ]
        }
      },
      axisTick: {
        splitNumber: 5,
        lineStyle: {
          width: 2,
          color: '#999'
        }
      },
      splitLine: {
        length: 12,
        lineStyle: {
          width: 3,
          color: '#999'
        }
      },
      axisLabel: {
        distance: 30,
        color: '#666',
        fontSize: 14,
        formatter: (value) => value + 'mA'  // 电流单位
      },
      title: {
        show: true,
        offsetCenter: [0, '-20%'],
        fontSize: 18,
        color: '#333',
        fontWeight: 'bold'
      },
      detail: {
        backgroundColor: '#fff',
        borderColor: '#ddd',
        borderWidth: 2,
        width: '60%',
        lineHeight: 40,
        height: 40,
        borderRadius: 8,
        offsetCenter: [0, '35%'],
        valueAnimation: true,
        formatter: (value) => `{value|${value.toFixed(2)}}{unit|mA}`,
        rich: {
          value: {
            fontSize: 36,
            fontWeight: 'bolder',
            color: '#222'
          },
          unit: {
            fontSize: 18,
            color: '#666',
            padding: [0, 0, -10, 5]
          }
        }
      },
      data: [{
        value: props.current || 0,
        name: '电流'  // 仪表标题
      }]
    }]
  };

  currentChart.setOption(option);
};

// 更新电流数据
const updateChart = (currentValue) => {
  if (!currentChart) return;

  currentChart.setOption({
    series: [{
      data: [{
        value: currentValue,
        name: '电流'
      }]
    }]
  });
};

// 监听电流值变化
watch(() => props.current, (newVal) => {
  updateChart(newVal);
});

// 初始化图表
onMounted(() => {
  initChart();

  // 响应式调整
  const resizeChart = debounce(() => {
    currentChart?.resize();
  }, 300);

  window.addEventListener('resize', resizeChart);

  onBeforeUnmount(() => {
    window.removeEventListener('resize', resizeChart);
    if (currentChart) {
      currentChart.dispose();
      currentChart = null;
    }
  });
});
</script>


