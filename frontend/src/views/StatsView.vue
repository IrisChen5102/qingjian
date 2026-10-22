<template>
  <section class="stats-view">
    <div class="month-bar">
      <button class="date-arrow" @click="shiftMonth(-1)">‹</button>
      <span class="month-label">{{ monthLabel }}</span>
      <button class="date-arrow" @click="shiftMonth(1)">›</button>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <h4>每日支出</h4>
        <div ref="expenseEl" class="chart"></div>
      </div>
      <div class="stat-card">
        <h4>记录类型分布</h4>
        <div ref="typeEl" class="chart"></div>
      </div>
      <div class="stat-card wide">
        <h4>每日专注（番茄）</h4>
        <div ref="pomoEl" class="chart"></div>
      </div>
      <div class="stat-card">
        <h4>待办完成率</h4>
        <div class="rate-wrap">
          <el-progress type="dashboard" :percentage="rate" :width="150" />
        </div>
        <p class="stat-note">{{ stats.todoDone || 0 }} / {{ stats.todoTotal || 0 }} 项</p>
      </div>
      <div class="stat-card">
        <h4>本月 Top 标签</h4>
        <ol v-if="stats.topTags && stats.topTags.length" class="tag-rank">
          <li v-for="(t, i) in stats.topTags" :key="t.tag">
            <span>{{ i + 1 }}. {{ t.tag }}</span><b>{{ t.count }}</b>
          </li>
        </ol>
        <p v-else class="panel-empty">还没有带标签的娱乐记录</p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue';
import * as echarts from 'echarts';
import { api } from '../api';

const now = new Date();
const pad = (n) => String(n).padStart(2, '0');
const month = ref(`${now.getFullYear()}-${pad(now.getMonth() + 1)}`);
const stats = ref({});

const expenseEl = ref(null);
const typeEl = ref(null);
const pomoEl = ref(null);
let charts = [];

const rate = computed(() => {
  const total = stats.value.todoTotal || 0;
  if (!total) return 0;
  return Math.round(((stats.value.todoDone || 0) / total) * 100);
});

const monthLabel = computed(() => {
  const [y, m] = month.value.split('-').map(Number);
  return `${y} 年 ${m} 月`;
});

function shiftMonth(delta) {
  const [y, m] = month.value.split('-').map(Number);
  const date = new Date(y, m - 1 + delta, 1);
  month.value = `${date.getFullYear()}-${pad(date.getMonth() + 1)}`;
}

function daysOfMonth() {
  const [y, m] = month.value.split('-').map(Number);
  return new Date(y, m, 0).getDate();
}

function renderCharts() {
  charts.forEach((c) => c.dispose());
  charts = [];
  const days = Array.from({ length: daysOfMonth() }, (_, i) => i + 1);
  const expenseMap = {};
  (stats.value.expenseByDay || []).forEach((d) => {
    expenseMap[Number(String(d.date).slice(8, 10))] = Number(d.amount);
  });
  const pomoMap = {};
  (stats.value.pomodoroByDay || []).forEach((d) => {
    pomoMap[Number(String(d.date).slice(8, 10))] = d.count;
  });

  const expenseChart = echarts.init(expenseEl.value);
  expenseChart.setOption({
    grid: { left: 40, right: 16, top: 20, bottom: 30 },
    tooltip: {},
    xAxis: { type: 'category', data: days, axisLabel: { fontSize: 10 } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#e6eff6' } } },
    series: [
      {
        type: 'bar',
        data: days.map((d) => expenseMap[d] || 0),
        itemStyle: { color: '#7aa6c2', borderRadius: 4 },
        barMaxWidth: 10,
      },
    ],
  });
  charts.push(expenseChart);

  const typeChart = echarts.init(typeEl.value);
  typeChart.setOption({
    tooltip: { trigger: 'item' },
    color: ['#7aa6c2', '#e6b49b', '#9dbfa9', '#c9b6d9'],
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        itemStyle: { borderRadius: 8, borderColor: '#fbfdff', borderWidth: 2 },
        data: (stats.value.typeCounts || []).map((t) => ({ name: t.type, value: t.count })),
      },
    ],
  });
  charts.push(typeChart);

  const pomoChart = echarts.init(pomoEl.value);
  pomoChart.setOption({
    grid: { left: 40, right: 16, top: 20, bottom: 30 },
    tooltip: {},
    xAxis: { type: 'category', data: days, axisLabel: { fontSize: 10 } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#e6eff6' } } },
    series: [
      {
        type: 'bar',
        data: days.map((d) => pomoMap[d] || 0),
        itemStyle: { color: '#9dbfa9', borderRadius: 4 },
        barMaxWidth: 10,
      },
    ],
  });
  charts.push(pomoChart);
}

async function load() {
  stats.value = await api.getStats(month.value);
  await nextTick();
  if (expenseEl.value) renderCharts();
}

function onResize() {
  charts.forEach((c) => c.resize());
}

watch(month, load, { immediate: true });

window.addEventListener('resize', onResize);
onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize);
  charts.forEach((c) => c.dispose());
});
</script>
