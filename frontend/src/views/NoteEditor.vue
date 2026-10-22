<template>
  <section class="note-editor">
    <div class="editor-bar">
      <button class="date-arrow" title="返回" @click="$emit('back')">←</button>
      <span class="editor-title">{{ isNew ? '新笔记' : '编辑笔记' }} · {{ logDate }}</span>
      <div class="date-spacer"></div>
      <el-radio-group v-model="category" size="small">
        <el-radio-button value="日记">日记</el-radio-button>
        <el-radio-button value="杂记">杂记</el-radio-button>
      </el-radio-group>
      <span class="font-ctrl">
        <button class="font-btn" @click="adjustFont(-1)">A−</button>
        <button class="font-btn" @click="adjustFont(1)">A＋</button>
      </span>
      <el-button v-if="!isNew" size="small" text @click="remove">删除</el-button>
      <el-button type="primary" size="small" @click="save">保存</el-button>
    </div>

    <div class="editor-paper">
      <input v-model="title" class="editor-title-input" placeholder="给这条笔记起个名字" />
      <textarea
        v-model="content"
        class="editor-body"
        :style="{ fontSize: fontSize + 'px' }"
        placeholder="写下你想记住的…"
      ></textarea>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { api } from '../api';

const props = defineProps({
  logDate: { type: String, required: true },
  noteId: { type: Number, default: null },
});
const emit = defineEmits(['back']);

const isNew = computed(() => !props.noteId);
const category = ref('日记');
const title = ref('');
const content = ref('');
const fontSize = ref(Number(localStorage.getItem('noteFontSize')) || 16);

onMounted(async () => {
  if (props.noteId) {
    const note = await api.getNote(props.noteId);
    category.value = note.category;
    title.value = note.title;
    content.value = note.content || '';
  }
});

function adjustFont(delta) {
  fontSize.value = Math.min(24, Math.max(13, fontSize.value + delta));
  localStorage.setItem('noteFontSize', String(fontSize.value));
}

async function save() {
  if (!title.value.trim()) {
    ElMessage.warning('给笔记起个名字吧');
    return;
  }
  const payload = {
    logDate: props.logDate,
    category: category.value,
    title: title.value.trim(),
    content: content.value,
  };
  if (props.noteId) {
    await api.updateNote(props.noteId, payload);
  } else {
    await api.createNote(payload);
  }
  ElMessage.success('已保存');
  emit('back');
}

async function remove() {
  try {
    await ElMessageBox.confirm('删除这条笔记？', '提示', { type: 'warning' });
  } catch (e) {
    return;
  }
  await api.deleteNote(props.noteId);
  ElMessage.success('已删除');
  emit('back');
}
</script>
