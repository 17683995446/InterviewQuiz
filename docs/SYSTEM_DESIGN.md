# InterviewQuiz 系统设计文档 (SDD)

## 1. 系统架构

### 1.1 整体架构
```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌─────────────────────────────────────────────────┐   │
│  │           HomeScreen (Jetpack Compose)          │   │
│  │  - WelcomeCard (统计数据展示)                    │   │
│  │  - FilterSection (筛选组件)                     │   │
│  │  - QuestionCard (题目卡片)                      │   │
│  │  - AnswerOption (选项组件)                      │   │
│  │  - ExplanationCard (解析组件)                   │   │
│  └─────────────────────────────────────────────────┘   │
│                          ↓                              │
│  ┌─────────────────────────────────────────────────┐   │
│  │              HomeViewModel                       │   │
│  │  - 状态管理 (selectedCategory, selectedDifficulty) │   │
│  │  - 业务逻辑 (filterQuestions)                    │   │
│  │  - 数据流 (StateFlow)                           │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                      Domain Layer                       │
│  ┌─────────────────────────────────────────────────┐   │
│  │            Question (Domain Model)               │   │
│  │  - id: Int                                      │   │
│  │  - title: String                                │   │
│  │  - options: List<String>                        │   │
│  │  - correctAnswer: Int                           │   │
│  │  - explanation: String                          │   │
│  │  - category: String                             │   │
│  │  - difficulty: Difficulty                       │   │
│  └─────────────────────────────────────────────────┘   │
│                          ↓                              │
│  ┌─────────────────────────────────────────────────┐   │
│  │            QuestionRepository                    │   │
│  │  - getQuestions(): List<Question>               │   │
│  │  - getQuestionsByCategory()                     │   │
│  │  - getQuestionsByDifficulty()                    │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                       Data Layer                        │
│  ┌─────────────────────────────────────────────────┐   │
│  │         QuestionRepositoryImpl                  │   │
│  │  - 本地数据源 (In-Memory)                       │   │
│  │  - 数据生成器 (QuestionBankGenerator)            │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

---

## 2. 核心模块设计

### 2.1 数据模型

#### Question (题目)
```kotlin
data class Question(
    val id: Int,                    // 唯一标识
    val title: String,               // 题目标题
    val options: List<String>,       // 选项列表（A、B、C、D）
    val correctAnswer: Int,         // 正确答案索引（0-3）
    val explanation: String,         // 解析说明
    val category: Category,          // 分类
    val difficulty: Difficulty       // 难度
)

enum class Category {
    ALGORITHM,      // 算法与数据结构
    JAVA_KOTLIN,    // Java与Kotlin
    DATABASE,       // 数据库
    NETWORK,        // 网络基础
    OPERATING_SYSTEM, // 操作系统
    ANDROID,        // Android开发
    SYSTEM_DESIGN,  // 系统设计
    AI_ML           // AI与机器学习
}

enum class Difficulty {
    EASY,   // 简单
    MEDIUM, // 中等
    HARD    // 困难
}
```

### 2.2 状态管理

#### HomeUiState
```kotlin
data class HomeUiState(
    val allQuestions: List<Question>,          // 所有题目
    val filteredQuestions: List<Question>,     // 筛选后题目
    val selectedCategory: Category?,           // 选中的分类
    val selectedDifficulty: Difficulty?,       // 选中的难度
    val showFilters: Boolean,                 // 是否显示筛选器
    val isLoading: Boolean,                    // 加载状态
    val error: String?                         // 错误信息
)
```

### 2.3 用户交互流程

```
用户打开应用
    ↓
HomeScreen 加载
    ↓
从 QuestionRepository 获取所有题目
    ↓
显示 WelcomeCard (统计信息)
    ↓
显示 QuestionCard 列表
    ↓
用户点击题目卡片
    ↓
展开显示选项
    ↓
用户选择答案
    ↓
显示反馈 (正确/错误)
    ↓
显示解析
```

---

## 3. UI/UX 设计

### 3.1 页面结构

#### HomeScreen 布局
```
┌────────────────────────────────┐
│      TopAppBar (标题栏)         │
│  "InterviewQuiz"  | 筛选 | 重置 │
├────────────────────────────────┤
│                                │
│     WelcomeCard (欢迎卡片)      │
│  📊 欢迎使用 | 简单/中等/困难   │
│                                │
├────────────────────────────────┤
│                                │
│    FilterSection (筛选区)       │
│  分类: [全部] [算法] [Java]... │
│  难度: [全部] [EASY] [MEDIUM]  │
│                                │
├────────────────────────────────┤
│                                │
│     QuestionCard (题目卡片)      │
│  ┌──────────────────────────┐   │
│  │ 分类: 算法  | 难度: EASY │   │
│  │ 什么是时间复杂度？         │   │
│  │ 点击查看详情              │   │
│  └──────────────────────────┘   │
│                                │
│     QuestionCard (更多...)      │
│                                │
└────────────────────────────────┘
```

### 3.2 颜色系统

#### Material Design 3 颜色
- Primary: #6200EE (紫色)
- Secondary: #03DAC6 (青色)
- Error: #B00020 (红色)
- Background: #FFFFFF (白色)
- Surface: #FFFFFF (白色)

#### 难度颜色
- EASY: Primary (紫色)
- MEDIUM: Secondary (青色)
- HARD: Error (红色)

### 3.3 组件状态

#### QuestionCard 状态
- Default: 显示标题和标签
- Expanded: 显示选项和解析
- Selected: 显示答案反馈

#### AnswerOption 状态
- Default: 白色背景
- Selected: 紫色背景
- Correct: 绿色背景 + ✓ 图标
- Incorrect: 红色背景

---

## 4. 性能优化

### 4.1 列表优化
- 使用 `LazyColumn` 懒加载
- `items` 替代 `repeat`
- `remember` 缓存筛选结果

### 4.2 状态管理优化
- `remember` vs `rememberSaveable`
- `StateFlow` vs `LiveData`
- 避免不必要的重组

### 4.3 内存优化
- `LazyColumn` item复用
- 避免创建不必要的对象
- 及时释放资源

---

## 5. 扩展性设计

### 5.1 题库扩展
```kotlin
// 简单添加新题目
Question(
    id = 121,
    title = "新题目",
    options = listOf("A", "B", "C", "D"),
    correctAnswer = 0,
    explanation = "解析",
    category = Category.ALGORITHM,
    difficulty = Difficulty.MEDIUM
)
```

### 5.2 新增分类
```kotlin
// 1. 在 Category 枚举中添加新分类
enum class Category {
    // ... 现有分类
    NEW_CATEGORY
}

// 2. 在 QuestionRepository 中添加生成方法
private fun generateNewCategoryQuestions(): List<Question> = listOf(...)
```

### 5.3 新增难度
```kotlin
// 在 Difficulty 枚举中添加新难度
enum class Difficulty {
    EASY,
    MEDIUM,
    HARD,
    EXPERT  // 新增
}
```

---

## 6. 测试策略

### 6.1 单元测试
- Question 数据模型测试
- QuestionRepository 测试
- 筛选逻辑测试

### 6.2 UI测试
- 筛选功能测试
- 答题交互测试
- 状态转换测试

### 6.3 集成测试
- 完整用户流程测试
- 性能测试

---

**文档版本**：1.0  
**更新日期**：2026-05-16  
**架构师**：AI Assistant
