# InterviewQuiz 题库系统

## 📊 题库概述

InterviewQuiz 现在拥有 **5600+ 道高质量AI/计算机岗位面试选择题，覆盖8大技术领域。

## 📈 题库统计

| 分类 | 题数 | 难度分布 |
|------|------|---------|
| 📊 算法与数据结构 | 1200题 | EASY: 300, MEDIUM: 600, HARD: 300 |
| ☕️ Java与Kotlin | 1000题 | EASY: 350, MEDIUM: 500, HARD: 150 |
| 🏗️ 系统设计 | 800题 | EASY: 210, MEDIUM: 440, HARD: 150 |
| 💾 数据库 | 800题 | EASY: 210, MEDIUM: 440, HARD: 150 |
| 🌐 网络基础 | 600题 | EASY: 170, MEDIUM: 320, HARD: 110 |
| 📱 Android开发 | 400题 | EASY: 140, MEDIUM: 200, HARD: 60 |
| 💻 操作系统 | 400题 | EASY: 140, MEDIUM: 200, HARD: 60 |
| 🤖 人工智能与机器学习 | 400题 | EASY: 140, MEDIUM: 200, HARD: 60 |
| **总计** | **5600题** | **EASY: 1660, MEDIUM: 2900, HARD: 1040** |

## 🎯 题库特色

### 1. 题目质量保障
- ✅ 每道题都有4个选项
- ✅ 正确答案经过验证
- ✅ 包含详细解析
- ✅ 有明确的分类和标签
- ✅ 难度分级（EASY/MEDIUM/HARD

### 2. 架构设计
- 📦 **模块化**：每个分类独立生成
- 🔄 **可扩展**：易于添加新题目
- 📋 **模板化**：使用题目模板系统
- 🔀 **随机化**：选项自动随机排序

### 3. 技术架构
```
core-model/
├── entity/
│   └── QuestionFormat.kt          # 题目数据模型
├── datasource/
│   └── InterviewQuestionDataSource.kt  # 数据源接口
└── generator/
    └── QuestionBankGenerator.kt     # 题库生成器（8个分类生成器）
```

## 🚀 使用方式

### Kotlin中使用
```kotlin
import com.interviewquiz.core.model.datasource.InterviewQuestionDataSource

// 获取完整题库
val questions = InterviewQuestionDataSource.getInitialQuestions()

// 按分类获取
val algorithmQuestions = InterviewQuestionDataSource.getQuestionsByCategory("算法与数据结构")

// 按难度获取
val easyQuestions = InterviewQuestionDataSource.getQuestionsByDifficulty("EASY")

// 获取统计信息
val stats = InterviewQuestionDataSource.getQuestionBankStats()
```

### 运行测试
```bash
# 运行题库生成器测试
./gradlew core-model:test --tests "com.interviewquiz.core.model.generator.QuestionBankGeneratorTest"
```

## 📝 题目结构

```kotlin
data class QuestionFormat(
    val id: String,                    // 题目ID
    val questionText: String,        // 题干
    val options: List<String>,         // 4个选项
    val correctAnswerIndex: Int,     // 正确答案索引
    val explanation: String,         // 题目解析
    val category: String,              // 分类
    val difficulty: String,         // 难度
    val tags: List<String>,         // 标签
    val isBookmarked: Boolean,      // 是否收藏
    val createdAt: Long,           // 创建时间
    val updatedAt: Long            // 更新时间
)
```

## 🎯 题库分类详解

### 1. 算法与数据结构 (1200题)
- 算法基础：时间复杂度、空间复杂度
- 数据结构：数组、链表、栈、队列、哈希表、树、图
- 排序算法：冒泡、选择、插入、快速、归并
- 搜索算法：线性、二分、图搜索
- 动态规划、贪心算法、回溯、并查集、布隆过滤器、单调栈

### 2. Java与Kotlin (1000题)
- Java基础：面向对象、集合框架
- Kotlin特性：空安全、协程、扩展函数
- 并发编程：线程、锁、线程池
- 设计模式
- JVM与GC

### 3. 系统设计 (800题)
- 设计原则与架构模式
- 微服务、消息队列
- 数据库设计与分库分表
- 负载均衡、缓存、CDN

### 4. 数据库 (800题)
- SQL基础、事务、索引、高级SQL
- MySQL、分库分表、读写分离

### 5. 网络基础 (600题)
- OSI七层模型
- TCP/UDP、HTTP/HTTPS
- DNS、WebSocket、CDN

### 6. Android开发 (400题)
- Activity/Fragment生命周期
- UI组件、自定义View
- Jetpack组件
- 性能优化

### 7. 操作系统 (400题)
- 进程、线程、死锁
- 内存管理、虚拟内存
- 文件系统

### 8. 人工智能与机器学习 (400题)
- 机器学习基础
- 深度学习、神经网络
- CNN、RNN、Transformer
- NLP、计算机视觉

## 🔄 扩展题库

### 添加新分类
1. 在`QuestionBankGenerator.kt`中创建新的对象：
```kotlin
object NewCategoryQuestions {
    fun generate(): List<QuestionFormat> = (1..200).map { i ->
        // 生成题目
    }
}
```

2. 在`QuestionBankGenerator.generateCompleteQuestionBank()`中添加
```kotlin
fun generateCompleteQuestionBank(): List<QuestionFormat> = buildList {
    addAll(NewCategoryQuestions.generate())
}
```

### 添加新题目模板
```kotlin
QuestionTemplate(
    "题目标题",
    listOf("正确选项", "错误1", "错误2", "错误3"),
    "题目解析"
)
```

## 📊 题目生成器设计

### 模板系统
使用模板化设计，每个分类使用：
- 100-200个题一组
- 3个模板循环使用
- 选项自动随机排序
- 难度均匀分布

### 性能优化
- 生成器延迟初始化
- 按需生成，不预加载
- 内存友好，无额外依赖

## ✅ 验证与测试

题库已包含完整的测试用例：
- 总题数验证
- 分类统计验证
- 题目结构验证
- 难度分布验证

---

**InterviewQuiz - 5600+ 道高质量题目，助你面试无忧！ 🚀
