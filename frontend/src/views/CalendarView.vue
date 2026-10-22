<template>
  <section class="calendar-view">
    <div class="month-bar">
      <button class="date-arrow" @click="shiftMonth(-1)">‹</button>
      <span class="month-label">{{ monthLabel }}</span>
      <button class="date-arrow" @click="shiftMonth(1)">›</button>
      <div class="date-spacer"></div>
      <div class="view-switch">
        <button :class="{ active: mode === 'grid' }" @click="mode = 'grid'">月历</button>
        <button :class="{ active: mode === 'list' }" @click="mode = 'list'">清单</button>
      </div>
    </div>

    <div v-if="mode === 'grid'" class="cal-grid">
      <div v-for="w in weekdays" :key="w" class="cal-weekday">{{ w }}</div>
      <div
        v-for="cell in cells"
        :key="cell.key"
        class="cal-cell"
        :class="{ empty: !cell.date, today: cell.date === todayStr, active: cell.hasAny }"
        @click="cell.date && $emit('open-day', cell.date)"
      >
        <template v-if="cell.date">
          <div class="cal-num">{{ cell.day }}</div>
          <div class="cal-marks">
            <span v-if="cell.hasNote" class="dot note" title="有笔记"></span>
            <span v-if="cell.timelineCount" class="dot time" title="有记录"></span>
            <span v-if="cell.pomodoroCount" class="pill">🍅{{ cell.pomodoroCount }}</span>
            <span v-if="cell.todoTotal > cell.todoDone" class="pill">待办{{ cell.todoTotal - cell.todoDone }}</span>
          </div>
          <div v-if="cell.expense" class="cal-expense">¥{{ Number(cell.expense).toFixed(0) }}</div>
        </template>
      </div>
    </div>

    <div v-else class="cal-list">
      <div v-if="!noteGroups.length" class="panel-empty">这个月还没有笔记。</div>
      <div v-for="group in noteGroups" :key="group.date" class="list-day" @click="$emit('open-day', group.date)">
        <div class="list-date">
          {{ formatDate(group.date) }}
          <span v-if="cellOf(group.date)" class="list-meta">
            记录 {{ cellOf(group.date).timelineCount }} · 🍅 {{ cellOf(group.date).pomodoroCount }}
          </span>
        </div>
        <div
          v-for="note in group.notes"
          :key="note.id"
          class="list-note"
          @click.stop="$emit('open-editor', { date: note.logDate, id: note.id })"
        >
          <span class="note-cat" :class="note.category === '日记' ? 'diary' : 'misc'">{{ note.category }}</span>
          <span class="list-note-title">{{ note.title }}</span>
        </div>
      </div>
    </div>

  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { api } from '../api';

defineEmits(['open-day', 'open-editor']);

const now = new Date();
const pad = (n) => String(n).padStart(2, '0');
const month = ref(`${now.getFullYear()}-${pad(now.getMonth() + 1)}`);
const mode = ref('grid');
const calendar = ref([]);
const notes = ref([]);

const weekdays = ['一', '二', '三', '四', '五', '六', '日'];
const todayStr = (() => {
  const t = new Date();
  return `${t.getFullYear()}-${pad(t.getMonth() + 1)}-${pad(t.getDate())}`;
})();

const monthLabel = computed(() => {
  const [y, m] = month.value.split('-').map(Number);
  return `${y} 年 ${m} 月`;
});

const cellMap = computed(() => {
  const map = {};
  calendar.value.forEach((c) => {
    map[c.date] = c;
  });
  return map;
});

const cells = computed(() => {
  const [y, m] = month.value.split('-').map(Number);
  const first = new Date(y, m - 1, 1);
  const daysInMonth = new Date(y, m, 0).getDate();
  const firstWeekday = (first.getDay() + 6) % 7;
  const list = [];
  for (let i = 0; i < firstWeekday; i++) {
    list.push({ key: `blank-${i}`, date: null });
  }
  for (let d = 1; d <= daysInMonth; d++) {
    const date = `${y}-${pad(m)}-${pad(d)}`;
    const c = cellMap.value[date] || {
      todoTotal: 0,
      todoDone: 0,
      timelineCount: 0,
      pomodoroCount: 0,
      expense: null,
      hasNote: false,
    };
    list.push({
      key: date,
      date,
      day: d,
      ...c,
      hasAny: !!(c.todoTotal || c.timelineCount || c.pomodoroCount || c.hasNote || c.expense),
    });
  }
  return list;
});

const noteGroups = computed(() => {
  const groups = {};
  notes.value.forEach((n) => {
    if (!groups[n.logDate]) groups[n.logDate] = { date: n.logDate, notes: [] };
    groups[n.logDate].notes.push(n);
  });
  return Object.values(groups).sort((a, b) => (a.date < b.date ? 1 : -1));
});

function cellOf(date) {
  return cellMap.value[date];
}

function formatDate(date) {
  const [, m, d] = date.split('-').map(Number);
  return `${m} 月 ${d} 日`;
}

function shiftMonth(delta) {
  const [y, m] = month.value.split('-').map(Number);
  const date = new Date(y, m - 1 + delta, 1);
  month.value = `${date.getFullYear()}-${pad(date.getMonth() + 1)}`;
}

async function load() {
  calendar.value = await api.getCalendar(month.value);
  notes.value = await api.listNotes(month.value);
}

watch(month, load, { immediate: true });
</script>
