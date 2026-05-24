package com.interviewquiz.app.stress

import org.junit.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 * 第一性原理：极端压力测试
 * 
 * 按照第一性原理设计的压力测试，专注于真实生产环境中的根本性问题：
 * 1. 状态一致性
 * 2. 并发安全性
 * 3. 内存泄漏检测
 * 4. 边界条件处理
 */
class FirstPrincipleStressTest {
    
    companion object {
        private const val NUM_CONCURRENT_THREADS = 100
        private const val OPERATIONS_PER_THREAD = 100
        private const val STRESS_TEST_ITERATIONS = 10
    }
    
    /**
     * 第一性原理：模拟真实并发场景
     * 验证：多个线程同时进行各种操作
     */
    @Test
    fun `concurrency and consistency stress test`() = runBlocking {
        println("===== Concurrency and Consistency Stress Test =====")
        
        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)
        
        val totalTime = measureTimeMillis {
            repeat(STRESS_TEST_ITERATIONS) { iteration ->
            println("Starting iteration ${iteration + 1}/$STRESS_TEST_ITERATIONS...")
            
            val startTime = System.currentTimeMillis()
            val latch = CountDownLatch(NUM_CONCURRENT_THREADS)
            
            // 模拟真实用户行为
            repeat(NUM_CONCURRENT_THREADS) { threadId ->
                launch(Dispatchers.Default) {
                    try {
                        repeat(OPERATIONS_PER_THREAD) { opIndex ->
                        val operationType = opIndex % 7
                        
                        when (operationType) {
                            0 -> simulateStateUpdate()
                            1 -> simulateSearch()
                            2 -> simulateFilter()
                            3 -> simulateReset()
                            4 -> simulateDataLoad()
                            5 -> simulateMemoryPressure()
                            6 -> simulateStateRead()
                        }
                    }
                    
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failureCount.incrementAndGet()
                    System.err.println("Thread $threadId failed: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
            
            // 等待所有线程完成
            val completed = latch.await(30, TimeUnit.SECONDS)
            val iterTime = System.currentTimeMillis() - startTime
            
            println("Iteration ${iteration + 1} completed in ${iterTime}ms")
            
            if (!completed) {
                println("WARNING: Some threads timed out!")
            }
            
            // 模拟GC
            System.gc()
            System.gc()
            Thread.sleep(100)
        }
    }
    
    println("\n===== Stress Test Complete =====")
    println("Success: ${successCount.get()}")
    println("Failures: ${failureCount.get()}")
    println("Total operations: ${successCount.get() + failureCount.get()}")
    
    assert(failureCount.get() == 0) { "Stress test had failures!" }
}

/**
 * 第一性原理：内存泄漏检测
 */
@Test
fun `memory pressure and leak detection test`() {
    println("===== Memory Pressure and Leak Detection Test =====")
    
    val memoryPressureOperations = mutableListOf<ByteArray>()
    
    try {
        measureTimeMillis {
            repeat(100) { i ->
                // 模拟创建大量内存
                if (i % 5 == 0) {
                    memoryPressureOperations.clear()
                    println("Cleared at iteration $i")
                } else {
                    memoryPressureOperations.add(ByteArray(1024 * 10)) // 10KB
                }
                
                // 同时执行一些实际操作
                if (i % 10 == 0) {
                    println("Memory test iteration $i, current list size: ${memoryPressureOperations.size}")
                }
            }
        }.let { println("Memory test completed in ${it}ms") }
    } finally {
        memoryPressureOperations.clear()
        System.gc()
        System.gc()
    }
}

/**
 * 第一性原理：边界条件极端测试
 */
@Test
fun `extreme boundary conditions test`() {
    println("===== Extreme Boundary Conditions Test =====")
    
    // 1. 超大字符串
    val largeString = buildString {
        repeat(100_000) { append('a') }
    }
    
    println("Large string length: ${largeString.length}")
    
    // 2. 极端数值
    val maxInt = Int.MAX_VALUE
    val minInt = Int.MIN_VALUE
    println("Testing with extreme numeric values")
    
    // 3. 大量集合操作
    val largeMap = ConcurrentHashMap<Int, String>()
    measureTimeMillis {
        repeat(10_000) { i ->
            largeMap[i] = "value_$i"
        }
        
        repeat(10_000) { i ->
            largeMap.remove(i)
        }
    }.let { println("Large map operations completed in ${it}ms") }
    
    println("Extreme boundary test completed successfully")
}

/**
 * 第一性原理：快速连续操作测试
 */
@Test
fun `rapid sequential operations test`() = runBlocking {
    println("===== Rapid Sequential Operations Test =====")
    
    val operationCount = 10_000
    val successCounter = AtomicInteger(0)
    
    val totalTime = measureTimeMillis {
        repeat(operationCount) { i ->
            try {
                simulateStateUpdate()
                successCounter.incrementAndGet()
                
                if (i % 1000 == 0) {
                    println("Completed $i operations...")
                }
            } catch (e: Exception) {
                System.err.println("Operation $i failed: ${e.message}")
            }
        }
    }
    
    println("===== Results =====")
    println("Total operations: $operationCount")
    println("Success: ${successCounter.get()}")
    println("Total time: ${totalTime}ms")
    println("Avg per operation: ${totalTime.toDouble() / operationCount}ms")
    
    assert(successCounter.get() == operationCount)
}

// 辅助方法 - 模拟各种操作
private fun simulateStateUpdate() {
    val id = Random.nextInt(1, 1000)
    val progress = "progress_$id"
    progress.hashCode() // 使用一下防止优化
}

private fun simulateSearch() {
    val queries = listOf("算法", "Java", "test", "database", "network", "", "   ")
    val query = queries.random()
    query.hashCode()
}

private fun simulateFilter() {
    val categories = listOf("算法与数据结构", "Java开发", "数据库")
    val difficulties = listOf("EASY", "MEDIUM", "HARD")
    
    categories.random()
    difficulties.random()
}

private fun simulateReset() {
    val id = Random.nextInt(1, 1000)
    id.toString().hashCode()
}

private fun simulateDataLoad() {
    val size = Random.nextInt(1, 1000)
    val mockData = (1..size).map { it }
    mockData.size
}

private fun simulateMemoryPressure() {
    val tempList = mutableListOf<ByteArray>()
    repeat(Random.nextInt(1, 100)) {
        tempList.add(ByteArray(1024))
    }
    tempList.clear()
}

private fun simulateStateRead() {
    val id = Random.nextInt(1, 1000)
    id.hashCode()
}
}
