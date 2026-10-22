<template>
  <div class="timeline-wrap">
    <div ref="axisRef" class="axis" :style="{ height: axisHeight + 'px' }" @click="onAxisClick">
      <div class="axis-line"></div>
      <div v-for="hour in hourMarks" :key="hour" class="hour-tick" :style="{ top: hour * HOUR_PX + 'px' }">
        <span class="hour-label">{{ String(hour).padStart(2, '0') }}:00</span>
      </div>
      <div
        v-for="(item, index) in items"
        :key="item.id"
        class="event"
        :class="index % 2 === 0 ? 'left' : 'right'"
        :style="{ top: topOf(item) + 'px' }"
        @click.stop
      >
        <div class="event-card">
          <div class="event-head">
            <span class="event-icon">{{ iconOf(item.type) }}</span>
            <span class="event-time">{{ timeOf(item) }}</span>
            <button class="event-del" title="删除" @click="remove(item)">×</button>
          </div>
          <div class="event-title">{{ item.title }}</div>
          <div v-if="item.detail" class="event-detail">{{ item.detail }}</div>
          <div class="event-foot">
            <span v-if="item.amount != null" class="event-amount">¥{{ Number(item.amount).toFixed(2) }}</span>
            <span v-if="item.tag" class="event-tag">{{ item.tag }}</span>
            <a v-if="item.url" class="event-link" :href="item.url" target="_blank" rel="noopener">链接</a>
            <a v-if="item.noteId" class="event-note-link" @click="$emit('open-note', item.noteId)">关联笔记</a>
          </div>
        </div>
        <div class="event-dot"></div>
      </div>
    </div>
    <p class="axis-hint">在轴上点一下，随时记一笔</p>

    <el-dialog v-model="dialog" title="记一笔" width="480px">
      <el-form label-width="70px" label-position="left">
        <el-form-item label="时间">
          <el-time-picker v-model="form.time" format="HH:mm" value-format="HH:mm" placeholder="选择时间" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type" size="small">
            <el-radio-button value="饮食">饮食</el-radio-button>
            <el-radio-button value="娱乐">娱乐</el-radio-button>
            <el-radio-button value="记账">记账</el-radio-button>
            <el-radio-button value="随手记">随手记</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="做了什么？" />
        </el-form-item>
        <el-form-item label="详情">
          <el-input v-model="form.detail" type="textarea" :rows="2" placeholder="补充两句（可选）" />
        </el-form-item>
        <el-form-item v-if="form.type === '娱乐'" label="链接">
          <el-input v-model="form.url" placeholder="https://...（可选）" />
        </el-form-item>
        <el-form-item v-if="form.type === '娱乐'" label="标签">
          <el-input v-model="form.tag" placeholder="歌手 / 平台 / 游戏（可选）" />
        </el-form-item>
        <el-form-item v-if="form.type === '记账'" label="金额">
          <el-input-number v-model="form.amount" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="关联笔记">
          <el-select v-model="form.noteId" clearable placeholder="可关联当天笔记" style="width: 100%">
            <el-option v-for="note in (day.notes || [])" :key="note.id" :label="note.title" :value="note.id" />
          </el-select>
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
import { computed, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { api } from '../api';

const props = defineProps({ date: String, day: Object });
const emit = defineEmits(['refresh', 'open-note']);

const HOUR_PX = 56;
const axisHeight = 24 * HOUR_PX;
const hourMarks = Array.from({ length: 24 }, (_, i) => i);
const axisRef = ref(null);
const dialog = ref(false);

const items = computed(() => props.day.timeline || []);

const form = reactive({
  time: '12:00',
  type: '随手记',
  title: '',
  detail: '',
  url: '',
  tag: '',
  amount: null,
  noteId: null,
});

function timeOf(item) {
  if (!item.happenedAt) return '';
  return String(item.happenedAt).slice(11, 16);
}

function topOf(item) {
  const time = timeOf(item);
  if (!time) return 0;
  const [h, m] = time.split(':').map(Number);
  return ((h * 60 + m) / 60) * HOUR_PX;
}

function iconOf(type) {
  return { 饮食: '🍚', 娱乐: '🎮', 记账: '💰', 随手记: '✍️' }[type] || '·';
}

function onAxisClick(event) {
  const rect = axisRef.value.getBoundingClientRect();
  const y = Math.max(0, Math.min(event.clientY - rect.top, axisHeight - 1));
  const minutes = Math.round((y / HOUR_PX) * 60 / 15) * 15;
  const clamped = Math.min(minutes, 23 * 60 + 45);
  const pad = (n) => String(n).padStart(2, '0');
  form.time = `${pad(Math.floor(clamped / 60))}:${pad(clamped % 60)}`;
  form.type = '随手记';
  form.title = '';
  form.detail = '';
  form.url = '';
  form.tag = '';
  form.amount = null;
  form.noteId = null;
  dialog.value = true;
}

async function save() {
  if (!form.title.trim()) {
    ElMessage.warning('写一个标题吧');
    return;
  }
  await api.createTimeline({
    type: form.type,
    title: form.title.trim(),
    detail: form.detail || null,
    url: form.url || null,
    tag: form.tag || null,
    amount: form.amount,
    noteId: form.noteId,
    happenedAt: `${props.date}T${form.time}:00`,
  });
  dialog.value = false;
  ElMessage.success('已记下');
  emit('refresh');
}

async function remove(item) {
  try {
    await ElMessageBox.confirm('删除这条记录？', '提示', { type: 'warning' });
  } catch (e) {
    return;
  }
  await api.deleteTimeline(item.id);
  emit('refresh');
}
</script>
