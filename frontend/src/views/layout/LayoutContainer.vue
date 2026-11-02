<script setup>
import { Setting, Pouring, Sunny, Loading, Lightning, VideoCamera,Briefcase,Document } from '@element-plus/icons-vue'
import { ref } from 'vue'
import { sp } from '@/stores'
import { useRouter } from 'vue-router'

const router = useRouter()
const spStore = sp()
// 定义机房数据
const rooms = ref([
  { id: 1, name: '灯杆1' },
  { id: 2, name: '灯杆2' },
  { id: 3, name: '灯杆3' }
])


// 定义功能菜单项
const menuItems = ref([
  { path: 'kg', name: '开关', icon: Setting },
  { path: 'wsd', name: '温湿度控制', icon: Pouring },
  // { path: 'wd', name: '温度控制', icon: Sunny },
  // { path: 'sd', name: '湿度控制', icon: Pouring },
  { path: 'gg', name: '光感控制', icon: Loading },
  { path: 'dg', name: '电感控制', icon: Lightning },
  { path: 'sxt', name: '摄像头控制', icon: VideoCamera }
])
const handleClick = (item) => {
  spStore.setId(item.id)
  console.log('点击了：', spStore.id)
}
const handleTitleClick = (room) => {
  spStore.setId(room.id)
  // 跳转到开关界面
  router.push(`/room${room.id}/kg`)
}
</script>

<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="el-aside__logo"></div>
      <el-menu
        active-text-color="#ffd04b"
        background-color="#232323"
        :default-active="$route.path"
        text-color="#fff"
        router
         unique-opened
      >
      <el-menu-item index="/room">
             <div class="submenu-title" >
          <el-icon><Document /></el-icon>
          <span>灯杆总体数据</span>
        </div>
        </el-menu-item>

        <!-- 循环生成机房菜单 -->
        <el-sub-menu
          v-for="room in rooms"
          :key="room.id"
          :index="`room${room.id}`"
          @click="handleClick(room)"
        >
         <template #title>
            <div class="submenu-title" @click.stop="handleTitleClick(room)">
              <el-icon><Briefcase /></el-icon>
              <span>{{ room.name }}</span>
            </div>
          </template>


          <!-- 循环生成功能菜单项 -->
          <el-menu-item
            v-for="item in menuItems"
            :key="item.path"
            :index="`/room${room.id}/${item.path}`"
          >
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.name }}</span>
          </el-menu-item>
        </el-sub-menu>
          <el-menu-item index="/table">
             <div class="submenu-title" >
          <el-icon><Document /></el-icon>
          <span>表格</span>
        </div>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-main>
      <router-view  :key="$route.fullPath"></router-view>
    </el-main>
  </el-container>
</template>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  display: flex;

  .el-aside {
    background-color: #232323;
    &__logo {
      height: 120px;
    }
    .el-menu {
      border-right: none;
    }
  }

  .el-main {
    flex: 1;
    padding: 20px;
    background: #f5f7fa;
  }
}
.submenu-title {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 0 20px;
  cursor: pointer;

  &:hover {
    color: #ffd04b;
  }

  .el-icon {
    margin-right: 8px;
  }
}

/* 调整子菜单箭头位置 */
.el-sub-menu__icon-arrow {
  right: 12px !important;
}
.el-menu-item {
  height: 50px;
  display: flex;
  align-items: center;
  padding: 0 20px !important;

  &.is-active {
    background-color: #1a1a1a !important;
  }

  &:hover {
    background-color: #3a3a3a !important;
  }
}

/* 调整子菜单箭头位置 */
.el-sub-menu__icon-arrow {
  right: 12px !important;
}
</style>
