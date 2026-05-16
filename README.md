# InterviewQuiz

> AI/计算机岗位面试刷题 Android 应用

## 📱 项目概述

InterviewQuiz 是一个专业的面试刷题应用，提供海量高质量的 AI/计算机岗位面试题目，帮助你高效准备面试！

## ✨ 主要特性

- 📚 **120+ 道精选面试题**（可扩展至 500+）
- 🎯 **6 大技术分类**（算法、Java/Kotlin、数据库、网络、Android、OS）
- 📊 **难度分级**（EASY/MEDIUM/HARD）
- 🎨 **Material Design 3** 现代 UI
- 🔍 **智能筛选**（分类 + 难度）
- 💡 **详细解析** 帮助理解
- 📈 **学习进度追踪**（开发中）

## 🏗️ 技术栈

- **语言**：Kotlin 2.0
- **UI 框架**：Jetpack Compose
- **依赖注入**：Hilt 2.50
- **架构模式**：MVVM + Clean Architecture
- **最低支持**：Android 7.0 (API 24)
- **目标版本**：Android 14 (API 34)

## 📂 项目结构

```
app/
├── src/main/kotlin/com/interviewquiz/app/
│   ├── InterviewQuizApplication.kt (@HiltAndroidApp)
│   ├── MainActivity.kt
│   ├── data/
│   │   ├── Question.kt
│   │   └── QuestionRepository.kt
│   ├── di/
│   │   └── AppModule.kt
│   └── ui/screens/
│       ├── HomeScreen.kt
│       └── HomeViewModel.kt
├── src/test/ (单元测试)
├── src/androidTest/ (UI 测试)
└── build.gradle.kts

docs/
├── REQUIREMENTS.md (需求文档)
├── SYSTEM_DESIGN.md (系统设计)
├── IMPLEMENTATION_PLAN.md (实施计划)
└── FIVE_PHASE_PLAN.md (五阶段计划)
```

## 🚀 快速开始

### 本地构建
```bash
# Clone 项目
git clone <repository-url>

# 构建 Debug APK
./gradlew assembleDebug

# 运行单元测试
./gradlew testDebugUnitTest

# 代码质量检查
./gradlew lintDebug
```

### 下载最新版本
访问 [GitHub Actions](https://github.com/17683995446/InterviewQuiz/actions) 获取自动构建的 APK。

## 📋 五阶段实施计划

我们已规划了五个完整的实施阶段：

### ✅ Phase 1: 测试与质量保障 (当前阶段)
- [x] 单元测试覆盖
- [x] UI 测试
- [x] GitHub Actions 自动化测试
- [ ] 代码质量工具

### 📋 Phase 2: 功能完善与用户体验
- [ ] 收藏功能
- [ ] 学习进度追踪
- [ ] 历史记录
- [ ] 设置页面

### 📊 Phase 3: 题库扩展与 AI/ML 集成
- [ ] 500+ 道题目
- [ ] AI/ML 分类
- [ ] 数据可视化

### 🚀 Phase 4: 性能优化与高级功能
- [ ] Room 数据库
- [ ] 离线功能
- [ ] 搜索功能

### 🎉 Phase 5: 生产准备与发布
- [ ] 配置签名
- [ ] 完整文档
- [ ] Firebase 集成

详细内容请查看 [五阶段实施计划](docs/FIVE_PHASE_PLAN.md)

## 📚 题库分类

| 分类 | 题目数 | 说明 |
|------|--------|------|
| 算法与数据结构 | 20 题 | 数据结构、排序、查找等 |
| Java 与 Kotlin | 20 题 | 语言特性、并发、设计模式等 |
| 数据库 | 20 题 | SQL、索引、事务等 |
| 网络基础 | 20 题 | TCP/IP、HTTP、DNS 等 |
| 操作系统 | 20 题 | 进程、内存、文件系统等 |
| Android 开发 | 20 题 | UI、架构、性能优化等 |
| **总计** | **120 题** | **持续扩展中** |

## 🧪 测试

### 运行单元测试
```bash
./gradlew testDebugUnitTest
```

### 运行 UI 测试
```bash
./gradlew connectedAndroidTest
```

## 📖 更多文档

- [需求文档](docs/REQUIREMENTS.md) - 完整的需求分析
- [系统设计](docs/SYSTEM_DESIGN.md) - 架构设计和技术选型
- [实施计划](docs/IMPLEMENTATION_PLAN.md) - 详细的实施步骤
- [五阶段计划](docs/FIVE_PHASE_PLAN.md) - 五阶段总体规划

## 🤝 贡献

欢迎贡献！请查看各阶段实施计划并提交 PR。

## 📄 许可证

MIT License

---

**更新时间：2026-05-16**  
**当前阶段：Phase 1 - 测试与质量保障**
