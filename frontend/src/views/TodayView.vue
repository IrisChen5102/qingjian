<template>
  <section class="day-view">
    <div class="date-bar">
      <button class="date-arrow" @click="shiftDate(-1)">‹</button>
      <div class="date-display">
        <span class="date-text">{{ dateLabel }}</span>
        <button v-if="!isToday" class="back-today" @click="goToday">回到今天</button>
      </div>
      <button class="date-arrow" @click="shiftDate(1)">›</button>
      <div class="date-spacer"></div>
      <label class="merge-toggle">
        <el-switch v-model="merged" size="small" />
        <span>合并视图</span>
      </label>
    </div>

    <div v-if="!merged" class="seg-tabs">
      <button
        v-for="tabItem in tabs"
        :key="tabItem.key"
        :class="{ active: activeTab === tabItem.key }"
        @click="activeTab = tabItem.key"
      >
        {{ tabItem.label }}
      </button>
    </div>

    <div class="day-content" :class="{ merged }">
      <section v-show="merged || activeTab === 'todo'">
        <h3 v-if="merged" class="section-head">待办</h3>
        <TodoPanel :date="date" :day="day" @refresh="load" />
      </section>
      <section v-show="merged || activeTab === 'timeline'">
        <h3 v-if="merged" class="section-head">时间线</h3>
        <TimelinePanel :date="date" :day="day" @refresh="load" @open-note="openNote" />
      </section>
      <section v-show="merged || activeTab === 'note'">
        <h3 v-if="merged" class="section-head">笔记</h3>
        <NotePanel :date="date" :day="day" @refresh="load" />
      </section>
      <section v-show="merged || activeTab === 'pomodoro'">
        <h3 v-if="merged" class="section-head">专注</h3>
        <PomodoroPanel :date="date" :day="day" @refresh="load" />
      </section>
    </div>

    <div class="summary-bar">
      <span class="summary-label">今日心情</span>
      <el-select v-model="mood" size="small" clearable placeholder="—" style="width: 90px">
        <el-option label="好" value="好" />
        <el-option label="一般" value="一般" />
        <el-option label="差" value="差" />
      </el-select>
      <span class="summary-label">明日重点</span>
      <el-input v-model="focus" size="small" placeholder="明天最重要的一件事…" style="flex: 1; min-width: 220px" />
      <el-button size="small" @click="saveSummary">保存</el-button>
    </div>

    <el-dialog v-model="noteDialog" :title="activeNote ? activeNote.title : '笔记'" width="640px">
      <template v-if="activeNote">
        <div class="note-meta">{{ activeNote.category }} · {{ activeNote.logDate }}</div>
        <div class="note-content">{{ activeNote.content }}</div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { api } from '../api';
import TodoPanel from '../components/TodoPanel.vue';
import TimelinePanel from '../components/TimelinePanel.vue';
import NotePanel from '../components/NotePanel.vue';
import PomodoroPanel from '../components/PomodoroPanel.vue';

const props = defineProps({ date: { type: String, required: true } });
const emit = defineEmits(['change-date']);

const day = ref({ todos: [], overdue: [], timeline: [], notes: [], pomodoros: [], summary: null });
const activeTab = ref('todo');
const merged = ref(false);
const mood = ref('');
const focus = ref('');
const noteDialog = ref(false);
const activeNote = ref(null);

const tabs = [
  { key: 'todo', label: '待办' },
  { key: 'timeline', label: '时间线' },
  { key: 'note', label: '笔记' },
  { key: 'pomodoro', label: '专注' },
];

function todayStr() {
  const t = new Date();
  const pad = (n) => String(n).padStart(2, '0');
  return `${t.getFullYear()}-${pad(t.getMonth() + 1)}-${pad(t.getDate())}`;
}

const isToday = computed(() => props.date === todayStr());

const dateLabel = computed(() => {
  const [y, m, d] = props.date.split('-').map(Number);
  const weekday = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][new Date(y, m - 1, d).getDay()];
  return `${y} 年 ${m} 月 ${d} 日 · ${weekday}`;
});

function shiftDate(delta) {
  const [y, m, d] = props.date.split('-').map(Number);
  const date = new Date(y, m - 1, d + delta);
  const pad = (n) => String(n).padStart(2, '0');
  emit('change-date', `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`);
}

function goToday() {
  emit('change-date', todayStr());
}

async function load() {
  const data = await api.getDay(props.date);
  day.value = data;
  mood.value = (data.summary && data.summary.mood) || '';
  focus.value = (data.summary && data.summary.tomorrowFocus) || '';
}

async function saveSummary() {
  await api.saveSummary(props.date, { mood: mood.value, tomorrowFocus: focus.value });
  ElMessage.success('已保存');
  load();
}

async function openNote(id) {
  activeNote.value = await api.getNote(id);
  noteDialog.value = true;
}

watch(
  () => props.date,
  () => {
    load();
  },
  { immediate: true }
);
</script>
