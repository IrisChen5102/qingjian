<template>
  <div class="todo-panel">
    <div v-if="day.overdue && day.overdue.length" class="overdue-box">
      <div class="overdue-title">顺延待办 · {{ day.overdue.length }} 项</div>
      <div v-for="t in day.overdue" :key="t.id" class="todo-item">
        <span class="todo-title">{{ t.title }}</span>
        <span class="due-tag overdue">{{ t.dueDate }} 已逾期</span>
        <el-button size="small" text @click="postpone(t)">顺延到明天</el-button>
      </div>
    </div>

    <div class="todo-add">
      <el-input v-model="form.title" size="small" placeholder="今天要做什么？" style="flex: 1; min-width: 180px" @keyup.enter="add" />
      <el-select v-model="form.priority" size="small" style="width: 90px">
        <el-option label="高" value="高" />
        <el-option label="中" value="中" />
        <el-option label="低" value="低" />
      </el-select>
      <el-button type="primary" size="small" @click="add">添加</el-button>
    </div>

    <div v-if="!day.todos || !day.todos.length" class="panel-empty">今天还没有待办，从一条小事开始吧。</div>

    <div v-for="t in day.todos" :key="t.id" class="todo-item" :class="{ done: t.status === '已办' }">
      <el-checkbox :model-value="t.status === '已办'" @change="toggle(t)" />
      <span class="todo-title">{{ t.title }}</span>
      <span class="priority-tag" :class="priorityClass(t.priority)">{{ t.priority }}</span>
      <span v-if="t.note" class="due-tag">{{ t.note }}</span>
      <button class="todo-del" @click="remove(t)">×</button>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { api } from '../api';

const props = defineProps({ date: String, day: Object });
const emit = defineEmits(['refresh']);

const form = reactive({ title: '', priority: '中' });

function priorityClass(p) {
  if (p === '高') return 'high';
  if (p === '低') return 'low';
  return '';
}

async function add() {
  if (!form.title.trim()) return;
  await api.createTodo({ title: form.title.trim(), priority: form.priority, dueDate: props.date, status: '待办' });
  form.title = '';
  emit('refresh');
}

async function toggle(todo) {
  await api.setTodoStatus(todo.id, todo.status === '已办' ? '待办' : '已办');
  emit('refresh');
}

async function remove(todo) {
  await api.deleteTodo(todo.id);
  emit('refresh');
}

async function postpone(todo) {
  const [y, m, d] = props.date.split('-').map(Number);
  const next = new Date(y, m - 1, d + 1);
  const pad = (n) => String(n).padStart(2, '0');
  await api.postponeTodo(todo.id, `${next.getFullYear()}-${pad(next.getMonth() + 1)}-${pad(next.getDate())}`);
  ElMessage.success('已顺延到明天');
  emit('refresh');
}
</script>
