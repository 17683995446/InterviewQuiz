# InterviewQuiz - 面试刷题应用

一个专业的 AI/计算机岗位面试刷题 Android 应用。

## 项目状态

✅ **GitHub Actions 自动构建**：每次 push 自动生成 APK  
✅ **完全可构建**：已优化配置，确保成功  
✅ **现代 UI**：Jetpack Compose + Material Design 3

## 构建

### 自动构建 (推荐)
项目使用 GitHub Actions 自动构建：
1. Push 代码到仓库
2. 访问 https://github.com/17683995446/InterviewQuiz/actions
3. 查看构建结果，下载 APK

### 本地构建
```bash
./gradlew assembleDebug
```

APK 输出位置：`app/build/outputs/apk/debug/app-debug.apk`

## 技术栈
- Kotlin 2.0
- Jetpack Compose
- Android Gradle Plugin 8.2.2
- Gradle 8.2
- 最低 SDK 24 (Android 7.0)
- 目标 SDK 34 (Android 14)
