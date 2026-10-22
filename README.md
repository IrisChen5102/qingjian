# 今日页（Today's Page）· 个人数字手账

一天一页的个人手账：待办、时间线、笔记、番茄专注、每日便签；月历回看、月度统计；数据可导出迁移。

## 技术栈

Java 17 / Spring Boot 3 / MyBatis-Plus / SQL Server 2022 / Vue 3 / Vite / Element Plus / ECharts

## 快速启动

```bash
sqlcmd -S localhost -E -C -f 65001 -i sql/init.sql   # 初始化数据库（首次）
cd backend && mvn spring-boot:run                     # 后端 :8080
cd frontend && npm install && npm run dev             # 前端 :5173
```

浏览器打开 http://localhost:5173

## 文档

- PRD.md —— 产品需求文档（含需求演进记录）
- TECH_DESIGN.md —— 技术设计文档
- AGENTS.md —— 开发约定
- CHANGELOG.md —— 迭代记录

## 版本

v0.1（需求与设计）→ 后续迭代见 CHANGELOG 与 git tag。
