# AGENTS.md · 今日页（Today's Page）

## 项目是什么

个人数字手账（Web 全栈）：一天一页，包含待办、时间线、笔记、专注（番茄钟）、每日便签；日历回看、统计出片；数据可导出/导入。单用户、本地运行。

## 怎么跑

```bash
# 1) 初始化数据库（首次或重建）
sqlcmd -S localhost -E -C -f 65001 -i sql/init.sql

# 2) 后端（端口 8080）
cd backend && mvn spring-boot:run

# 3) 前端（端口 5173）
cd frontend && npm install && npm run dev
```

打开 http://localhost:5173

## 硬性约定

- 技术栈：Spring Boot 3 + MyBatis-Plus + SQL Server + Vue 3 + Vite + Element Plus + ECharts；不引入其他后端框架或数据库
- 目录：后端只动 `backend/src/main/java/com/qingjian`；前端只动 `frontend/src`；文档放根目录
- 数据：仅本地 SQL Server（库名 QingJian，应用登录名 qingjian）；不联网、不上传任何数据
- 界面文案全部中文；视觉遵循淡蓝水彩 + 手绘感（见 TECH_DESIGN 样式变量）
- 接口统一 REST + JSON，返回结构直接为数据本体或 { message } 错误
- 每次可运行的功能改动都执行一次 git 提交，并打 tag（v0.x），推送到 GitHub

## 测试要求

每次改动后自测：

1. 新增待办 → 完成 → 顺延
2. 时间线点击轴添加记录 → 两侧展示 → 关联笔记跳转
3. 笔记新建 → 当日标签可见 → 日历清单可打开
4. 番茄倒计时（预设/自定义）→ 结束后记录出现
5. 日历两种视图、统计图表
6. 导出 → 导入还原
7. 刷新与重启后数据仍在

## 变更流程

需求变化先改 PRD / TECH_DESIGN，再动代码；大改动前先提交当前版本并打 tag。
