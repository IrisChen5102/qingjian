# 今日页（Today's Page）· 技术设计文档

- 版本：v0.1
- 日期：2026-10-22

## 1. 技术栈

| 层 | 选型 |
| --- | --- |
| 后端 | Java 17 + Spring Boot 3 + MyBatis-Plus |
| 数据库 | SQL Server 2022 Express（本机） |
| 前端 | Vue 3 + Vite + Element Plus + ECharts |
| 通信 | REST JSON（CORS 允许 localhost:5173） |

## 2. 项目结构

```
qingjian/
├── PRD.md / TECH_DESIGN.md / AGENTS.md / CHANGELOG.md / README.md
├── sql/init.sql            # 建库建表 + 种子数据
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/qingjian/
│       ├── QingJianApplication.java
│       ├── config/          # MyBatis-Plus 分页、CORS
│       ├── entity/          # Todo / Timeline / Note / Pomodoro / DailySummary
│       ├── mapper/          # MyBatis-Plus BaseMapper
│       ├── service/         # 业务逻辑（DayService / StatsService / DataService）
│       ├── controller/      # REST 接口
│       └── dto/             # DayView 聚合、统计返回结构
└── frontend/
    ├── package.json / vite.config.js / index.html
    └── src/
        ├── main.js / App.vue / api.js / style.css
        ├── views/TodayView.vue / CalendarView.vue / StatsView.vue
        └── components/TodoPanel.vue / TimelineAxis.vue / NotePanel.vue / PomodoroPanel.vue
```

## 3. 数据模型

```sql
todo(id INT IDENTITY PK, title NVARCHAR(100), priority NVARCHAR(10),
     due_date DATE, status NVARCHAR(10), note NVARCHAR(300),
     created_at DATETIME2, completed_at DATETIME2 NULL)

note(id INT IDENTITY PK, log_date DATE, category NVARCHAR(10),  -- 日记/杂记
     title NVARCHAR(200), content NVARCHAR(MAX),
     created_at DATETIME2, updated_at DATETIME2)

timeline(id INT IDENTITY PK, type NVARCHAR(20),               -- 饮食/娱乐/记账/随手记
         title NVARCHAR(100), detail NVARCHAR(500), url NVARCHAR(500),
         amount DECIMAL(10,2) NULL, tag NVARCHAR(50),
         happened_at DATETIME2, note_id INT NULL,
         FK note_id -> note(id) ON DELETE SET NULL)

pomodoro(id INT IDENTITY PK, minutes INT, started_at DATETIME2,
         ended_at DATETIME2, note NVARCHAR(200))

daily_summary(log_date DATE PK, mood NVARCHAR(10),
              tomorrow_focus NVARCHAR(200), updated_at DATETIME2)
```

索引：timeline(happened_at)、todo(due_date)、note(log_date)。

## 4. REST API

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | /api/day/{date} | 某日聚合：待办、顺延、时间线、笔记、专注、便签 |
| GET/POST/PUT/DELETE | /api/todos[/{id}] | 待办 CRUD |
| PUT | /api/todos/{id}/status | 完成/取消完成 |
| PUT | /api/todos/{id}/postpone?date= | 顺延 |
| POST/PUT/DELETE | /api/timeline[/{id}] | 时间线条目 CRUD（按日期查询走 /api/day） |
| POST/PUT/DELETE | /api/notes[/{id}] | 笔记 CRUD |
| GET | /api/notes?month=YYYY-MM | 月度笔记（日历清单） |
| POST | /api/pomodoros | 写入专注记录 |
| GET | /api/calendar?month=YYYY-MM | 每天聚合标记 |
| GET | /api/stats?month=YYYY-MM | 月度统计 |
| PUT | /api/day/{date}/summary | 保存心情与明日重点 |
| GET | /api/export | 整库导出 JSON |
| POST | /api/import | 整库导入（覆盖） |

## 5. 前端关键实现

- **时间线（TimelineAxis.vue）**：固定高度纵向轴（0-24h 映射为像素），左右两条批注列，条目按分钟定位（absolute top），奇数/偶数条目左右交替；在轴上点击换算成时间后打开快速记录弹窗；轴顶/底留白避免遮挡
- **番茄钟（PomodoroPanel.vue）**：SVG 圆环（circle stroke-dasharray / stroke-dashoffset 驱动进度）；预设 15/25/45 + 自定义输入；结束回调 POST /api/pomodoros
- **今日页（TodayView.vue）**：四标签（el-tabs）+「合并视图」开关（v-show 平铺）；日期导航切换 day 数据
- **样式系统**：CSS 变量集中管理（纸底 #eef5fb、卡片 #f8fbfe、墨色 #33506b、强调 #7aa6c2、警示 #c96f6f）；标题使用楷体（KaiTi）；卡片圆角不规则化 + 柔和纸感阴影营造手绘感
- **图表（StatsView.vue）**：ECharts 柱状/饼图/热力

## 6. 导入导出格式

导出对象：`{ exportedAt, todos, notes, timeline, pomodoros, summaries }`（数组均为原表行）。
导入为覆盖式：清空后按导出结构恢复；主键自增重新生成（保留数据内容，不保留自增 id 关系时对 timeline.note_id 做映射）。

## 7. 运行方式

```bash
# 数据库初始化（本机 SQL Server）
sqlcmd -S localhost -E -C -f 65001 -i sql/init.sql

# 后端
cd backend && mvn spring-boot:run

# 前端
cd frontend && npm install && npm run dev
```

## 8. 风险与对策

- 时间线交互是最大不确定项：先实现基础版（轴 + 两侧卡片 + 点击添加），再打磨动画与样式
- SQL Server + MyBatis-Plus 分页方言：使用 DbType.SQL_SERVER2005 分页
- 导入导出的自增主键与外键映射：导入时按顺序插入并重映射 note_id
- 字体与手绘感依赖系统字体：楷体在 Windows 自带；无则不降级为衬线体
