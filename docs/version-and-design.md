# Sisyphus — 版本与设计文档

> 最后更新：2026-05-29

---

## 1. 项目概述

**Sisyphus** 是一款通用学习可视化与智能推荐 Android 应用，帮助用户追踪学习进度、管理知识掌握度、获得个性化学习推荐。

- **前身**：LTS Program（HTML5/CSS3/vanilla JS PWA）
- **技术栈**：Kotlin + Jetpack Compose + Material 3 (Material You)
- **最低支持**：Android 10 (API 29)
- **远期目标**：Compose Multiplatform（Android → Windows）

---

## 2. 版本号规则

**格式**：`VA.B.C (beta n)`

| 规则 | 说明 |
|------|------|
| 起始版本 | 0.0.0 |
| A/B/C/n | ≤ 30 |
| 进入 1.0.0 | 所有正式功能完成后 |
| Beta 版本 | 仅在 1.0.0 之后开放 |

---

## 3. 核心功能

### 3.1 学习可视化

- **XP 等级系统**：固定里程碑映射 + 数据驱动曲线拟合
- **温度模型**：HOT / WARM / COOL / COLD 反映学习状态
- **连续天数**：有任何数据即有效，断一天归零，凌晨 0-6 点算前一天

#### 等级-得分率里程碑映射

| 里程碑 | 得分率 | 考试类型 |
|--------|--------|----------|
| 10 级 | 50% | 学考基础 |
| 20 级 | 80% | 学考 |
| 30 级 | 90% | 学考 |
| 40 级 | 60% | 高考 |
| 50 级 | 70% | 高考 |
| 60 级 | 78% | 高考 |
| 70 级 | 83% | 高考 |
| 80 级 | 目标−10% | 高考 |
| 90 级 | 目标−5% | 高考 |
| 100 级 | 用户目标（如 95%） | 高考 |

- 每 10 级为**考试里程碑**，用户须考试证明达到对应分数
- 中间小级由系统根据日常数据估算，用于调整 XP 曲线
- **XP 拟合数据**（重要性排序）：考试得分率 > 学习时长 > FSRS 参数 > 任务完成量
- 模拟考试须为完整高考题型试卷，高一高二阶段试卷不计入

### 3.2 智能推荐

- **算法**：背包问题 + 多约束求解
- **约束条件**：
  1. 时间预算（用户设定可用学习时间）
  2. 输入输出比例（30% 输入 / 70% 输出）
  3. 学科权重（手动滑块 0-10 + 自动推断）
  4. FSRS 到期优先
- **推荐上限**：多因素动态算法（考虑工作日/周末差异、近期趋势、学习节奏波动）
- **学科权重自动推断**：基于每日学习时长分布
- **输入输出分类**：NLP 系统检测用户录入内容自动判断

### 3.3 间隔重复（FSRS）

- **算法**：FSRS（Free Spaced Repetition Scheduler）
- **评分**：4 档（Again=0, Hard=1, Good=2, Easy=3）
- **实现**：纯函数 + 状态机，f(card, rating) → newCard
- **新用户过渡**：SM-2 → 积累 50 张卡片复习记录后切换 FSRS
- **初始参数**：Anki 默认参数

### 3.4 自然语言解析（NLP）

- **三层架构**：
  1. 正则表达式（覆盖 ~70% 常见格式）— 第一版实现
  2. 设备端 NLP / jieba（~20%）— 后续迭代
  3. LLM API（~10%）— 后续迭代
- **LLM 优先级**：GLM-4-Flash（免费）→ DeepSeek-V3 → Qwen-Turbo
- **用途**：解析用户录入内容，自动分类（学科/教辅/网课/考试/阅读）、提取知识点

### 3.5 番茄钟

- **自动录入**：学科（开始前必须选择）、时长、开始/结束时间
- **其他字段**：要求用户事后补充（知识点、备注等）
- **悬浮窗**：环状番茄图标，双击进入全屏（须已设置当前任务）
- **全屏内配置**：顶部今日数据，小按钮查看历史

### 3.6 考试系统

- NLP 检测用户录入的卷子 / 模拟考成绩
- 完整高考题型试卷可作为里程碑评判标准
- 成绩自动关联知识点

### 3.7 阅读记录

- 独立数据类型，与学科记录分开
- 书架视觉风格
- 不记进度，便捷优先

---

## 4. 学习字段（6 种）

| 字段 | 说明 |
|------|------|
| 学科 | 语文/数学/英语/物理/化学/生物/政治/历史/地理 |
| 教辅 | 教材目录：学科→教材→章→节→知识点 |
| 网课 | 网课目录：平台→课程→章节 |
| 知识点 | 三层：内置基础库（全面覆盖各教材各小节）+ AI 补充 + 自然积累 |
| 番茄钟 | 学习会话记录 |
| 阅读 | 独立阅读记录 |

---

## 5. 数据同步

- **方案**：OneDrive 增量同步 + 时间戳合并
- **参考**：Obsidian Remotely Save 插件
- **本地存储**：Room 数据库
- **冲突策略**：按字段合并，时间戳较新的优先

---

## 6. 架构设计

### 6.1 整体分层

```
Clean Architecture 三层 + Feature-based 模块

┌─────────────────────────────────────────┐
│           Presentation Layer            │
│   Compose UI + ViewModel + Navigation   │
├─────────────────────────────────────────┤
│             Domain Layer                │
│   Use Cases + Pure Algorithms + Models  │
│   (零 Android 依赖，纯 Kotlin)           │
├─────────────────────────────────────────┤
│              Data Layer                 │
│   Repository + Room + OneDrive API      │
└─────────────────────────────────────────┘
```

### 6.2 模块划分

```
:app                          — Application 入口
:core:common                  — 公共工具/扩展
:core:database                — Room 数据库
:core:network                 — Retrofit / OneDrive API
:core:algorithm               — FSRS / 推荐 / XP / NLP 算法
:feature:home                 — 首页 Tab
:feature:data                 — 数据 Tab
:feature:search               — 搜索 Tab
:feature:settings             — 设置 Tab
:feature:pomodoro             — 番茄钟（含悬浮窗）
:feature:review               — 复习系统
:feature:exam                 — 考试系统
:feature:reading              — 阅读记录
```

### 6.3 依赖注入

- **框架**：Koin（KMP 兼容）
- ViewModel 注入：`koinViewModel()`

### 6.4 状态管理

- **StateFlow** 管理 UI 状态
- **viewModelScope.launch** 启动协outines
- **Repository 模式**：各字段独立 Repo + LearningFacade 统一对外

---

## 7. UI 架构

> 详细 UI 规范见 `ui-design.md`（待编写）

### 7.1 导航

- 4 个 Tab：首页 / 数据 / 设置 / 搜索
- 底部导航栏：4 Tab 横排 + 番茄钟上方叠加等大加号按钮
- 悬浮导航栏风格（Apple Music 风格）

### 7.2 设计语言

- Material 3 (Material You)
- **主题色**：淡蓝色 + 白色
- 深色模式：手动切换，深灰底 + 浅灰文字 + 毛玻璃细节
- 纯白极简 + 毛玻璃细节 + 精致交互引导

### 7.3 关键页面

- **首页**：同步状态 → Hero Stats → 番茄钟 → 智能推荐 → 学科卡片
- **数据 Tab**：图表（6 种 + ECharts 雷达图），独立于首页
- **学科详情**：概览摘要 + 4 子模块折叠，知识点四层嵌套到节级别
- **搜索**：下滑手势呼出，全局关键词匹配，结果分组 + 跳转 + 拼音

---

## 8. 平台策略

| 阶段 | 平台 | 说明 |
|------|------|------|
| 第一阶段 | Android (API 29+) | 纯 Android 原生 |
| 第二阶段 | Windows | Compose Multiplatform |
| 远期 | iOS / Web | 视情况 |

- **手机优先**，双端不同设计
- 用户手机：HarmonyOS

---

## 9. 第三方服务

| 服务 | 用途 |
|------|------|
| OneDrive | 数据同步（Microsoft Graph API + OAuth 2.0 + PKCE） |
| LLM API | NLP 第三层（GLM-4-Flash 优先，免费层） |
| GitHub | 代码仓库（MokelungAAA/Sisyphus） |

---

## 10. 签名配置

| 项 | 值 |
|----|-----|
| 密钥库 | `E:\Coding\Kotlin\Sisyphus\release.jks` |
| 算法 | RSA 2048-bit |
| 有效期 | 25 年 |
| 别名 | sisyphus |
| CN | Mokelung |
| OU | Sisyphus |
| 测试阶段 | 使用 release 签名 |
