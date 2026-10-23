import { createApp } from 'vue';
import ElementPlus, { ElMessage } from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import 'element-plus/dist/index.css';
import App from './App.vue';
import './style.css';

// 接口失败时给出可见提示，避免操作"静默无响应"
window.addEventListener('unhandledrejection', (event) => {
  const reason = event.reason;
  ElMessage.error('操作失败：' + (reason && reason.message ? reason.message : reason));
});

createApp(App).use(ElementPlus, { locale: zhCn }).mount('#app');
