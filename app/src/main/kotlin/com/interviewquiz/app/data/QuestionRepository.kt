package com.interviewquiz.app.data

import javax.inject.Inject
import javax.inject.Singleton

interface QuestionRepository {
    fun getQuestions(): List<Question>
    fun getQuestionsByCategory(category: String): List<Question>
    fun getQuestionsByDifficulty(difficulty: String): List<Question>
    fun getCategories(): List<String>
    fun getDifficulties(): List<String>
    fun getQuestionCount(): Int
    fun getQuestionsPaginated(page: Int, pageSize: Int): List<Question>
    fun searchQuestions(query: String): List<Question>
}

@Singleton
class QuestionRepositoryImpl @Inject constructor() : QuestionRepository {
    
    private val allQuestions: List<Question> by lazy { generateAllQuestions() }
    
    private val categoryCache: Map<String, List<Question>> by lazy {
        allQuestions.groupBy { it.category }
    }
    
    private val difficultyCache: Map<String, List<Question>> by lazy {
        allQuestions.groupBy { it.difficulty }
    }
    
    private val categories: List<String> by lazy {
        allQuestions.map { it.category }.distinct().sorted()
    }
    
    private val difficultyLevels: List<String> = listOf("EASY", "MEDIUM", "HARD")
    
    override fun getQuestions(): List<Question> = allQuestions
    
    override fun getQuestionCount(): Int = allQuestions.size
    
    override fun getQuestionsByCategory(category: String): List<Question> {
        return categoryCache[category] ?: emptyList()
    }
    
    override fun getQuestionsByDifficulty(difficulty: String): List<Question> {
        return difficultyCache[difficulty] ?: emptyList()
    }
    
    override fun getCategories(): List<String> = categories
    
    override fun getDifficulties(): List<String> = difficultyLevels
    
    override fun getQuestionsPaginated(page: Int, pageSize: Int): List<Question> {
        val startIndex = page * pageSize
        if (startIndex >= allQuestions.size) return emptyList()
        val endIndex = minOf(startIndex + pageSize, allQuestions.size)
        return allQuestions.subList(startIndex, endIndex)
    }
    
    override fun searchQuestions(query: String): List<Question> {
        if (query.isBlank()) return allQuestions
        val lowerQuery = query.lowercase()
        return allQuestions.filter { question ->
            question.title.lowercase().contains(lowerQuery) ||
            question.category.lowercase().contains(lowerQuery) ||
            question.explanation.lowercase().contains(lowerQuery) ||
            question.options.any { it.lowercase().contains(lowerQuery) }
        }
    }
    
    private fun generateAllQuestions(): List<Question> {
        return generateAlgorithmQuestions() + 
               generateJavaQuestions() + 
               generateDatabaseQuestions() + 
               generateNetworkQuestions() + 
               generateOSQuestions() + 
               generateAndroidQuestions()
    }
    
    private fun generateAlgorithmQuestions(): List<Question> = listOf(
        Question(1, "什么是时间复杂度？", listOf("衡量算法执行时间与输入规模关系的指标", "算法代码的长度", "算法消耗的内存大小", "算法执行的实际时间"), 0, "时间复杂度用大O表示法描述算法效率，衡量执行时间与输入规模的关系。", "算法与数据结构", "EASY"),
        Question(2, "大O表示法O(n)表示什么？", listOf("算法执行时间随输入规模线性增长", "算法执行时间为常数", "算法执行时间指数增长", "算法执行时间对数增长"), 0, "O(n)表示线性时间复杂度，执行时间与输入规模成正比。", "算法与数据结构", "EASY"),
        Question(3, "以下哪种排序算法的时间复杂度是O(n log n)？", listOf("快速排序", "冒泡排序", "选择排序", "插入排序"), 0, "快速排序平均时间复杂度是O(n log n)，最坏情况是O(n²)。", "算法与数据结构", "EASY"),
        Question(4, "数组和链表的主要区别是什么？", listOf("数组支持随机访问，链表支持快速插入删除", "数组内存不连续，链表内存连续", "数组可以动态扩容，链表不可以", "链表访问更快"), 0, "数组O(1)访问但O(n)插入，链表O(1)插入但O(n)访问。", "算法与数据结构", "MEDIUM"),
        Question(5, "栈的数据结构特点是？", listOf("后进先出(LIFO)", "先进先出(FIFO)", "随机访问", "无序存储"), 0, "栈是后进先出的数据结构，元素只能从栈顶操作。", "算法与数据结构", "EASY"),
        Question(6, "二叉搜索树的中序遍历结果是什么？", listOf("有序序列", "逆序序列", "随机序列", "无序序列"), 0, "BST中序遍历会得到一个递增有序的序列。", "算法与数据结构", "MEDIUM"),
        Question(7, "排序算法的稳定性指的是什么？", listOf("相等元素排序后相对位置不变", "排序算法不会崩溃", "排序时间稳定", "排序空间稳定"), 0, "稳定性指相等元素的相对位置在排序后保持不变。", "算法与数据结构", "MEDIUM"),
        Question(8, "以下哪种排序算法不是比较排序？", listOf("计数排序", "快速排序", "归并排序", "堆排序"), 0, "计数排序、基数排序、桶排序是非比较排序。", "算法与数据结构", "MEDIUM"),
        Question(9, "二分查找的前提条件是什么？", listOf("数组必须是有序的", "数组必须是无序的", "数组必须是连续内存", "数组必须是整数数组"), 0, "二分查找要求数组必须有序，才能通过比较缩小搜索范围。", "算法与数据结构", "EASY"),
        Question(10, "BFS和DFS的区别是什么？", listOf("BFS用队列，DFS用栈", "BFS用栈，DFS用队列", "BFS更快，DFS更慢", "BFS空间更小"), 0, "BFS使用队列按层遍历，DFS使用栈或递归深入遍历。", "算法与数据结构", "MEDIUM"),
        Question(11, "动态规划的核心思想是什么？", listOf("将问题分解为子问题并存储结果避免重复计算", "贪心选择局部最优解", "分治法将问题划分为独立子问题", "回溯法枚举所有可能解"), 0, "动态规划通过存储子问题解来避免重复计算。", "算法与数据结构", "HARD"),
        Question(12, "以下哪个是典型的动态规划问题？", listOf("斐波那契数列", "快速排序", "二分查找", "二叉树遍历"), 0, "斐波那契数列具有重叠子问题特性，是DP经典入门题。", "算法与数据结构", "MEDIUM"),
        Question(13, "HashMap在JDK8中引入了什么改进？", listOf("链表过长时转为红黑树", "完全重写了实现", "移除了hash冲突处理", "使用了数组"), 0, "JDK8当链表长度超过8且容量超过64时转为红黑树。", "算法与数据结构", "HARD"),
        Question(14, "什么情况下快速排序性能最差？", listOf("数组已经有序", "数组完全逆序", "数组随机分布", "数组元素都相同"), 0, "有序数组会导致快速排序退化为O(n²)。", "算法与数据结构", "HARD"),
        Question(15, "堆排序的空间复杂度是？", listOf("O(1)", "O(n)", "O(n log n)", "O(log n)"), 0, "堆排序是原地排序，空间复杂度为O(1)。", "算法与数据结构", "MEDIUM"),
        Question(16, "图的深度优先搜索使用什么数据结构？", listOf("栈", "队列", "堆", "数组"), 0, "DFS使用栈或递归实现，遍历图的所有可达节点。", "算法与数据结构", "MEDIUM"),
        Question(17, "什么是最小生成树？", listOf("连接所有顶点且边权值之和最小的树", "最短路径", "环", "所有边的集合"), 0, "最小生成树用Prim或Kruskal算法求解。", "算法与数据结构", "HARD"),
        Question(18, "Dijkstra算法不能处理哪种情况？", listOf("存在负权边的图", "所有边权值为正", "稀疏图", "稠密图"), 0, "Dijkstra算法基于贪心，负权边会导致错误结果。", "算法与数据结构", "HARD"),
        Question(19, "AVL树和普通二叉搜索树的主要区别是？", listOf("AVL树是自平衡的", "AVL树查找更快", "AVL树插入更快", "没有区别"), 0, "AVL树通过旋转保持平衡，保证O(log n)查找。", "算法与数据结构", "HARD"),
        Question(20, "布隆过滤器的主要用途是？", listOf("快速判断元素是否可能存在", "精确查找元素", "排序", "压缩数据"), 0, "布隆过滤器可以快速判断元素一定不存在或可能存在。", "算法与数据结构", "HARD")
    )
    
    private fun generateJavaQuestions(): List<Question> = listOf(
        Question(21, "Java中int和Integer的区别是什么？", listOf("int是基本类型，Integer是包装类", "int是包装类，Integer是基本类型", "没有区别", "int不可以为null"), 0, "int是原始类型，直接存储数值；Integer是包装类，提供对象操作和null支持。", "Java与Kotlin", "EASY"),
        Question(22, "ArrayList和LinkedList的主要区别是什么？", listOf("ArrayList基于动态数组，LinkedList基于双向链表", "LinkedList访问更快", "ArrayList插入删除更快", "没有区别"), 0, "ArrayList支持O(1)随机访问，LinkedList支持O(1)插入删除。", "Java与Kotlin", "EASY"),
        Question(23, "Kotlin的空安全特性通过什么符号表示可空类型？", listOf("?", "!", "Nullable", "@Nullable"), 0, "Kotlin使用?后缀表示类型可空，如String?。", "Java与Kotlin", "EASY"),
        Question(24, "synchronized关键字的作用是什么？", listOf("保证同一时间只有一个线程执行被保护的代码", "提高代码执行速度", "使代码更简洁", "没有作用"), 0, "synchronized实现线程互斥，确保同时只有一个线程执行。", "Java与Kotlin", "MEDIUM"),
        Question(25, "volatile关键字的主要作用是？", listOf("保证可见性和禁止指令重排序", "保证原子性", "保证线程安全", "让变量变得更稳定"), 0, "volatile保证多线程间可见性并防止指令重排序，但不保证原子性。", "Java与Kotlin", "MEDIUM"),
        Question(26, "ThreadPoolExecutor的核心参数不包括？", listOf("maxThreads", "corePoolSize", "keepAliveTime", "workQueue"), 0, "参数名是maximumPoolSize，不是maxThreads。", "Java与Kotlin", "HARD"),
        Question(27, "单例模式的主要用途是什么？", listOf("确保一个类只有一个实例", "创建多个实例", "提高执行速度", "减少内存占用"), 0, "单例模式确保一个类在整个应用只有一个实例。", "Java与Kotlin", "EASY"),
        Question(28, "观察者模式定义了什么关系？", listOf("一对多的依赖关系", "一对一的关系", "多对多的关系", "没有关系"), 0, "观察者模式定义对象间一对多的依赖关系。", "Java与Kotlin", "MEDIUM"),
        Question(29, "工厂模式的主要优点是什么？", listOf("将对象的创建和使用分离", "让代码更复杂", "提高执行速度", "减少类数量"), 0, "工厂模式将对象创建封装在工厂类中，客户端无需了解实现细节。", "Java与Kotlin", "MEDIUM"),
        Question(30, "data class的主要用途是什么？", listOf("自动生成equals、hashCode、toString等方法", "存储数据", "创建单例", "继承其他类"), 0, "data class用于数据类，自动生成标准方法，适合POJO。", "Java与Kotlin", "EASY"),
        Question(31, "协程的主要优势是什么？", listOf("比线程更轻量，挂起不阻塞线程", "更快的执行速度", "不需要考虑并发问题", "完全替代线程"), 0, "协程比线程更轻量，可以高效管理大量并发任务。", "Java与Kotlin", "MEDIUM"),
        Question(32, "Java中的HashMap底层实现原理是？", listOf("数组+链表/红黑树", "只有链表", "只有数组", "只有红黑树"), 0, "JDK8使用数组+链表，当链表过长时转为红黑树优化查找。", "Java与Kotlin", "HARD"),
        Question(33, "什么是JVM的垃圾回收？", listOf("自动回收不再使用的内存", "手动释放内存", "压缩硬盘空间", "备份数据"), 0, "JVM通过垃圾回收自动回收不再引用的对象占用的内存。", "Java与Kotlin", "MEDIUM"),
        Question(34, "Java中sleep()和wait()的区别是？", listOf("sleep不释放锁，wait释放锁", "sleep释放锁，wait不释放锁", "没有区别", "两者功能相同"), 0, "sleep不释放对象锁，wait会释放对象锁。", "Java与Kotlin", "HARD"),
        Question(35, "什么是反射机制？", listOf("在运行时动态获取类的信息", "编译时检查类型", "快速排序", "加密算法"), 0, "反射允许程序在运行时动态获取和操作类的信息。", "Java与Kotlin", "HARD"),
        Question(36, "Kotlin中的by lazy是什么？", listOf("延迟初始化属性", "快速初始化", "空安全检查", "异步执行"), 0, "by lazy延迟初始化属性，第一次访问时才初始化。", "Java与Kotlin", "MEDIUM"),
        Question(37, "Java中如何避免死锁？", listOf("按固定顺序获取锁，设置锁超时", "使用更多的锁", "减少线程数量", "增加内存"), 0, "死锁预防包括固定加锁顺序、使用超时、减少锁粒度等。", "Java与Kotlin", "HARD"),
        Question(38, "什么是AQS？", listOf("抽象队列同步器，用于构建锁和同步器", "抽象查询语言", "异步队列服务", "原子操作类"), 0, "AQS是Java并发包的基础，ReentrantLock等基于它实现。", "Java与Kotlin", "HARD"),
        Question(39, "HashSet底层实现基于什么？", listOf("HashMap", "ArrayList", "LinkedList", "TreeMap"), 0, "HashSet底层使用HashMap存储元素，value使用固定对象。", "Java与Kotlin", "MEDIUM"),
        Question(40, "Kotlin中的when表达式替代了Java的什么？", listOf("switch语句", "if-else", "循环", "异常处理"), 0, "when比switch更强大，支持任意类型和条件。", "Java与Kotlin", "EASY")
    )
    
    private fun generateDatabaseQuestions(): List<Question> = listOf(
        Question(41, "SQL中用于从表中查询数据的语句是？", listOf("SELECT", "INSERT", "UPDATE", "DELETE"), 0, "SELECT用于从数据库查询数据。", "数据库", "EASY"),
        Question(42, "WHERE子句的作用是？", listOf("过滤查询条件", "指定查询的表", "排序结果", "分组结果"), 0, "WHERE用于设置查询条件，过滤结果。", "数据库", "EASY"),
        Question(43, "ACID中的C代表什么？", listOf("一致性(Consistency)", "并发(Concurrency)", "缓存(Cache)", "控制(Control)"), 0, "ACID是原子性、一致性、隔离性、持久性。", "数据库", "MEDIUM"),
        Question(44, "MySQL默认的事务隔离级别是？", listOf("REPEATABLE READ", "READ UNCOMMITTED", "READ COMMITTED", "SERIALIZABLE"), 0, "MySQL InnoDB默认隔离级别是REPEATABLE READ。", "数据库", "MEDIUM"),
        Question(45, "脏读是指什么？", listOf("读取其他事务未提交的数据", "读取到垃圾数据", "读取到错误数据", "重复读取"), 0, "脏读是读取到其他事务未提交的数据。", "数据库", "MEDIUM"),
        Question(46, "索引的主要作用是？", listOf("提高查询速度", "加快插入速度", "增加存储空间", "提高数据安全性"), 0, "索引通过创建辅助数据结构加快查询速度。", "数据库", "EASY"),
        Question(47, "MySQL InnoDB使用什么数据结构作为索引？", listOf("B+树", "B树", "哈希表", "红黑树"), 0, "InnoDB使用B+树作为索引结构，适合范围查询。", "数据库", "MEDIUM"),
        Question(48, "覆盖索引是指？", listOf("索引包含查询所需的所有列，无需回表", "覆盖了所有表数据的索引", "覆盖所有查询的索引", "主键索引"), 0, "覆盖索引包含查询所需所有列，可直接返回结果。", "数据库", "HARD"),
        Question(49, "视图(View)的特点是？", listOf("虚拟表，不实际存储数据", "存储真实数据", "比普通表查询更快", "不可以被更新"), 0, "视图是基于SQL的虚拟表，每次查询时动态计算。", "数据库", "MEDIUM"),
        Question(50, "EXPLAIN语句用于？", listOf("查看SQL执行计划，分析性能", "执行SQL语句", "创建执行计划", "优化SQL语句"), 0, "EXPLAIN用于分析SQL执行计划，帮助优化查询。", "数据库", "MEDIUM"),
        Question(51, "数据库事务的四大特性不包括？", listOf("原子性", "一致性", "隔离性", "共享性"), 0, "ACID是原子性、一致性、隔离性、持久性。", "数据库", "EASY"),
        Question(52, "什么是数据库的规范化？", listOf("减少数据冗余，消除插入异常", "加快查询速度", "增加数据安全", "备份数据"), 0, "规范化通过分解表减少冗余，提高数据完整性。", "数据库", "MEDIUM"),
        Question(53, "JOIN的类型不包括？", listOf("CROSS JOIN", "INNER JOIN", "LEFT JOIN", "RIGHT JOIN"), 0, "内连接、左连接、右连接是常见类型。", "数据库", "EASY"),
        Question(54, "什么是SQL注入攻击？", listOf("通过输入恶意SQL代码获取数据库权限", "数据库性能问题", "数据丢失", "网络中断"), 0, "SQL注入通过拼接用户输入执行恶意SQL语句。", "数据库", "MEDIUM"),
        Question(55, "MySQL中VARCHAR和CHAR的区别是？", listOf("VARCHAR变长，CHAR定长", "CHAR更快", "VARCHAR更占空间", "没有区别"), 0, "CHAR定长适合固定长度，VARCHAR变长节省空间。", "数据库", "MEDIUM"),
        Question(56, "什么是数据库的分区？", listOf("将大表分成小表，提高查询效率", "备份数据库", "加密数据", "压缩数据"), 0, "分区将大表逻辑分成小表，提高管理和查询效率。", "数据库", "HARD"),
        Question(57, "Redis和MySQL的主要区别是？", listOf("Redis是内存数据库，MySQL是磁盘数据库", "Redis更安全", "MySQL更快", "没有区别"), 0, "Redis数据存内存，适合缓存；MySQL数据存磁盘，适合持久化。", "数据库", "MEDIUM"),
        Question(58, "什么是主从复制？", listOf("主库写，从库读，提高可用性", "两个数据库同时写", "数据备份", "数据加密"), 0, "主从复制通过读写分离提高性能和可用性。", "数据库", "HARD"),
        Question(59, "COUNT(*)和COUNT(1)的区别是？", listOf("性能几乎相同", "COUNT(*)更快", "COUNT(1)更快", "COUNT(*)会忽略NULL"), 0, "在InnoDB中，两者性能几乎相同。", "数据库", "MEDIUM"),
        Question(60, "什么是存储过程？", listOf("预编译的SQL代码块，可重复调用", "临时表", "索引", "触发器"), 0, "存储过程是预编译的SQL代码块，减少网络传输。", "数据库", "MEDIUM")
    )
    
    private fun generateNetworkQuestions(): List<Question> = listOf(
        Question(61, "OSI七层模型中网络层的主要功能是？", listOf("寻址和路由选择", "建立可靠连接", "数据表示", "用户界面"), 0, "网络层负责IP寻址和路由选择。", "网络基础", "MEDIUM"),
        Question(62, "TCP/IP四层模型不包括？", listOf("会话层", "网络接口层", "网际层", "传输层"), 0, "TCP/IP四层包括：网络接口层、网际层、传输层、应用层。", "网络基础", "MEDIUM"),
        Question(63, "TCP和UDP的主要区别是？", listOf("TCP面向连接、可靠，UDP无连接、不可靠", "TCP更快", "UDP更可靠", "没有区别"), 0, "TCP提供可靠连接，UDP提供快速无连接传输。", "网络基础", "EASY"),
        Question(64, "TCP三次握手的目的是？", listOf("建立可靠的连接", "发送数据", "断开连接", "确认连接"), 0, "三次握手确保双方都能正常收发数据。", "网络基础", "MEDIUM"),
        Question(65, "HTTP状态码404表示什么？", listOf("资源未找到", "服务器错误", "成功", "未授权"), 0, "404 Not Found表示请求的资源在服务器上未找到。", "网络基础", "EASY"),
        Question(66, "HTTPS相比HTTP的主要改进是？", listOf("增加了安全性，数据加密传输", "更快的速度", "更好的兼容性", "更大的请求限制"), 0, "HTTPS在HTTP基础上增加了TLS/SSL层提供加密。", "网络基础", "EASY"),
        Question(67, "HTTP GET和POST的主要区别是？", listOf("GET在URL中传参，POST在请求体中传参", "POST更快", "GET更安全", "没有区别"), 0, "GET参数在URL中，POST参数在请求体中更安全。", "网络基础", "EASY"),
        Question(68, "DNS的作用是？", listOf("将域名解析为IP地址", "加密数据", "加速网络", "压缩数据"), 0, "DNS将域名解析为IP地址。", "网络基础", "EASY"),
        Question(69, "WebSocket的主要优势是？", listOf("全双工通信，服务器可以主动推送数据", "更快的HTTP请求", "更好的兼容性", "更小的头部"), 0, "WebSocket支持全双工通信，服务器可主动推送。", "网络基础", "MEDIUM"),
        Question(70, "TCP四次挥手断开连接的目的是？", listOf("确保双方都发送完数据", "加快连接速度", "建立连接", "加密数据"), 0, "四次挥手确保双方都发送完数据后才断开连接。", "网络基础", "MEDIUM"),
        Question(71, "什么是非对称加密？", listOf("使用公钥和私钥进行加密解密", "加密和解密使用相同密钥", "不需要密钥", "只有解密"), 0, "非对称加密使用公钥加密、私钥解密，或反之。", "网络基础", "MEDIUM"),
        Question(72, "CDN的核心机制是？", listOf("边缘缓存，就近访问", "增加带宽", "提高服务器性能", "压缩传输"), 0, "CDN通过边缘节点缓存内容，让用户就近访问。", "网络基础", "MEDIUM"),
        Question(73, "HTTP/2相比HTTP/1.1的主要改进是？", listOf("多路复用，头部压缩", "更简单的协议", "明文传输", "更短的数据格式"), 0, "HTTP/2支持多路复用，头部压缩提升性能。", "网络基础", "HARD"),
        Question(74, "什么是TCP的流量控制？", listOf("滑动窗口机制控制发送速率", "加快传输速度", "减少网络延迟", "防止死锁"), 0, "流量控制通过滑动窗口确保发送方不超过接收方处理能力。", "网络基础", "HARD"),
        Question(75, "Cookie和Session的主要区别是？", listOf("Cookie存客户端，Session存服务器", "Session存客户端", "Cookie更安全", "没有区别"), 0, "Cookie在客户端存储，Session在服务器端存储。", "网络基础", "MEDIUM"),
        Question(76, "什么是SYN Flood攻击？", listOf("发送大量SYN请求耗尽服务器资源", "窃取用户密码", "篡改网页内容", "删除数据库"), 0, "SYN Flood通过发送大量半连接耗尽服务器资源。", "网络基础", "HARD"),
        Question(77, "RESTful API设计原则不包括？", listOf("无状态，使用HTTP方法表示操作", "有状态", "资源导向URL", "使用HTTP状态码"), 0, "RESTful要求无状态，使用标准HTTP方法。", "网络基础", "MEDIUM"),
        Question(78, "TCP的拥塞控制算法不包括？", listOf("随机退让", "慢开始", "拥塞避免", "快重传快恢复"), 0, "拥塞控制包括慢开始、拥塞避免、快重传等算法。", "网络基础", "HARD"),
        Question(79, "ARP协议的作用是？", listOf("将IP地址解析为MAC地址", "将域名解析为IP", "加密数据", "路由选择"), 0, "ARP将IP地址解析为局域网中的MAC地址。", "网络基础", "HARD"),
        Question(80, "负载均衡的目的是？", listOf("分发请求到多台服务器，提高性能和可用性", "加快单台服务器速度", "减少数据存储", "提高安全性"), 0, "负载均衡将请求分发到多台服务器，提高性能和可用性。", "网络基础", "MEDIUM")
    )
    
    private fun generateOSQuestions(): List<Question> = listOf(
        Question(81, "操作系统的主要功能不包括？", listOf("编译源代码", "进程管理", "内存管理", "文件管理"), 0, "操作系统不负责编译源代码，那是编译器的工作。", "操作系统", "EASY"),
        Question(82, "什么是系统调用？", listOf("用户程序请求操作系统服务的接口", "函数调用", "系统崩溃", "数据库查询"), 0, "系统调用是用户态与内核态的桥梁。", "操作系统", "MEDIUM"),
        Question(83, "进程和线程的区别是？", listOf("进程是资源分配基本单位，线程是调度基本单位", "进程更快", "线程消耗更多资源", "没有区别"), 0, "进程拥有独立资源，线程共享进程资源。", "操作系统", "EASY"),
        Question(84, "死锁的四个必要条件不包括？", listOf("并发条件", "互斥条件", "请求与保持", "循环等待"), 0, "死锁必要条件：互斥、请求与保持、不可抢占、循环等待。", "操作系统", "HARD"),
        Question(85, "虚拟内存的主要优势是？", listOf("允许进程使用超过物理内存的地址空间", "提高内存访问速度", "减少内存占用", "简化操作系统"), 0, "虚拟内存通过页面置换支持使用比物理内存更大的地址空间。", "操作系统", "MEDIUM"),
        Question(86, "LRU页面置换算法是指？", listOf("最近最少使用", "先进先出", "最不常用", "最佳置换"), 0, "LRU置换最长时间未使用的页面。", "操作系统", "MEDIUM"),
        Question(87, "什么是分页机制？", listOf("将虚拟内存和物理内存划分为固定大小的页", "文件存储方式", "进程调度算法", "磁盘分区"), 0, "分页将地址空间划分为固定大小的页便于管理。", "操作系统", "MEDIUM"),
        Question(88, "进程调度算法FCFS是指？", listOf("先来先服务", "最短作业优先", "时间片轮转", "优先级调度"), 0, "FCFS按到达顺序执行，不考虑执行时间。", "操作系统", "EASY"),
        Question(89, "什么是线程同步？", listOf("协调多个线程访问共享资源", "加快线程执行", "创建更多线程", "终止线程"), 0, "线程同步确保共享资源在多线程环境下正确访问。", "操作系统", "MEDIUM"),
        Question(90, "内核态和用户态的区别是？", listOf("内核态可以访问所有资源，用户态权限受限", "用户态更快", "内核态更安全", "没有区别"), 0, "内核态有最高权限，用户态必须通过系统调用请求服务。", "操作系统", "MEDIUM"),
        Question(91, "什么是信号量？", listOf("用于进程/线程同步的计数器", "存储进程信息", "中断信号", "输入输出设备"), 0, "信号量是用于同步的计数器，支持P/V操作。", "操作系统", "HARD"),
        Question(92, "银行家算法用于解决什么问题？", listOf("死锁预防", "内存管理", "进程调度", "文件管理"), 0, "银行家算法通过预先检查资源分配防止系统进入不安全状态。", "操作系统", "HARD"),
        Question(93, "页面置换算法的Belady异常是指？", listOf("增加物理页框数反而导致缺页率增加", "缺页率总是增加", "内存泄漏", "进程崩溃"), 0, "Belady异常是指某些置换算法增加页框数反而增加缺页率。", "操作系统", "HARD"),
        Question(94, "文件系统的索引节点(Inode)包含什么信息？", listOf("文件的元数据，不包括文件名", "文件名和内容", "只有文件大小", "文件权限"), 0, "Inode存储文件元数据，文件名存储在目录中。", "操作系统", "HARD"),
        Question(95, "什么是用户级线程？", listOf("线程在用户空间管理，内核不可见", "线程由内核管理", "比内核级线程更快", "不能创建"), 0, "用户级线程由库管理，上下文切换快但不能利用多核。", "操作系统", "HARD"),
        Question(96, "SPOOLing技术用于什么？", listOf("将独占设备改为共享设备", "加快CPU速度", "增加内存", "备份数据"), 0, "SPOOLing通过缓冲将独占设备改造成共享设备。", "操作系统", "HARD"),
        Question(97, "什么是地址重定位？", listOf("将逻辑地址转换为物理地址", "复制数据", "加密地址", "压缩数据"), 0, "地址重定位将程序逻辑地址映射到物理地址。", "操作系统", "MEDIUM"),
        Question(98, "操作系统中管程的作用是？", listOf("封装共享资源，方便同步", "加快执行速度", "增加内存", "减少代码量"), 0, "管程将共享资源及其操作封装，简化并发编程。", "操作系统", "HARD"),
        Question(99, "什么是抢先式多任务？", listOf("操作系统强制暂停进程分配CPU", "进程主动放弃CPU", "只有一个进程运行", "禁止多任务"), 0, "抢先式多任务通过时间片轮转强制分配CPU。", "操作系统", "MEDIUM"),
        Question(100, "文件系统挂载的含义是？", listOf("将文件系统连接到目录树", "删除文件系统", "格式化", "备份"), 0, "挂载将文件系统连接到根文件系统指定目录。", "操作系统", "MEDIUM")
    )
    
    private fun generateAndroidQuestions(): List<Question> = listOf(
        Question(101, "Activity的启动方法是什么？", listOf("startActivity()", "launchActivity()", "openActivity()", "start()"), 0, "startActivity()用于启动Activity。", "Android开发", "EASY"),
        Question(102, "Activity生命周期的第一个回调是？", listOf("onCreate()", "onStart()", "onResume()", "onInit()"), 0, "onCreate是Activity生命周期第一个回调。", "Android开发", "EASY"),
        Question(103, "Intent的主要用途不包括？", listOf("数据持久化", "启动Activity", "启动Service", "发送广播"), 0, "Intent用于组件通信，不用于数据持久化。", "Android开发", "MEDIUM"),
        Question(104, "ViewModel的主要作用是什么？", listOf("在配置变更时保存UI相关数据", "处理网络请求", "数据持久化", "替代Activity"), 0, "ViewModel在配置变更时保存数据。", "Android开发", "MEDIUM"),
        Question(105, "Room数据库是什么？", listOf("Android Jetpack的SQLite对象映射库", "NoSQL数据库", "网络数据库", "缓存库"), 0, "Room是Jetpack提供的SQLite抽象层。", "Android开发", "MEDIUM"),
        Question(106, "RecyclerView相比ListView的优势是？", listOf("ViewHolder复用更高效，支持多种布局", "更少的代码", "不需要适配器", "自动处理点击事件"), 0, "RecyclerView有更高效的ViewHolder复用机制。", "Android开发", "MEDIUM"),
        Question(107, "什么是ANR？", listOf("应用无响应（Application Not Responding）", "网络异常", "内存溢出", "系统崩溃"), 0, "ANR在主线程阻塞超过5秒时触发。", "Android开发", "MEDIUM"),
        Question(108, "内存泄漏的主要原因是？", listOf("生命周期长的对象持有短生命周期对象的引用", "内存不足", "代码写得不好", "系统问题"), 0, "内存泄漏因长生命周期对象持有短生命周期对象引用。", "Android开发", "MEDIUM"),
        Question(109, "Jetpack Navigation的作用是？", listOf("简化Fragment和Activity间的导航", "处理网络请求", "数据持久化", "UI渲染"), 0, "Navigation组件提供统一的导航管理方式。", "Android开发", "MEDIUM"),
        Question(110, "Kotlin中let和also的区别是？", listOf("let返回lambda结果，also返回调用者", "没有区别", "also更快", "let不能链式调用"), 0, "let返回lambda结果用于链式调用，also返回调用者。", "Android开发", "HARD"),
        Question(111, "Handler的主要作用是？", listOf("在线程间传递消息", "启动Activity", "存储数据", "加密数据"), 0, "Handler用于在线程间传递消息和执行代码。", "Android开发", "MEDIUM"),
        Question(112, "什么情况下会触发onSaveInstanceState？", listOf("Activity被销毁前", "Activity创建时", "Activity可见时", "Activity获得焦点时"), 0, "onSaveInstanceState在Activity被销毁前调用保存数据。", "Android开发", "HARD"),
        Question(113, "ConstraintLayout的主要优势是？", listOf("灵活的相对定位，减少布局层级", "性能更好", "更易学习", "支持更多控件"), 0, "ConstraintLayout通过相对定位减少布局层级提高性能。", "Android开发", "MEDIUM"),
        Question(114, "什么是LiveData？", listOf("可观察的数据持有者类", "后台线程", "网络库", "数据库"), 0, "LiveData是生命周期感知的可观察数据类。", "Android开发", "MEDIUM"),
        Question(115, "WorkManager用于什么场景？", listOf("执行可延迟的后台任务", "即时任务", "UI操作", "数据存储"), 0, "WorkManager适合执行可延迟、可靠的后台任务。", "Android开发", "MEDIUM"),
        Question(116, "ViewStub的作用是？", listOf("延迟加载布局，按需inflate", "永久隐藏视图", "加快布局加载", "替代include"), 0, "ViewStub在需要时才加载布局，节省资源。", "Android开发", "HARD"),
        Question(117, "Kotlin Flow相比LiveData的优势是？", listOf("支持背压，更丰富的操作符", "更简单", "更稳定", "不需要协程"), 0, "Flow支持背压和更多操作符，适合数据流处理。", "Android开发", "HARD"),
        Question(118, "什么是SurfaceView？", listOf("在独立线程渲染，适合视频和游戏", "普通视图", "图像处理", "相机预览"), 0, "SurfaceView在独立线程渲染，适合高性能场景。", "Android开发", "HARD"),
        Question(119, "注解@ColorInt的作用是？", listOf("表示颜色是ARGB整数格式", "标记颜色资源", "加密颜色", "压缩颜色"), 0, "@ColorInt注解表示颜色是整数格式。", "Android开发", "HARD"),
        Question(120, "Parcelable和Serializable的区别是？", listOf("Parcelable更快，Serializable使用反射", "Parcelable更慢", "没有区别", "Serializable支持所有平台"), 0, "Parcelable通过手动序列化实现，速度更快。", "Android开发", "HARD")
    )
}
