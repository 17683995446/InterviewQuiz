# 代码架构深度分析

## 一、分层架构

```
┌─────────────────────────────────────────────────────────────┐
│                        UI 层                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────┐  │
│  │  HomeScreen  │  │ HomeViewModel │  │ Theme  │  │
│  └──────┬───────┘  └──────┬───────┘  └────────┘  │
└─────────┼───────────────────┼──────────────────┘
          │                   │
┌─────────▼───────────────────▼──────────────────┐
│                    领域层 (Domain Layer)                 │
│  ┌──────────────────────────────────┐       │
│  │         Use Cases             │       │
│  │  - QuestionUseCases          │       │
│  │  - QuizUseCases             │       │
│  └──────────────────────────────────┘       │
│  ┌──────────────────────────────────┐       │
│  │        Models                   │       │
│  │  - QuizProgress            │       │
│  │  - QuizStatistics          │       │
│  └──────────────────────────────────┘       │
└─────────────────────────────────────────────────┘
          │
┌─────────▼──────────────────────────────────────┐
│                   数据层 (Data Layer)               │
│  ┌──────────────────────────────────┐       │
│  │    Repositories              │       │
│  │  - QuestionRepository      │       │
│  │  - QuizProgressRepository │       │
│  └──────────────────────────────────┘       │
└─────────────────────────────────────────────────┘
          │
┌─────────▼──────────────────────────────────────┐
│                 核心层 (Core Layer)                │
│  ┌─────────┐  ┌──────────┐  ┌────────────┐  │
│  │Dispatchers│  │  Result  │  │BaseUseCase│  │
│  └─────────┘  └──────────┘  └────────────┘  │
└─────────────────────────────────────────────────┘
          │
┌─────────▼──────────────────────────────────────┐
│               基础设施层 (Infrastructure)             │
│  ┌──────────────────────────────────┐       │
│  │  SecurityManager             │       │
│  │  AppLogger               │       │
│  │  DI Modules               │       │
│  └──────────────────────────────────┘       │
└─────────────────────────────────────────────────┘
```

## 二、调用链路

### 2.1 初始化流程
```
MainActivity
  └─> HomeScreen
        └─> HomeViewModel (init)
              ├─> loadQuestions()
              │     ├─> getQuestionsUseCase
              │     ├─> getCategoriesUseCase
              │     ├─> getDifficultiesUseCase
              │     ├─> getQuestionCountUseCase
              │     ├─> getAllProgressUseCase
              │     └─> getStatisticsUseCase
              └─> observeFiltersAndApply()
                    └─> filterQuestionsUseCase
```

### 2.2 答题记录流程
```
HomeScreen (QuestionCard)
  └─> HomeViewModel.recordAnswer()
        └─> recordAnswerUseCase
              ├─> QuizProgressRepository.getProgress()
              ├─> QuizProgress.recordAnswer()
              └─> QuizProgressRepository.saveProgress()
        └─> 更新状态
              ├─> progressMap
              └─> statistics
```

## 三、模块依赖

| 模块 | 依赖 |
|------|------|
| HomeViewModel | QuestionUseCases, QuizUseCases, BaseViewModel |
| QuestionUseCases | QuestionRepository |
| QuizUseCases | QuizProgressRepository |
| QuestionRepository | AppLogger |
| QuizProgressRepository | AppLogger, Context |
| BaseViewModel | AppLogger |
| AppLogger | (无外部依赖) |
| SecurityManager | Context |

## 四、关键参数规则

| 参数 | 规则 | 默认值 |
|------|------|--------|
| question.id | 整数，唯一标识 | - |
| searchQuery | 长度限制 500 字符 | "" |
| selectedCategory | 可选，字符串 | null |
| selectedDifficulty | 可选，字符串 | null |
| attempts | 非负整数 | 0 |
| lastStateVersion | 严格递增 | 0 |
| progressMap.size | ≤ 1000 条 | emptyMap() |
| cache.size | ≤ 20 条 | - |

## 五、边界阈值

| 阈值 | 值 | 说明 |
|------|---|------|
| 搜索查询最大长度 | 500 | 防止超长查询 |
| 进度映射最大条目 | 1000 | 防止内存泄漏 |
| 搜索缓存条目 | 20 | LRU 淘汰策略 |
| 搜索防抖延迟 | 300ms | 优化性能 |
| 分页大小限制 | 1-1000 | 防止超大请求 |
