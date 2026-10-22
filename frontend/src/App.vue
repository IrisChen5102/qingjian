<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="brand">
        <span class="brand-mark">今日页</span>
        <span class="brand-sub">Today's Page · 个人手账</span>
      </div>
      <nav class="nav">
        <button :class="['nav-btn', { active: tab === 'today' }]" @click="tab = 'today'">今日</button>
        <button :class="['nav-btn', { active: tab === 'calendar' }]" @click="tab = 'calendar'">日历</button>
        <button :class="['nav-btn', { active: tab === 'stats' }]" @click="tab = 'stats'">统计</button>
      </nav>
      <div class="data-actions">
        <el-button size="small" text @click="exportData">导出</el-button>
        <el-button size="small" text @click="triggerImport">导入</el-button>
        <input ref="fileInput" type="file" accept="application/json" hidden @change="onImportFile" />
      </div>
    </header>

    <main class="app-main">
      <TodayView v-if="tab === 'today'" :date="currentDate" @change-date="currentDate = $event" />
      <CalendarView v-else-if="tab === 'calendar'" @open-day="openDay" />
      <StatsView v-else />
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import TodayView from './views/TodayView.vue';
import CalendarView from './views/CalendarView.vue';
import StatsView from './views/StatsView.vue';
import { api } from './api';

const now = new Date();
const pad = (n) => String(n).padStart(2, '0');
const currentDate = ref(`${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`);
const initialTab = location.hash === '#calendar' ? 'calendar' : location.hash === '#stats' ? 'stats' : 'today';
const tab = ref(initialTab);
const fileInput = ref(null);

function openDay(date) {
  currentDate.value = date;
  tab.value = 'today';
}

async function exportData() {
  const data = await api.exportAll();
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = `今日页备份_${currentDate.value}.json`;
  a.click();
  URL.revokeObjectURL(a.href);
  ElMessage.success('已导出备份文件');
}

function triggerImport() {
  fileInput.value.click();
}

async function onImportFile(event) {
  const file = event.target.files[0];
  event.target.value = '';
  if (!file) return;
  try {
    await ElMessageBox.confirm('导入会覆盖当前全部数据，确定继续吗？', '导入备份', {
      type: 'warning',
      confirmButtonText: '覆盖导入',
      cancelButtonText: '取消',
    });
  } catch (e) {
    return;
  }
  const text = await file.text();
  try {
    await api.importAll(JSON.parse(text));
    ElMessage.success('导入完成，页面将刷新');
    setTimeout(() => window.location.reload(), 600);
  } catch (e) {
    ElMessage.error('导入失败：' + e.message);
  }
}
</script>
