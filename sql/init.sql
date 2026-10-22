-- 青笺 QingJian · 数据库初始化（SQL Server 2022）
IF DB_ID('QingJian') IS NULL
BEGIN
    CREATE DATABASE QingJian;
END
GO

-- 用法：sqlcmd -S localhost -E -C -f 65001 -v DB_PASSWORD="你的密码" -i sql/init.sql
IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'qingjian')
BEGIN
    CREATE LOGIN qingjian WITH PASSWORD = '$(DB_PASSWORD)', CHECK_POLICY = OFF;
END
GO

USE QingJian;
GO

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'qingjian')
BEGIN
    CREATE USER qingjian FOR LOGIN qingjian;
    ALTER ROLE db_owner ADD MEMBER qingjian;
END
GO

IF OBJECT_ID('dbo.note', 'U') IS NULL
CREATE TABLE dbo.note (
    id INT IDENTITY(1,1) PRIMARY KEY,
    log_date DATE NOT NULL,
    category NVARCHAR(10) NOT NULL CONSTRAINT df_note_category DEFAULT N'日记',
    title NVARCHAR(200) NOT NULL,
    content NVARCHAR(MAX) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT df_note_created DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL CONSTRAINT df_note_updated DEFAULT SYSDATETIME()
);
GO

IF OBJECT_ID('dbo.todo', 'U') IS NULL
CREATE TABLE dbo.todo (
    id INT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(100) NOT NULL,
    priority NVARCHAR(10) NOT NULL CONSTRAINT df_todo_priority DEFAULT N'中',
    due_date DATE NOT NULL,
    status NVARCHAR(10) NOT NULL CONSTRAINT df_todo_status DEFAULT N'待办',
    note NVARCHAR(300) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT df_todo_created DEFAULT SYSDATETIME(),
    completed_at DATETIME2 NULL
);
GO

IF OBJECT_ID('dbo.timeline', 'U') IS NULL
CREATE TABLE dbo.timeline (
    id INT IDENTITY(1,1) PRIMARY KEY,
    type NVARCHAR(20) NOT NULL,
    title NVARCHAR(100) NOT NULL,
    detail NVARCHAR(500) NULL,
    url NVARCHAR(500) NULL,
    amount DECIMAL(10,2) NULL,
    tag NVARCHAR(50) NULL,
    happened_at DATETIME2 NOT NULL,
    note_id INT NULL,
    CONSTRAINT fk_timeline_note FOREIGN KEY (note_id) REFERENCES dbo.note(id) ON DELETE SET NULL
);
GO

IF OBJECT_ID('dbo.pomodoro', 'U') IS NULL
CREATE TABLE dbo.pomodoro (
    id INT IDENTITY(1,1) PRIMARY KEY,
    minutes INT NOT NULL,
    started_at DATETIME2 NOT NULL,
    ended_at DATETIME2 NOT NULL,
    note NVARCHAR(200) NULL
);
GO

IF OBJECT_ID('dbo.daily_summary', 'U') IS NULL
CREATE TABLE dbo.daily_summary (
    log_date DATE PRIMARY KEY,
    mood NVARCHAR(10) NULL,
    tomorrow_focus NVARCHAR(200) NULL,
    updated_at DATETIME2 NOT NULL CONSTRAINT df_summary_updated DEFAULT SYSDATETIME()
);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_timeline_time')
    CREATE INDEX ix_timeline_time ON dbo.timeline(happened_at);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_todo_due')
    CREATE INDEX ix_todo_due ON dbo.todo(due_date);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_note_date')
    CREATE INDEX ix_note_date ON dbo.note(log_date);
GO

-- 种子数据（仅空库时插入）
IF NOT EXISTS (SELECT 1 FROM dbo.todo)
BEGIN
    INSERT INTO dbo.note (log_date, category, title, content) VALUES
        ('2026-10-21', N'日记', N'秋天的图书馆', N'下午在图书馆坐了一整个下午。窗外银杏刚开始黄。今天把阅读器的文档写完了，晚上奖励自己看了两集动画。'),
        ('2026-10-22', N'日记', N'实习倒数第二天', N'今天把个人网站和插件的材料都交上了。晚上要把大作业的清单列出来。明天录视频、打包、发邮件。'),
        ('2026-10-22', N'杂记', N'面试复盘：美团二面', N'二面问到缓存穿透和缓存雪崩的处理，回答得有点散。下次要先说结论再展开。另外 JVM 那块要系统复习一遍。');

    INSERT INTO dbo.todo (title, priority, due_date, status, note, completed_at) VALUES
        (N'写实习报告', N'高', '2026-10-22', N'待办', N'先写结构再填内容', NULL),
        (N'录大作业演示视频', N'高', '2026-10-22', N'待办', N'先演示个人网站再演示手账', NULL),
        (N'背 30 个单词', N'中', '2026-10-22', N'已办', N'', '2026-10-22T09:20:00'),
        (N'跑步 3 公里', N'低', '2026-10-22', N'待办', N'', NULL),
        (N'整理简历版本', N'中', '2026-10-20', N'待办', N'国企版和 Java 版分开存', NULL),
        (N'给插件补 README 截图', N'低', '2026-10-21', N'已办', N'', '2026-10-21T21:00:00');

    INSERT INTO dbo.timeline (type, title, detail, url, amount, tag, happened_at, note_id) VALUES
        (N'饮食', N'早餐', N'食堂：豆浆 + 包子', NULL, 6.50, NULL, '2026-10-22T08:10:00', NULL),
        (N'娱乐', N'听歌', N'循环了一上午', N'https://music.163.com/', NULL, N'网易云 · 周杰伦', '2026-10-22T10:00:00', NULL),
        (N'随手记', N'灵感：时间轴左滑删除', N'记录在轴上的条目，左滑可以快速删除，比点进弹窗快。', NULL, NULL, NULL, '2026-10-22T11:05:00', NULL),
        (N'饮食', N'午饭', N'和室友去吃了麻辣烫', NULL, 24.00, NULL, '2026-10-22T12:15:00', NULL),
        (N'娱乐', N'看 B 站', N'刷了两集动画', N'https://www.bilibili.com/', NULL, N'B站 · 新番', '2026-10-22T13:30:00', NULL),
        (N'记账', N'奶茶', N'下午提神', NULL, 15.00, NULL, '2026-10-22T15:20:00', NULL),
        (N'随手记', N'面试复盘已整理', N'详见笔记《面试复盘：美团二面》', NULL, NULL, NULL, '2026-10-22T16:40:00', 3),
        (N'饮食', N'晚饭', N'宿舍楼下小馆', NULL, 18.50, NULL, '2026-10-22T18:30:00', NULL),
        (N'娱乐', N'打游戏', N'两把排位，一胜一负', NULL, NULL, N'Minecraft', '2026-10-21T21:30:00', NULL),
        (N'饮食', N'晚饭', N'图书馆门口面馆', NULL, 20.00, NULL, '2026-10-21T18:00:00', NULL);

    INSERT INTO dbo.pomodoro (minutes, started_at, ended_at, note) VALUES
        (25, '2026-10-22T09:00:00', '2026-10-22T09:25:00', N'写实习报告第一节'),
        (25, '2026-10-22T16:00:00', '2026-10-22T16:25:00', N'收拾大作业清单'),
        (25, '2026-10-21T20:00:00', '2026-10-21T20:25:00', N'阅读器文档收尾');

    INSERT INTO dbo.daily_summary (log_date, mood, tomorrow_focus) VALUES
        ('2026-10-22', N'好', N'录视频 + 打包 zip 发邮件'),
        ('2026-10-21', N'一般', N'把大作业的选题敲定');
END
GO
