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
        <div class="pomo-label">
          {{ pomoStore.running ? '专注中…（切页也不会中断）' : pomoStore.finished ? '完成一轮，休息一下' : '准备好了就点开始' }}
        </div>
      </div>
    </div>

    <div class="pomo-controls">
      <el-radio-group :model-value="pomoStore.preset" size="small" :disabled="pomoStore.running" @change="setPreset">
        <el-radio-button :value="15">15 分</el-radio-button>
        <el-radio-button :value="25">25 分</el-radio-button>
        <el-radio-button :value="45">45 分</el-radio-button>
        <el-radio-button :value="0">自定义</el-radio-button>
      </el-radio-group>
      <el-input-number
        v-if="pomoStore.preset === 0"
        :model-value="pomoStore.custom"
        :min="1"
        :max="180"
        size="small"
        :disabled="pomoStore.running"
        @change="onCustom"
      />
      <el-button type="primary" size="small" @click="toggleTimer">{{ pomoStore.running ? '暂停' : '开始' }}</el-button>
      <el-button size="small" :disabled="pomoStore.remaining === pomoStore.total && !pomoStore.startedAt" @click="resetTimer">
        重置
      </el-button>
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
import { computed } from 'vue';
import { pomoStore, resetTimer, setPreset, toggleTimer } from '../stores/pomodoro';

defineProps({ date: String, day: Object });

const circumference = 2 * Math.PI * 104;

const mmss = computed(() => {
  const m = Math.floor(pomoStore.remaining / 60);
  const s = pomoStore.remaining % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
});

const dashOffset = computed(() => {
  if (!pomoStore.total) return circumference;
  return circumference * (1 - pomoStore.remaining / pomoStore.total);
});

function onCustom(value) {
  pomoStore.custom = value || 1;
  if (!pomoStore.running) {
    resetTimer();
  }
}
</script>
