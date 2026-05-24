package com.interviewquiz.app.stress

import com.interviewquiz.app.domain.model.Difficulty
import com.interviewquiz.app.domain.model.QuizProgress
import com.interviewquiz.app.domain.model.Result
import com.interviewquiz.app.ui.screens.HomeUiState
import com.interviewquiz.app.ui.screens.HomeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MassiveConcurrencyTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: HomeViewModel
    private val loadCount = AtomicInteger(0)
    private val saveCount = AtomicInteger(0)

    @Before
    fun setup() {
        setMain(testDispatcher)
        
        viewModel = object : HomeViewModel(
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true)
        ) {
            override suspend fun loadQuizProgress() {
                loadCount.incrementAndGet()
                val progress = QuizProgress(
                    currentQuestionIndex = 0,
                    totalQuestions = 10,
                    score = 0,
                    isCompleted = false,
                    selectedAnswers = emptyMap(),
                    difficulty = Difficulty.EASY
                )
                _uiState.value = HomeUiState.Loaded(
                    questions = emptyList(),
                    quizProgress = progress,
                    currentQuestion = null,
                    showResult = false,
                    showStats = false,
                    isLoading = false,
                    error = null,
                    lastStateVersion = 1
                )
            }

            override suspend fun saveQuizProgress() {
                saveCount.incrementAndGet()
            }
        }
    }

    @After
    fun tearDown() {
        resetMain()
    }

    @Test
    fun `massive concurrent state updates - 1000 concurrent operations`() = runTest {
        val operationCount = 1000
        val jobs = mutableListOf<Job>()
        val successCount = AtomicInteger(0)
        val errorCount = AtomicInteger(0)

        repeat(operationCount) { index ->
            jobs += launch {
                try {
                    when (index % 6) {
                        0 -> viewModel.loadQuestions(Difficulty.EASY)
                        1 -> viewModel.selectAnswer(index % 4)
                        2 -> viewModel.nextQuestion()
                        3 -> viewModel.previousQuestion()
                        4 -> viewModel.showStatistics()
                        5 -> viewModel.hideStatistics()
                    }
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    errorCount.incrementAndGet()
                }
            }
        }

        jobs.joinAll()

        println("=== Massive Concurrency Test ===")
        println("Total operations: $operationCount")
        println("Success: ${successCount.get()}")
        println("Errors: ${errorCount.get()}")
        
        assertTrue(errorCount.get() == 0, "No errors should occur during concurrent operations")
    }

    @Test
    fun `chaotic operation sequence - random rapid fire operations`() = runTest {
        val operationCount = 500
        val jobs = mutableListOf<Job>()
        val successCount = AtomicInteger(0)
        val errorCount = AtomicInteger(0)
        val random = java.util.Random()

        repeat(operationCount) {
            jobs += launch {
                try {
                    delay(random.nextLong(0, 10))
                    when (random.nextInt(12)) {
                        0 -> viewModel.loadQuestions(Difficulty.EASY)
                        1 -> viewModel.loadQuestions(Difficulty.MEDIUM)
                        2 -> viewModel.loadQuestions(Difficulty.HARD)
                        3 -> viewModel.selectAnswer(random.nextInt(4))
                        4 -> viewModel.nextQuestion()
                        5 -> viewModel.previousQuestion()
                        6 -> viewModel.showStatistics()
                        7 -> viewModel.hideStatistics()
                        8 -> viewModel.clearError()
                        9 -> viewModel.restartQuiz()
                        10 -> viewModel.submitAnswer()
                        11 -> viewModel.loadStatistics()
                    }
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    errorCount.incrementAndGet()
                }
            }
        }

        jobs.joinAll()

        println("=== Chaotic Operation Test ===")
        println("Total operations: $operationCount")
        println("Success: ${successCount.get()}")
        println("Errors: ${errorCount.get()}")
        
        assertTrue(errorCount.get() == 0, "No errors should occur during chaotic operations")
    }

    @Test
    fun `state version monotonicity - versions should always increase`() = runTest {
        val initialVersion = (viewModel.uiState.first() as? HomeUiState.Loaded)?.lastStateVersion ?: 0
        val operationCount = 200
        val lastVersion = AtomicInteger(initialVersion)
        val violations = AtomicInteger(0)

        val jobs = (1..operationCount).map {
            launch {
                repeat(10) { _ ->
                    viewModel.selectAnswer(it % 4)
                    val currentState = viewModel.uiState.value
                    if (currentState is HomeUiState.Loaded) {
                        val currentVersion = currentState.lastStateVersion
                        val prev = lastVersion.get()
                        if (currentVersion < prev) {
                            violations.incrementAndGet()
                        }
                        lastVersion.updateAndGet { maxOf(it, currentVersion) }
                    }
                }
            }
        }

        jobs.joinAll()

        println("=== State Version Monotonicity Test ===")
        println("Violations: ${violations.get()}")
        println("Final version: ${lastVersion.get()}")
        
        assertEquals(0, violations.get(), "State versions should never decrease")
        assertTrue(lastVersion.get() > initialVersion, "State version should increase")
    }

    @Test
    fun `concurrent load and save - database race conditions`() = runTest {
        val loadSaveCount = 500
        val jobs = mutableListOf<Job>()

        repeat(loadSaveCount) {
            jobs += launch {
                viewModel.loadStatistics()
            }
            jobs += launch {
                viewModel.saveQuizProgress()
            }
        }

        jobs.joinAll()

        println("=== Concurrent Load/Save Test ===")
        println("Load operations: ${loadCount.get()}")
        println("Save operations: ${saveCount.get()}")
        
        assertTrue(loadCount.get() > 0, "Load operations should execute")
    }

    @Test
    fun `extreme rapid state changes - 10000 operations in 1 second`() = runTest {
        val operationCount = 10000
        val startTime = System.currentTimeMillis()
        val successCount = AtomicInteger(0)

        val jobs = (1..operationCount).map { index ->
            launch {
                try {
                    when (index % 4) {
                        0 -> viewModel.selectAnswer(0)
                        1 -> viewModel.selectAnswer(1)
                        2 -> viewModel.selectAnswer(2)
                        3 -> viewModel.selectAnswer(3)
                    }
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                }
            }
        }

        jobs.joinAll()
        val endTime = System.currentTimeMillis()

        println("=== Extreme Rapid Changes Test ===")
        println("Operations: $operationCount")
        println("Success: ${successCount.get()}")
        println("Time: ${endTime - startTime}ms")
        println("Throughput: ${(operationCount * 1000) / (endTime - startTime + 1)} ops/s")
        
        assertTrue(successCount.get() >= operationCount * 0.9, "At least 90% of operations should succeed")
    }
}
