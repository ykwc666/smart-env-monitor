<script setup>
// 正确使用 defineProps，直接列出 props
const props = defineProps({
  title: {
    type: String,
    required: true
  }
});
</script>

<template>
  <el-card class="page-container">
    <!-- 使用 header 插槽 -->
    <template #header>
      <div class="header">
        <span>{{ props.title }}</span> <!-- 使用 props.title 来访问传入的 title -->
        <div class="extra">
          <!-- 这里可以放置额外的内容，但您之前使用的是 <slot name="extra"></slot>，这是正确的 -->
          <slot name="extra"></slot>
        </div>
      </div>
    </template>
    <!-- 默认插槽，用于放置组件的其他内容 -->
    <slot></slot>
  </el-card>
</template>

<style lang="scss" scoped>
.page-container {
  min-height: 100%;
  box-sizing: border-box;
}

.page-container .header { /* 由于 scoped 样式，这里不需要嵌套，但可以直接这样写 */
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
