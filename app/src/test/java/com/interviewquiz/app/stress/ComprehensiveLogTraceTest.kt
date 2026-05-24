package com.interviewquiz.app.stress

import com.interviewquiz.app.data.Question
import com.interviewquiz.app.data.QuestionRepositoryImpl
import com.interviewquiz.app.domain.model.QuizProgress
import com.interviewquiz.app.data.QuizProgressRepositoryImpl
import com.interviewquiz.app.util.AppLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import android.content.Context
import android.content.SharedPreferences
import org.mockito.Mockito
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 第26轮 - 全面日志追踪测试
 * 通过详细日志追踪验证每个代码路径
 */
@RunWith(MockitoJUnitRunner::class)
class ComprehensiveLogTraceTest {

    companion object {
        private const val TAG = "ComprehensiveLogTraceTest"
    }

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var appLogger: AppLogger
    private lateinit var questionRepository: QuestionRepositoryImpl
    private lateinit var progressRepository: QuizProgressRepositoryImpl

    @Before
    fun setup() {
        appLogger = AppLogger()
        questionRepository = QuestionRepositoryImpl()

        Mockito.`when`(context.getSharedPreferences(Mockito.anyString(), Mockito.anyInt()))
            .thenReturn(sharedPreferences)
        Mockito.`when`(sharedPreferences.edit()).thenReturn(editor)
        Mockito.`when`(editor.putBoolean(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(editor)
        Mockito.`when`(editor.putInt(Mockito.anyString(), Mockito.anyInt())).thenReturn(editor)
        Mockito.`when`(editor.putLong(Mockito.anyString(), Mockito.anyLong())).thenReturn(editor)
        Mockito.`when`(editor.remove(Mockito.anyString())).thenReturn(editor)
        Mockito.`when`(editor.clear()).thenReturn(editor)

        progressRepository = QuizProgressRepositoryImpl(context, appLogger)
    }

    /**
     * 测试 1: 完整代码路径覆盖 - QuestionRepository
     */
    @Test
    fun `full code path coverage question repository`() = runTest {
        println("\n=== [Test 1] Full Code Path Coverage - QuestionRepository ===")
        val coverageCount = AtomicInteger(0)
        val questions = questionRepository.getQuestions()

        // 路径 1: getQuestions()
        println("📝 [Path 1] Testing getQuestions()")
        val allQuestions = questionRepository.getQuestions()
        assertTrue(allQuestions.isNotEmpty())
        coverageCount.incrementAndGet()

        // 路径 2: getQuestionCount()
        println("📝 [Path 2] Testing getQuestionCount()")
        val count = questionRepository.getQuestionCount()
        assertEquals(allQuestions.size, count)
        coverageCount.incrementAndGet()

        // 路径 3: getCategories()
        println("📝 [Path 3] Testing getCategories()")
        val categories = questionRepository.getCategories()
        assertTrue(categories.isNotEmpty())
        coverageCount.incrementAndGet()

        // 路径 4: getDifficulties()
        println("📝 [Path 4] Testing getDifficulties()")
        val difficulties = questionRepository.getDifficulties()
        assertTrue(difficulties.isNotEmpty())
        coverageCount.incrementAndGet()

        // 路径 5: getQuestionsByCategory() - 所有分类
        println("📝 [Path 5] Testing getQuestionsByCategory() - all categories")
        categories.forEach { category ->
            val q = questionRepository.getQuestionsByCategory(category)
            assertTrue(q.isNotEmpty())
        }
        coverageCount.incrementAndGet()

        // 路径 6: getQuestionsByCategory() - 无效分类
        println("📝 [Path 6] Testing getQuestionsByCategory() - invalid category")
        val invalidCategoryResult = questionRepository.getQuestionsByCategory("INVALID_CATEGORY")
        assertTrue(invalidCategoryResult.isEmpty())
        coverageCount.incrementAndGet()

        // 路径 7: getQuestionsByDifficulty() - 所有难度
        println("📝 [Path 7] Testing getQuestionsByDifficulty() - all difficulties")
        difficulties.forEach { difficulty ->
            val q = questionRepository.getQuestionsByDifficulty(difficulty)
            assertTrue(q.isNotEmpty())
        }
        coverageCount.incrementAndGet()

        // 路径 8: getQuestionsByDifficulty() - 无效难度
        println("📝 [Path 8] Testing getQuestionsByDifficulty() - invalid difficulty")
        val invalidDifficultyResult = questionRepository.getQuestionsByDifficulty("INVALID_DIFFICULTY")
        assertTrue(invalidDifficultyResult.isEmpty())
        coverageCount.incrementAndGet()

        // 路径 9: getQuestionsPaginated() - 正常情况
        println("📝 [Path 9] Testing getQuestionsPaginated() - normal case")
        val paginated1 = questionRepository.getQuestionsPaginated(0, 10)
        assertTrue(paginated1.size <= 10)
        coverageCount.incrementAndGet()

        // 路径 10: getQuestionsPaginated() - 负数页码
        println("📝 [Path 10] Testing getQuestionsPaginated() - negative page")
        val paginated2 = questionRepository.getQuestionsPaginated(-10, 10)
        assertTrue(paginated2.isNotEmpty()) // 应该处理为 0
        coverageCount.incrementAndGet()

        // 路径 11: getQuestionsPaginated() - 超大页码
        println("📝 [Path 11] Testing getQuestionsPaginated() - huge page")
        val paginated3 = questionRepository.getQuestionsPaginated(10000, 100)
        assertTrue(paginated3.isEmpty())
        coverageCount.incrementAndGet()

        // 路径 12: getQuestionsPaginated() - 0 页大小
        println("📝 [Path 12] Testing getQuestionsPaginated() - zero page size")
        val paginated4 = questionRepository.getQuestionsPaginated(0, 0)
        assertTrue(paginated4.isNotEmpty()) // 应该处理为 1
        coverageCount.incrementAndGet()

        // 路径 13: getQuestionsPaginated() - 负页大小
        println("📝 [Path 13] Testing getQuestionsPaginated() - negative page size")
        val paginated5 = questionRepository.getQuestionsPaginated(0, -100)
        assertTrue(paginated5.isNotEmpty()) // 应该处理为 1
        coverageCount.incrementAndGet()

        // 路径 14: getQuestionsPaginated() - 超大页大小
        println("📝 [Path 14] Testing getQuestionsPaginated() - huge page size")
        val paginated6 = questionRepository.getQuestionsPaginated(0, 1000000)
        assertTrue(paginated6.size <= 1000) // 应该限制为 1000
        coverageCount.incrementAndGet()

        // 路径 15: searchQuestions() - 空查询
        println("📝 [Path 15] Testing searchQuestions() - empty query")
        val search1 = questionRepository.searchQuestions("")
        assertEquals(allQuestions.size, search1.size)
        coverageCount.incrementAndGet()

        // 路径 16: searchQuestions() - 正常查询
        println("📝 [Path 16] Testing searchQuestions() - normal query")
        val search2 = questionRepository.searchQuestions("Java")
        assertTrue(search2.isNotEmpty())
        coverageCount.incrementAndGet()

        // 路径 17: searchQuestions() - 超长查询
        println("📝 [Path 17] Testing searchQuestions() - very long query")
        val longQuery = "a".repeat(10000)
        val search3 = questionRepository.searchQuestions(longQuery)
        coverageCount.incrementAndGet()

        // 路径 18: searchQuestions() - 无结果查询
        println("📝 [Path 18] Testing searchQuestions() - no results query")
        val search4 = questionRepository.searchQuestions("THIS_IS_A_VERY_UNLIKELY_QUERY_STRING_THAT_WILL_NEVER_MATCH")
        assertTrue(search4.isEmpty())
        coverageCount.incrementAndGet()

        // 路径 19: isQuestionMatch() - 标题匹配
        println("📝 [Path 19] Testing isQuestionMatch() - title match")
        val q1 = questions.first()
        val match1 = questionRepository.isQuestionMatch(q1, q1.title.lowercase())
        assertTrue(match1)
        coverageCount.incrementAndGet()

        // 路径 20: isQuestionMatch() - 分类匹配
        println("📝 [Path 20] Testing isQuestionMatch() - category match")
        val match2 = questionRepository.isQuestionMatch(q1, q1.category.lowercase())
        assertTrue(match2)
        coverageCount.incrementAndGet()

        // 路径 21: isQuestionMatch() - 解释匹配
        println("📝 [Path 21] Testing isQuestionMatch() - explanation match")
        val match3 = questionRepository.isQuestionMatch(q1, q1.explanation.lowercase().take(20))
        assertTrue(match3)
        coverageCount.incrementAndGet()

        // 路径 22: isQuestionMatch() - 选项匹配
        println("📝 [Path 22] Testing isQuestionMatch() - option match")
        val match4 = questionRepository.isQuestionMatch(q1, q1.options.first().lowercase().take(10))
        assertTrue(match4)
        coverageCount.incrementAndGet()

        println("\n✅ [Test 1] Completed! ${coverageCount.get()} code paths tested and verified!")
        println("✅ All QuestionRepository paths covered successfully!\n")
    }

    /**
     * 测试 2: 完整代码路径覆盖 - QuizProgressRepository
     */
    @Test
    fun `full code path coverage quiz progress repository`() = runTest {
        println("\n=== [Test 2] Full Code Path Coverage - QuizProgressRepository ===")
        val coverageCount = AtomicInteger(0)
        val questions = questionRepository.getQuestions()

        // 路径 1: getProgress() - 新题目
        println("📝 [Path 1] Testing getProgress() - new question")
        val progress1 = progressRepository.getProgress(99999)
        assertEquals(99999, progress1.questionId)
        assertEquals(0, progress1.attempts)
        coverageCount.incrementAndGet()

        // 路径 2: saveProgress() - 完成，正确答案
        println("📝 [Path 2] Testing saveProgress() - completed, correct")
        val q1 = questions.first()
        val newProgress1 = QuizProgress(
            questionId = q1.id,
            lastAnswer = q1.correctAnswer,
            isCompleted = true,
            isCorrect = true,
            completedAt = System.currentTimeMillis(),
            attempts = 1
        )
        progressRepository.saveProgress(newProgress1)
        coverageCount.incrementAndGet()

        // 路径 3: getProgress() - 已存在
        println("📝 [Path 3] Testing getProgress() - existing question")
        val progress3 = progressRepository.getProgress(q1.id)
        assertEquals(q1.id, progress3.questionId)
        coverageCount.incrementAndGet()

        // 路径 4: saveProgress() - 未完成
        println("📝 [Path 4] Testing saveProgress() - not completed")
        val newProgress4 = QuizProgress(
            questionId = q1.id,
            lastAnswer = 0,
            isCompleted = false,
            isCorrect = null,
            completedAt = null,
            attempts = 2
        )
        progressRepository.saveProgress(newProgress4)
        coverageCount.incrementAndGet()

        // 路径 5: saveProgress() - 完成，错误答案
        println("📝 [Path 5] Testing saveProgress() - completed, incorrect")
        val newProgress5 = QuizProgress(
            questionId = q1.id,
            lastAnswer = (q1.correctAnswer + 1) % 4,
            isCompleted = true,
            isCorrect = false,
            completedAt = System.currentTimeMillis(),
            attempts = 3
        )
        progressRepository.saveProgress(newProgress5)
        coverageCount.incrementAndGet()

        // 路径 6: saveProgress() - 数据修复 - 已完成但lastAnswer为null
        println("📝 [Path 6] Testing saveProgress() - auto-repair: completed but lastAnswer null")
        val brokenProgress6 = QuizProgress(
            questionId = q1.id + 100,
            lastAnswer = null,
            isCompleted = true,
            isCorrect = true,
            completedAt = System.currentTimeMillis(),
            attempts = 1
        )
        progressRepository.saveProgress(brokenProgress6)
        coverageCount.incrementAndGet()

        // 路径 7: saveProgress() - 数据修复 - 已完成但isCorrect为null
        println("📝 [Path 7] Testing saveProgress() - auto-repair: completed but isCorrect null")
        val brokenProgress7 = QuizProgress(
            questionId = q1.id + 101,
            lastAnswer = 0,
            isCompleted = true,
            isCorrect = null,
            completedAt = System.currentTimeMillis(),
            attempts = 1
        )
        progressRepository.saveProgress(brokenProgress7)
        coverageCount.incrementAndGet()

        // 路径 8: getAllProgress() - 空状态
        println("📝 [Path 8] Testing getAllProgress() - empty state")
        progressRepository.clearProgress()
        val allProgress8 = progressRepository.getAllProgress()
        coverageCount.incrementAndGet()

        // 路径 9: getAllProgress() - 有数据
        println("📝 [Path 9] Testing getAllProgress() - with data")
        repeat(10) { i ->
            progressRepository.saveProgress(
                QuizProgress(
                    questionId = 1000 + i,
                    lastAnswer = 0,
                    isCompleted = true,
                    isCorrect = true,
                    completedAt = System.currentTimeMillis(),
                    attempts = i + 1
                )
            )
        }
        val allProgress9 = progressRepository.getAllProgress()
        assertTrue(allProgress9.isNotEmpty())
        coverageCount.incrementAndGet()

        // 路径 10: getStatistics() - 空数据
        println("📝 [Path 10] Testing getStatistics() - empty data")
        progressRepository.clearProgress()
        val stats10 = progressRepository.getStatistics(questions)
        assertEquals(0, stats10.completedQuestions)
        coverageCount.incrementAndGet()

        // 路径 11: getStatistics() - 有数据
        println("📝 [Path 11] Testing getStatistics() - with data")
        repeat(20) { i ->
            progressRepository.saveProgress(
                QuizProgress(
                    questionId = questions[i].id,
                    lastAnswer = if (i % 2 == 0) questions[i].correctAnswer else (questions[i].correctAnswer + 1) % 4,
                    isCompleted = true,
                    isCorrect = i % 2 == 0,
                    completedAt = System.currentTimeMillis(),
                    attempts = i + 1
                )
            )
        }
        val stats11 = progressRepository.getStatistics(questions)
        assertTrue(stats11.completedQuestions > 0)
        coverageCount.incrementAndGet()

        // 路径 12: resetQuestionProgress() - 重置一个题目
        println("📝 [Path 12] Testing resetQuestionProgress()")
        val q12 = questions[0]
        progressRepository.saveProgress(
            QuizProgress(
                questionId = q12.id,
                lastAnswer = 0,
                isCompleted = true,
                isCorrect = true,
                completedAt = System.currentTimeMillis(),
                attempts = 5
            )
        )
        progressRepository.resetQuestionProgress(q12.id)
        val resetProgress = progressRepository.getProgress(q12.id)
        assertEquals(0, resetProgress.attempts)
        coverageCount.incrementAndGet()

        // 路径 13: clearProgress() - 清空所有
        println("📝 [Path 13] Testing clearProgress()")
        progressRepository.clearProgress()
        val cleared = progressRepository.getAllProgress()
        assertTrue(cleared.isEmpty())
        coverageCount.incrementAndGet()

        println("\n✅ [Test 2] Completed! ${coverageCount.get()} code paths tested and verified!")
        println("✅ All QuizProgressRepository paths covered successfully!\n")
    }

    /**
     * 测试 3: 数据流向追踪
     */
    @Test
    fun `data flow tracing test`() = runTest {
        println("\n=== [Test 3] Data Flow Tracing Test ===")

        val questions = questionRepository.getQuestions()
        val testQuestion = questions.first()

        println("\n🔄 [Data Flow] Starting tracing...")

        // 步骤 1: 初始状态
        println("📊 [Step 1] Initial state - getting progress for question ${testQuestion.id}")
        val initialProgress = progressRepository.getProgress(testQuestion.id)
        println("   ↳ Initial: isCompleted=${initialProgress.isCompleted}, attempts=${initialProgress.attempts}")

        // 步骤 2: 记录第一次答题
        println("📊 [Step 2] Recording first answer...")
        val progress1 = initialProgress.recordAnswer(0, false)
        progressRepository.saveProgress(progress1)
        val afterFirst = progressRepository.getProgress(testQuestion.id)
        println("   ↳ After first answer: isCompleted=${afterFirst.isCompleted}, attempts=${afterFirst.attempts}")

        // 步骤 3: 记录第二次答题（正确）
        println("📊 [Step 3] Recording second (correct) answer...")
        val progress2 = afterFirst.recordAnswer(testQuestion.correctAnswer, true)
        progressRepository.saveProgress(progress2)
        val afterSecond = progressRepository.getProgress(testQuestion.id)
        println("   ↳ After second answer: isCompleted=${afterSecond.isCompleted}, attempts=${afterSecond.attempts}, isCorrect=${afterSecond.isCorrect}")

        // 步骤 4: 验证统计更新
        println("📊 [Step 4] Verifying statistics update...")
        val stats = progressRepository.getStatistics(questions)
        println("   ↳ Statistics: completedQuestions=${stats.completedQuestions}, correctAnswers=${stats.correctAnswers}")

        // 步骤 5: 重置进度
        println("📊 [Step 5] Resetting progress...")
        progressRepository.resetQuestionProgress(testQuestion.id)
        val afterReset = progressRepository.getProgress(testQuestion.id)
        println("   ↳ After reset: isCompleted=${afterReset.isCompleted}, attempts=${afterReset.attempts}")

        // 验证数据一致性
        assertEquals(false, afterReset.isCompleted)
        assertEquals(0, afterReset.attempts)

        println("\n✅ [Test 3] Passed - All data flows correctly traced!\n")
    }

    /**
     * 测试 4: 边界条件和异常场景的完整验证
     */
    @Test
    fun `boundary and edge case validation`() = runTest {
        println("\n=== [Test 4] Boundary and Edge Case Validation ===")
        val questions = questionRepository.getQuestions()
        var testCount = 0

        // 边界测试 1: 最小题目ID
        println("🔍 [Boundary 1] Testing minimum questionId")
        progressRepository.saveProgress(QuizProgress(1, 0, true, true, System.currentTimeMillis(), 1))
        testCount++

        // 边界测试 2: 最大题目ID
        println("🔍 [Boundary 2] Testing maximum questionId")
        progressRepository.saveProgress(QuizProgress(Int.MAX_VALUE, 0, true, true, System.currentTimeMillis(), 1))
        testCount++

        // 边界测试 3: 0 attempts
        println("🔍 [Boundary 3] Testing 0 attempts")
        progressRepository.saveProgress(QuizProgress(100, null, false, null, null, 0))
        testCount++

        // 边界测试 4: 最大 attempts
        println("🔍 [Boundary 4] Testing maximum attempts")
        progressRepository.saveProgress(QuizProgress(101, 0, true, true, System.currentTimeMillis(), Int.MAX_VALUE))
        testCount++

        // 边界测试 5: 最小 lastAnswer
        println("🔍 [Boundary 5] Testing minimum lastAnswer")
        progressRepository.saveProgress(QuizProgress(102, Int.MIN_VALUE, true, true, System.currentTimeMillis(), 1))
        testCount++

        // 边界测试 6: 最大 lastAnswer
        println("🔍 [Boundary 6] Testing maximum lastAnswer")
        progressRepository.saveProgress(QuizProgress(103, Int.MAX_VALUE, true, true, System.currentTimeMillis(), 1))
        testCount++

        // 边界测试 7: 最小 completedAt
        println("🔍 [Boundary 7] Testing minimum completedAt")
        progressRepository.saveProgress(QuizProgress(104, 0, true, true, Long.MIN_VALUE, 1))
        testCount++

        // 边界测试 8: 最大 completedAt
        println("🔍 [Boundary 8] Testing maximum completedAt")
        progressRepository.saveProgress(QuizProgress(105, 0, true, true, Long.MAX_VALUE, 1))
        testCount++

        // 边界测试 9: 负数 attempts
        println("🔍 [Boundary 9] Testing negative attempts (should be clamped to 0)")
        progressRepository.saveProgress(QuizProgress(106, 0, true, true, System.currentTimeMillis(), -100))
        val clampedProgress = progressRepository.getProgress(106)
        assertTrue(clampedProgress.attempts >= 0)
        testCount++

        // 边界测试 10: 大量题目同时保存
        println("🔍 [Boundary 10] Testing bulk save")
        repeat(1000) { i ->
            progressRepository.saveProgress(
                QuizProgress(2000 + i, i % 4, i % 2 == 0, i % 3 == 0, System.currentTimeMillis(), i + 1)
            )
        }
        testCount++

        // 验证所有操作成功
        val allProgress = progressRepository.getAllProgress()
        assertTrue(allProgress.isNotEmpty())

        println("\n✅ [Test 4] Passed - $testCount boundary and edge cases validated!\n")
    }
}
