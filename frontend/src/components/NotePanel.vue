<template>
  <div class="note-panel">
    <div class="note-actions">
      <el-button type="primary" size="small" @click="$emit('open-editor', { date, id: null })">写一条笔记</el-button>
    </div>

    <div v-if="!day.notes || !day.notes.length" class="panel-empty">今天还没有笔记，留白也是一种记录。</div>

    <div v-for="note in day.notes" :key="note.id" class="note-card" @click="$emit('open-editor', { date, id: note.id })">
      <div class="note-card-head">
        <span class="note-cat" :class="note.category === '日记' ? 'diary' : 'misc'">{{ note.category }}</span>
        <span class="note-title">{{ note.title }}</span>
        <button class="note-del" @click.stop="remove(note)">×</button>
      </div>
      <div class="note-excerpt">{{ excerpt(note.content) }}</div>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox } from 'element-plus';
import { api } from '../api';

const props = defineProps({ date: String, day: Object });
const emit = defineEmits(['refresh', 'open-editor']);

function excerpt(content) {
  if (!content) return '';
  return content.length > 80 ? content.slice(0, 80) + '…' : content;
}

async function remove(note) {
  try {
    await ElMessageBox.confirm(`删除笔记「${note.title}」？`, '提示', { type: 'warning' });
  } catch (e) {
    return;
  }
  await api.deleteNote(note.id);
  emit('refresh');
}
</script>
