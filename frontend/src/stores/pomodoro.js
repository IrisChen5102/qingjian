import { reactive } from 'vue';
import { api } from '../api';

/**
 * 番茄钟放在模块级状态里：
 * 与组件生命周期解耦，切换页面/日期不会打断计时。
 * 只有「主动停止 / 到时间 / 关闭网页」才会结束。
 */
export const pomoStore = reactive({
  preset: 25,
  custom: 10,
  running: false,
  finished: false,
  total: 25 * 60,
  remaining: 25 * 60,
  startedAt: null,
  completed: null,
  saved: false,
  showToast: false,
  openPomodoro: false,
});

export function pomoMinutes() {
  return pomoStore.preset === 0 ? pomoStore.custom || 1 : pomoStore.preset;
}

let timerId = null;

function formatLocal(date) {
  const p = (n) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${p(date.getMonth() + 1)}-${p(date.getDate())}T${p(date.getHours())}:${p(date.getMinutes())}:${p(date.getSeconds())}`;
}

export function resetTimer() {
  if (timerId) {
    clearInterval(timerId);
    timerId = null;
  }
  pomoStore.running = false;
  pomoStore.finished = false;
  pomoStore.saved = false;
  pomoStore.startedAt = null;
  pomoStore.total = pomoMinutes() * 60;
  pomoStore.remaining = pomoStore.total;
}

export function setPreset(value) {
  if (pomoStore.running) return;
  pomoStore.preset = value;
  resetTimer();
}

export function startTimer() {
  if (pomoStore.running) return;
  if (pomoStore.finished || pomoStore.remaining <= 0) {
    resetTimer();
  }
  if (!pomoStore.startedAt) {
    pomoStore.startedAt = formatLocal(new Date());
  }
  pomoStore.running = true;
  pomoStore.finished = false;
  if (!timerId) {
    timerId = setInterval(() => {
      if (!pomoStore.running) return;
      pomoStore.remaining -= 1;
      if (pomoStore.remaining <= 0) {
        pomoStore.remaining = 0;
        completeTimer();
      }
    }, 1000);
  }
}

export function pauseTimer() {
  pomoStore.running = false;
}

export function toggleTimer() {
  if (pomoStore.running) {
    pauseTimer();
  } else {
    startTimer();
  }
}

function completeTimer() {
  if (timerId) {
    clearInterval(timerId);
    timerId = null;
  }
  pomoStore.running = false;
  pomoStore.finished = true;
  pomoStore.saved = false;
  pomoStore.completed = {
    minutes: Math.round(pomoStore.total / 60),
    startedAt: pomoStore.startedAt || formatLocal(new Date(Date.now() - pomoStore.total * 1000)),
    endedAt: formatLocal(new Date()),
  };
  pomoStore.startedAt = null;
  pomoStore.showToast = true;
}

/** 保存本轮记录（备注可为空）；保存成功后重置为下一轮 */
export async function savePomodoroRecord(note) {
  if (!pomoStore.completed || pomoStore.saved) {
    pomoStore.showToast = false;
    return;
  }
  await api.createPomodoro({
    minutes: pomoStore.completed.minutes,
    startedAt: pomoStore.completed.startedAt,
    endedAt: pomoStore.completed.endedAt,
    note: note || '',
  });
  pomoStore.saved = true;
  pomoStore.showToast = false;
  pomoStore.completed = null;
  resetTimer();
}
