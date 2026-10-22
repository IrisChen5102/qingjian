<template>
  <transition name="toast">
    <div v-if="pomoStore.showToast" class="pomo-toast">
      <div class="toast-head">🍅 完成一轮 {{ pomoStore.completed ? pomoStore.completed.minutes : '' }} 分钟专注</div>
      <el-input v-model="note" size="small" placeholder="这次在做什么？（选填）" />
      <div class="toast-actions">
        <a class="toast-link" @click="goPomodoro">去专注页</a>
        <span class="toast-spacer"></span>
        <el-button size="small" text @click="save('')">不写备注</el-button>
        <el-button size="small" type="primary" @click="save(note)">保存</el-button>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { pomoStore, savePomodoroRecord } from '../stores/pomodoro';

const note = ref('');

async function save(text) {
  try {
    await savePomodoroRecord(text);
    note.value = '';
    ElMessage.success('已记录这轮专注');
  } catch (e) {
    ElMessage.error('保存失败：' + e.message);
  }
}

function goPomodoro() {
  pomoStore.openPomodoro = true;
}
</script>
