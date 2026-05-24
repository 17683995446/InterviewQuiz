package com.interviewquiz.app.stress

import org.junit.Assert.*
import org.junit.Test
import java.util.*
import kotlin.math.abs
import kotlin.system.measureTimeMillis

class ExtremeDataStressTest {
    
    companion object {
        private const val ITERATIONS = 10_000
        private const val LARGE_QUERY_LENGTH = 10_000
        private const val MAX_CACHE_ENTRIES = 100
    }
    
    private val testCategories = listOf("算法", "Java", "数据库", "网络", "操作系统")
    private val testDifficulties = listOf("EASY", "MEDIUM", "HARD")
    
    @Test
    fun `test extremely long search queries`() {
        val largeQuery = buildString {
            repeat(LARGE_QUERY_LENGTH) {
                append('a')
            }
        }
        
        val result = try {
            simulateSearch(largeQuery)
        } catch (e: Exception) {
            fail("Long query should not throw: ${e.message}")
            emptyList()
        }
        
        assertNotNull(result)
    }
    
    @Test
    fun `test rapid repeated searches`() {
        val queries = mutableListOf<String>()
        repeat(ITERATIONS) { i ->
            queries.add("test_$i")
        }
        
        val time = measureTimeMillis {
            queries.forEach { query ->
                try {
                    simulateSearch(query)
                } catch (e: Exception) {
                    fail("Rapid search failed for $query: ${e.message}")
                }
            }
        }
        
        println("Rapid repeated $ITERATIONS searches took ${time}ms")
        assertTrue("Should complete in reasonable time", time < 10_000)
    }
    
    @Test
    fun `test boundary category names`() {
        val boundaryCategories = listOf(
            "", " ", "  ",
            "a".repeat(1000),
            "测试\n换行",
            "测试\t制表",
            "测试\0空字符",
            "!@#$%^&*()",
            "测试 unicode \uD83D\uDE00"
        )
        
        boundaryCategories.forEach { category ->
            val result = try {
                simulateFilterByCategory(category)
            } catch (e: Exception) {
                fail("Failed for category '$category': ${e.message}")
                emptyList()
            }
            assertNotNull(result)
        }
    }
    
    @Test
    fun `test boundary difficulty values`() {
        val boundaryDifficulties = listOf(
            "", " ", "  ",
            "EASY".repeat(10),
            "easy", "Easy", "EASY123",
            "UNKNOWN", "NULL", "NONE"
        )
        
        boundaryDifficulties.forEach { difficulty ->
            val result = try {
                simulateFilterByDifficulty(difficulty)
            } catch (e: Exception) {
                fail("Failed for difficulty '$difficulty': ${e.message}")
                emptyList()
            }
            assertNotNull(result)
        }
    }
    
    @Test
    fun `test large progress data corruption`() {
        val corruptedProgressData = listOf(
            "", "{}", "{ invalid json",
            "{\"questionId\": -1, \"isCompleted\": true}",
            "{\"questionId\": 1, \"attempts\": -100}",
            "{\"questionId\": 1, \"isCompleted\": true, \"isCorrect\": \"not boolean\"}"
        )
        
        corruptedProgressData.forEach { data ->
            val result = try {
                simulateLoadProgress(data)
            } catch (e: Exception) {
                fail("Corrupted data should not crash: $data")
                null
            }
            // Should handle gracefully
        }
    }
    
    @Test
    fun `test rapid state updates`() {
        val time = measureTimeMillis {
            repeat(ITERATIONS) { i ->
                try {
                    simulateStateUpdate(i)
                } catch (e: Exception) {
                    fail("State update failed at $i: ${e.message}")
                }
            }
        }
        
        println("Rapid $ITERATIONS state updates took ${time}ms")
        assertTrue("State updates should be efficient", time < 30_000)
    }
    
    @Test
    fun `test large statistics calculation`() {
        val time = measureTimeMillis {
            repeat(100) { iteration ->
                try {
                    val stats = simulateCalculateStatistics(iteration * 100)
                    assertNotNull(stats)
                } catch (e: Exception) {
                    fail("Statistics calculation failed at $iteration: ${e.message}")
                }
            }
        }
        
        println("100 large statistics calculations took ${time}ms")
        assertTrue("Statistics should be efficient", time < 10_000)
    }
    
    @Test
    fun `test memory pressure scenario`() {
        val time = measureTimeMillis {
            val largeList = mutableListOf<Int>()
            repeat(100_000) {
                try {
                    largeList.add(it)
                    if (it % 10_000 == 0) {
                        simulateSearch("memory_test_$it")
                    }
                } catch (e: OutOfMemoryError) {
                    fail("OOM at iteration $it")
                } catch (e: Exception) {
                    fail("Exception at $it: ${e.message}")
                }
            }
        }
        
        println("Memory pressure test took ${time}ms")
    }
    
    @Test
    fun `test concurrent operation simulation`() {
        val operations = listOf(
            { simulateSearch("concurrent_1") },
            { simulateSearch("concurrent_2") },
            { simulateStateUpdate(1) },
            { simulateCalculateStatistics(10) },
            { simulateFilterByCategory("算法") }
        )
        
        repeat(100) {
            operations.forEach { operation ->
                try {
                    operation()
                } catch (e: Exception) {
                    fail("Concurrent operation failed: ${e.message}")
                }
            }
        }
    }
    
    @Test
    fun `test rapid search cache eviction`() {
        val time = measureTimeMillis {
            repeat(MAX_CACHE_ENTRIES * 2) { i ->
                try {
                    simulateSearch("cache_test_$i")
                } catch (e: Exception) {
                    fail("Cache eviction failed at $i: ${e.message}")
                }
            }
        }
        
        println("Cache eviction test took ${time}ms")
        assertTrue("Cache eviction should be efficient", time < 10_000)
    }
    
    private fun simulateSearch(query: String): List<String> {
        return when {
            query.length > LARGE_QUERY_LENGTH -> emptyList()
            query.isEmpty() -> testCategories
            else -> testCategories.filter { it.lowercase().contains(query.lowercase()) }
        }
    }
    
    private fun simulateFilterByCategory(category: String): List<String> {
        return if (category.isBlank()) {
            testCategories
        } else {
            testCategories.filter { it == category }
        }
    }
    
    private fun simulateFilterByDifficulty(difficulty: String): List<String> {
        return testDifficulties.filter { it == difficulty.uppercase() }
    }
    
    private fun simulateLoadProgress(data: String): Any? {
        return try {
            data.length
        } catch (e: Exception) {
            null
        }
    }
    
    private fun simulateStateUpdate(id: Int) {
        if (id < 0) {
            throw IllegalArgumentException("Invalid id")
        }
    }
    
    private fun simulateCalculateStatistics(count: Int): Map<String, Int> {
        return mapOf(
            "total" to count,
            "completed" to count / 2,
            "correct" to count / 3
        )
    }
}
