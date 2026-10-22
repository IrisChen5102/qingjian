<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="brand">
        <span class="brand-mark">今日页</span>
        <span class="brand-sub">Today's Page · 个人手账</span>
      </div>
      <nav class="nav">
        <button :class="['nav-btn', { active: view === 'today' }]" @click="view = 'today'">今日</button>
        <button :class="['nav-btn', { active: view === 'calendar' }]" @click="view = 'calendar'">日历</button>
        <button :class="['nav-btn', { active: view === 'stats' }]" @click="view = 'stats'">统计</button>
      </nav>
      <div class="data-actions">
        <el-button size="small" text @click="exportData">导出</el-button>
        <el-button size="small" text @click="triggerImport">导入</el-button>
        <input ref="fileInput" type="file" accept="application/json" hidden @change="onImportFile" />
      </div>
    </header>

    <main class="app-main">
      <TodayView v-if="view === 'today'" :date="currentDate" @change-date="currentDate = $event" @open-editor="openEditor" />
      <CalendarView v-else-if="view === 'calendar'" @open-day="openDay" @open-editor="openEditor" />
      <StatsView v-else-if="view === 'stats'" />
      <NoteEditor v-else-if="view === 'note'" :log-date="editorDate" :note-id="editorNoteId" @back="backFromEditor" />
    </main>

    <PomoFinishToast />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import TodayView from './views/TodayView.vue';
import CalendarView from './views/CalendarView.vue';
import StatsView from './views/StatsView.vue';
import NoteEditor from './views/NoteEditor.vue';
import PomoFinishToast from './components/PomoFinishToast.vue';
import { api } from './api';
import { pomoStore } from './stores/pomodoro';

const now = new Date();
const pad = (n) => String(n).padStart(2, '0');
const currentDate = ref(`${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`);

const initialView = location.hash === '#calendar' ? 'calendar' : location.hash === '#stats' ? 'stats' : 'today';
const view = ref(initialView);
const editorDate = ref(currentDate.value);
const editorNoteId = ref(null);
const returnView = ref('today');
const fileInput = ref(null);

function openDay(date) {
  currentDate.value = date;
  view.value = 'today';
}

function openEditor(payload) {
  returnView.value = view.value === 'note' ? returnView.value : view.value;
  editorDate.value = (payload && payload.date) || currentDate.value;
  editorNoteId.value = (payload && payload.id) || null;
  view.value = 'note';
}

function backFromEditor() {
  view.value = returnView.value;
}

watch(
  () => pomoStore.openPomodoro,
  (flag) => {
    if (flag) {
      view.value = 'today';
      // 具体切到「专注」标签由 TodayView 内的 watcher 完成
    }
  }
);

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
