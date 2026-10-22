<template>
  <div class="note-panel">
    <div class="note-actions">
      <el-button type="primary" size="small" @click="openEdit()">写一条笔记</el-button>
    </div>

    <div v-if="!day.notes || !day.notes.length" class="panel-empty">今天还没有笔记，留白也是一种记录。</div>

    <div v-for="note in day.notes" :key="note.id" class="note-card" @click="openEdit(note)">
      <div class="note-card-head">
        <span class="note-cat" :class="note.category === '日记' ? 'diary' : 'misc'">{{ note.category }}</span>
        <span class="note-title">{{ note.title }}</span>
        <button class="note-del" @click.stop="remove(note)">×</button>
      </div>
      <div class="note-excerpt">{{ excerpt(note.content) }}</div>
    </div>

    <el-dialog v-model="dialog" :title="editingId ? '编辑笔记' : '新建笔记'" width="680px">
      <el-form label-width="60px" label-position="left">
        <el-form-item label="分类">
          <el-radio-group v-model="form.category" size="small">
            <el-radio-button value="日记">日记</el-radio-button>
            <el-radio-button value="杂记">杂记</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="给这条笔记起个名字" />
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="form.content" type="textarea" :rows="12" placeholder="写下你想记住的…" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { api } from '../api';

const props = defineProps({ date: String, day: Object });
const emit = defineEmits(['refresh']);

const dialog = ref(false);
const editingId = ref(null);
const form = reactive({ category: '日记', title: '', content: '' });

function excerpt(content) {
  if (!content) return '';
  return content.length > 80 ? content.slice(0, 80) + '…' : content;
}

function openEdit(note) {
  if (note) {
    editingId.value = note.id;
    form.category = note.category;
    form.title = note.title;
    form.content = note.content || '';
  } else {
    editingId.value = null;
    form.category = '日记';
    form.title = '';
    form.content = '';
  }
  dialog.value = true;
}

async function save() {
  if (!form.title.trim()) {
    ElMessage.warning('笔记需要一个标题');
    return;
  }
  const payload = {
    logDate: props.date,
    category: form.category,
    title: form.title.trim(),
    content: form.content,
  };
  if (editingId.value) {
    await api.updateNote(editingId.value, payload);
  } else {
    await api.createNote(payload);
  }
  dialog.value = false;
  ElMessage.success('已保存');
  emit('refresh');
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
