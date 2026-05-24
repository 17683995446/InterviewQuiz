package com.interviewquiz.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.interviewquiz.app.data.Question
import com.interviewquiz.app.domain.model.CategoryStats
import com.interviewquiz.app.domain.model.DifficultyStats
import com.interviewquiz.app.domain.model.QuizProgress
import com.interviewquiz.app.util.AppLogger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { 
            AppLogger.e("HomeScreen", "Error occurred: $it")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("InterviewQuiz", fontWeight = FontWeight.Bold)
                        Text(
                            "${uiState.filteredQuestions.size}/${uiState.totalQuestionCount} 道题目", 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.toggleStatistics() }) {
                        Icon(Icons.Default.Analytics, "统计")
                    }
                    IconButton(onClick = { viewModel.toggleFilters() }) {
                        Icon(Icons.Default.FilterList, "筛选")
                    }
                    IconButton(onClick = { viewModel.resetFilters() }) {
                        Icon(Icons.Default.Refresh, "重置")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onClear = { viewModel.updateSearchQuery("") }
            )
            
            // 错误提示
            uiState.error?.let { error ->
                ErrorBanner(
                    message = error,
                    onDismiss = { viewModel.clearError() }
                )
            }
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        WelcomeCard(
                            questions = uiState.filteredQuestions,
                            statistics = uiState.statistics
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    item {
                        AnimatedVisibility(
                            visible = uiState.showFilters,
                            enter = expandVertically(animationSpec = tween(durationMillis = 300)),
                            exit = shrinkVertically(animationSpec = tween(durationMillis = 300))
                        ) {
                            FilterSection(
                                categories = uiState.categories,
                                selectedCategory = uiState.selectedCategory,
                                onCategorySelected = { viewModel.selectCategory(it) },
                                difficulties = uiState.difficulties,
                                selectedDifficulty = uiState.selectedDifficulty,
                                onDifficultySelected = { viewModel.selectDifficulty(it) }
                            )
                        }
                    }
                    
                    item {
                        AnimatedVisibility(
                            visible = uiState.showStatistics,
                            enter = expandVertically(animationSpec = tween(durationMillis = 300)),
                            exit = shrinkVertically(animationSpec = tween(durationMillis = 300))
                        ) {
                            StatisticsSection(
                                statistics = uiState.statistics,
                                onClearAllProgress = { viewModel.clearAllProgress() }
                            )
                        }
                    }
                    
                    item {
                        Text(
                            "题目列表",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(
                        items = uiState.filteredQuestions,
                        key = { question -> question.id }
                    ) { question ->
                        val progress = viewModel.getProgress(question.id)
                        QuestionCard(
                            question = question,
                            progress = progress,
                            onAnswer = { answerIndex ->
                                viewModel.recordAnswer(question, answerIndex)
                            },
                            onResetProgress = {
                                viewModel.resetQuestionProgress(question.id)
                            }
                        )
                    }
                    
                    if (uiState.filteredQuestions.isEmpty() && !uiState.isLoading) {
                        item {
                            EmptyState()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .focusRequester(focusRequester),
        placeholder = { Text("搜索题目...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "搜索")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = {
                    onClear()
                    focusRequester.freeFocus()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "清除")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { focusRequester.freeFocus() }
        ),
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
fun WelcomeCard(
    questions: List<Question>,
    statistics: com.interviewquiz.app.domain.model.QuizStatistics
) {
    val easyCount = questions.count { it.difficulty == "EASY" }
    val mediumCount = questions.count { it.difficulty == "MEDIUM" }
    val hardCount = questions.count { it.difficulty == "HARD" }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "欢迎使用 InterviewQuiz！",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "准备面试的最佳伙伴，海量高质量题目",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // 进度显示
            if (statistics.completedQuestions > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "已完成 ${statistics.completedQuestions}/${statistics.totalQuestions}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "正确率 ${(statistics.accuracyRate * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { statistics.completionRate },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip("简单", easyCount.toString(), MaterialTheme.colorScheme.primary)
                StatChip("中等", mediumCount.toString(), MaterialTheme.colorScheme.secondary)
                StatChip("困难", hardCount.toString(), MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun StatisticsSection(
    statistics: com.interviewquiz.app.domain.model.QuizStatistics,
    onClearAllProgress: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "学习统计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (statistics.completedQuestions > 0) {
                    TextButton(onClick = onClearAllProgress) {
                        Text("清除进度")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // 总体统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${statistics.completedQuestions}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("已完成", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${(statistics.accuracyRate * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text("正确率", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${statistics.totalAttempts}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text("总答题", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            // 分类统计
            if (statistics.categoryStats.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "分类统计",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                statistics.categoryStats.values.forEach { categoryStat ->
                    CategoryStatsItem(categoryStat)
                }
            }
            
            // 难度统计
            if (statistics.difficultyStats.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "难度统计",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                statistics.difficultyStats.values.forEach { difficultyStat ->
                    DifficultyStatsItem(difficultyStat)
                }
            }
        }
    }
}

@Composable
fun CategoryStatsItem(stat: CategoryStats) {
    val completionRate = if (stat.totalQuestions > 0) {
        stat.completedQuestions.toFloat() / stat.totalQuestions
    } else 0f
    
    val accuracyRate = if (stat.completedQuestions > 0) {
        stat.correctAnswers.toFloat() / stat.completedQuestions
    } else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stat.category,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${stat.completedQuestions}/${stat.totalQuestions}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { completionRate },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (stat.completedQuestions > 0) {
                Text(
                    "正确率: ${(accuracyRate * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun DifficultyStatsItem(stat: DifficultyStats) {
    val color = when (stat.difficulty) {
        "EASY" -> MaterialTheme.colorScheme.primary
        "MEDIUM" -> MaterialTheme.colorScheme.secondary
        "HARD" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.tertiary
    }
    
    val completionRate = if (stat.totalQuestions > 0) {
        stat.completedQuestions.toFloat() / stat.totalQuestions
    } else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stat.difficulty,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
                Text(
                    "${stat.completedQuestions}/${stat.totalQuestions}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { completionRate },
                modifier = Modifier.fillMaxWidth(),
                color = color
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun StatChip(label: String, count: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                count,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
fun FilterSection(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    difficulties: List<String>,
    selectedDifficulty: String?,
    onDifficultySelected: (String?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "筛选条件",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "分类",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text("全部") }
                )
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "难度",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedDifficulty == null,
                    onClick = { onDifficultySelected(null) },
                    label = { Text("全部") }
                )
                difficulties.forEach { difficulty ->
                    val color = when (difficulty) {
                        "EASY" -> MaterialTheme.colorScheme.primary
                        "MEDIUM" -> MaterialTheme.colorScheme.secondary
                        "HARD" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.tertiary
                    }
                    FilterChip(
                        selected = selectedDifficulty == difficulty,
                        onClick = { onDifficultySelected(difficulty) },
                        label = { Text(difficulty) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "没有找到符合条件的题目",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "请尝试调整筛选条件",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuestionCard(
    question: Question,
    progress: QuizProgress?,
    onAnswer: (Int) -> Unit,
    onResetProgress: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    // 使用 mutableState 但通过 LaunchedEffect 确保同步
    var selectedAnswer by remember { 
        mutableStateOf<Int?>(progress?.lastAnswer) 
    }
    
    // 当 progress 更新时同步到本地状态
    LaunchedEffect(progress?.lastAnswer) {
        selectedAnswer = progress?.lastAnswer
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        question.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (progress != null && progress.isCompleted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (progress.isCorrect == true) 
                                Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = if (progress.isCorrect == true) "已答对" else "已答错",
                            tint = if (progress.isCorrect == true) 
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DifficultyChip(question.difficulty)
                    if (progress != null && progress.attempts > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "×${progress.attempts}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                question.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    question.options.forEachIndexed { index, option ->
                        AnswerOption(
                            text = option,
                            index = index,
                            isSelected = selectedAnswer == index,
                            isCorrect = index == question.correctAnswer,
                            showCorrectness = selectedAnswer != null,
                            onClick = {
                                if (selectedAnswer == null) {
                                    selectedAnswer = index
                                    onAnswer(index)
                                }
                            }
                        )
                    }
                }
                if (selectedAnswer != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ExplanationCard(
                        explanation = question.explanation,
                        isCorrect = selectedAnswer == question.correctAnswer
                    )
                    
                    // 重置进度按钮
                    if (progress != null && progress.isCompleted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = {
                            selectedAnswer = null // 先清除本地状态
                            onResetProgress()
                        }) {
                            Text("重新答题")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (expanded) "点击收起" else "点击查看详情",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DifficultyChip(difficulty: String) {
    val (containerColor, textColor) = when (difficulty) {
        "EASY" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "MEDIUM" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        "HARD" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    }
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = difficulty,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun AnswerOption(
    text: String,
    index: Int,
    isSelected: Boolean,
    isCorrect: Boolean,
    showCorrectness: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        !showCorrectness && isSelected -> MaterialTheme.colorScheme.primaryContainer
        showCorrectness && isCorrect -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        showCorrectness && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = when {
        showCorrectness && isCorrect -> MaterialTheme.colorScheme.primary
        showCorrectness && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${('A'.code + index).toChar()}.",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
            if (showCorrectness && isCorrect) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "正确答案",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ExplanationCard(explanation: String, isCorrect: Boolean) {
    val containerColor = if (isCorrect) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val title = if (isCorrect) "回答正确" else "回答错误"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "解析：$explanation",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
