export const formatTime = (timeValue) => {
  // 检查空值
  if (!timeValue) return '无时间数据';

  let date;

  // 如果是数字（可能是时间戳）
  if (typeof timeValue === 'number') {
    date = new Date(timeValue);
  }
  // 如果是字符串
  else if (typeof timeValue === 'string') {
    // 尝试直接解析
    date = new Date(timeValue);

    // 如果解析失败，尝试处理可能的格式问题
    if (isNaN(date.getTime())) {
      // 移除可能的多余字符
      const cleanedTime = timeValue.replace('T', ' ').replace('Z', '');
      date = new Date(cleanedTime);

      // 如果仍然失败，尝试作为时间戳处理
      if (isNaN(date.getTime()) && !isNaN(timeValue)) {
        date = new Date(parseInt(timeValue));
      }
    }
  }
  // 如果是 Date 对象
  else if (timeValue instanceof Date) {
    date = timeValue;
  }
  // 其他情况
  else {
    return '时间格式错误';
  }

  // 检查最终日期是否有效
  if (isNaN(date.getTime())) {
    return '无效时间';
  }

  // 格式化日期
  return date.toLocaleString('zh-CN', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).replace(/\//g, '-');
}
