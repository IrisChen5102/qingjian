<template>
  <div class="pomodoro-panel">
    <div class="pomodoro-stage">
      <svg class="pomo-ring" viewBox="0 0 240 240">
        <circle class="ring-bg" cx="120" cy="120" r="104" />
        <circle
          class="ring-fg"
          cx="120"
          cy="120"
          r="104"
          :stroke-dasharray="circumference"
          :stroke-dashoffset="dashOffset"
          transform="rotate(-90 120 120)"
        />
      </svg>
      <div class="pomo-center">
        <div class="pomo-tomato">🍅</div>
        <div class="pomo-time">{{ mmss }}</div>
        <div class="pomo-label">{{ running ? '专注中…' : finished ? '完成一轮，休息一下' : '准备好了就点开始' }}</div>
      </div>
    </div>

    <div class="pomo-controls">
      <el-radio-group v-model="preset" size="small" :disabled="running">
        <el-radio-button :value="15">15 分</el-radio-button>
        <el-radio-button :value="25">25 分</el-radio-button>
        <el-radio-button :value="45">45 分</el-radio-button>
        <el-radio-button :value="0">自定义</el-radio-button>
      </el-radio-group>
      <el-input-number v-if="preset === 0" v-model="customMinutes" :min="1" :max="180" size="small" :disabled="running" />
      <el-button type="primary" size="small" @click="toggle">{{ running ? '暂停' : '开始' }}</el-button>
      <el-button size="small" :disabled="!elapsedSeconds" @click="reset">重置</el-button>
    </div>

    <div class="pomo-history">
      <div class="panel-sub">今天的专注</div>
      <div v-if="!day.pomodoros || !day.pomodoros.length" class="panel-empty">今天还没有专注记录</div>
      <div v-for="p in day.pomodoros" :key="p.id" class="pomo-item">
        <span>{{ String(p.startedAt).slice(11, 16) }}</span>
        <span>{{ p.minutes }} 分钟</span>
        <span class="pomo-note">{{ p.note }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { api } from '../api';

const props = defineProps({ date: String, day: Object });
const emit = defineEmits(['refresh']);

const preset = ref(25);
const customMinutes = ref(10);
const running = ref(false);
const finished = ref(false);
const totalSeconds = ref(25 * 60);
const remaining = ref(25 * 60);
const startedAt = ref(null);
let timer = null;

const circumference = 2 * Math.PI * 104;

const minutes = computed(() => (preset.value === 0 ? customMinutes.value || 1 : preset.value));
const elapsedSeconds = computed(() => totalSeconds.value - remaining.value);

const dashOffset = computed(() => {
  if (!totalSeconds.value) return circumference;
  const ratio = remaining.value / totalSeconds.value;
  return circumference * (1 - ratio);
});

const mmss = computed(() => {
  const m = Math.floor(remaining.value / 60);
  const s = remaining.value % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
});

watch([preset, customMinutes], () => {
  if (running.value) return;
  remaining.value = minutes.value * 60;
  totalSeconds.value = minutes.value * 60;
  finished.value = false;
});

function tick() {
  remaining.value -= 1;
  if (remaining.value <= 0) {
    clearInterval(timer);
    timer = null;
    running.value = false;
    finished.value = true;
    finish();
  }
}

async function finish() {
  const usedMinutes = Math.round(totalSeconds.value / 60);
  const end = new Date();
  const start = startedAt.value || new Date(end.getTime() - usedMinutes * 60000);
  try {
    await api.createPomodoro({
      minutes: usedMinutes,
      startedAt: start.toISOString().slice(0, 19),
      endedAt: end.toISOString().slice(0, 19),
      note: '',
    });
    ElMessage.success(`完成一轮 ${usedMinutes} 分钟专注 🍅`);
    emit('refresh');
  } catch (e) {
    ElMessage.error('记录保存失败：' + e.message);
  }
  startedAt.value = null;
  remaining.value = minutes.value * 60;
  totalSeconds.value = minutes.value * 60;
}

function toggle() {
  if (running.value) {
    clearInterval(timer);
    timer = null;
    running.value = false;
    return;
  }
  if (!startedAt.value) {
    startedAt.value = new Date();
  }
  running.value = true;
  finished.value = false;
  timer = setInterval(tick, 1000);
}

function reset() {
  clearInterval(timer);
  timer = null;
  running.value = false;
  finished.value = false;
  startedAt.value = null;
  remaining.value = minutes.value * 60;
  totalSeconds.value = minutes.value * 60;
}

watch(
  () => props.date,
  () => reset()
);

onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});
</script>
