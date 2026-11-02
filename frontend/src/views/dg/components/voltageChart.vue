<template>
  <div ref="chartRef" style="width: 500px; height: 500px;"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import * as echarts from 'echarts';
import { debounce } from 'lodash-es';

// 定义接收的props
const props = defineProps({
  voltage: Number // 电压值（单位：V）
});

const chartRef = ref(null);
let myChart = null;

// 初始化图表配置
const initChart = () => {
  if (!chartRef.value) return;

  myChart = echarts.init(chartRef.value);

  const option = {
    series: [
      {
        type: 'gauge',
        startAngle: 180,
        endAngle: 0,
        min: 0,
        max: 100, // ✅ 改为 100V
        splitNumber: 10, // 每10V一个主刻度
        itemStyle: {
          color: '#FF9E0D', // 橙色指针（更符合电气仪表风格）
          shadowColor: 'rgba(255, 158, 13, 0.3)',
          shadowBlur: 10,
          shadowOffsetX: 2,
          shadowOffsetY: 2
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
            // 电压范围颜色分段（安全区/工作区/警告区）
            color: [
              [0.3, '#67e0e3'], // 0-300V 绿色安全区
              [0.7, '#37a2da'], // 300-700V 蓝色工作区
              [1, '#fd666d']    // 700-1000V 红色警告区
            ]
          }
        },
        axisTick: {
          splitNumber: 5, // 增加刻度密度
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
          formatter: function(value) {
            return value + 'V'; // 电压单位
          }
        },
        title: {
          show: true,
          offsetCenter: [0, '-20%'],
          fontSize: 22,
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
          formatter: function (value) {
            return '{value|' + value.toFixed(1) + '}{unit|V}'; // 显示1位小数
          },
          rich: {
            value: {
              fontSize: 42,
              fontWeight: 'bolder',
              color: '#222'
            },
            unit: {
              fontSize: 20,
              color: '#666',
              padding: [0, 0, -15, 5]
            }
          }
        },
        data: [
          {
            value: props.voltage || 0,
            name: '电压' // 仪表标题
          }
        ]
      }
    ]
  };

  myChart.setOption(option);
};

// 更新图表数据
const updateChart = (voltageValue) => {
  if (!myChart) return;

  myChart.setOption({
    series: [{
      data: [{
        value: voltageValue,
        name: '电压'
      }]
    }]
  });
};

// 监听电压值变化
watch(() => props.voltage, (newVal) => {
  updateChart(newVal);
});

// 初始化图表
onMounted(() => {
  initChart();

  // 添加防抖的窗口大小调整监听
  const resizeHandler = debounce(() => {
    if (myChart) {
      myChart.resize();
    }
  }, 300);

  window.addEventListener('resize', resizeHandler);

  // 在组件卸载时移除监听器
  onBeforeUnmount(() => {
    window.removeEventListener('resize', resizeHandler);
  });
});

// 销毁图表实例
onBeforeUnmount(() => {
  if (myChart) {
    myChart.dispose();
    myChart = null;
  }
});
</script>

