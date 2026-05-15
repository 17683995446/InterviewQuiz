package com.interviewquiz.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.interviewquiz.R
import com.interviewquiz.ui.theme.InterviewQuizTheme
import com.interviewquiz.ui.theme.Primary
import com.interviewquiz.ui.theme.PrimaryDark
import com.interviewquiz.ui.theme.PrimaryLight

@Composable
fun HomeScreen(
    onStartQuiz: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val quickActions = listOf(
        QuickAction("📝", "开始练习", "开始答题", onStartQuiz),
        QuickAction("📚", "浏览题目", "所有分类", { onNavigate("categories") }),
        QuickAction("📊", "学习统计", "查看进度", { onNavigate("statistics") }),
        QuickAction("⭐", "我的收藏", "收藏夹", { onNavigate("bookmarks") })
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = PrimaryLight
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🚀 InterviewQuiz",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "人工智能/计算机岗位面试刷题",
                    fontSize = 16.sp,
                    color = PrimaryDark,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "准备好你的面试了吗？\n通过练习提升你的技能！",
                    fontSize = 14.sp,
                    color = PrimaryDark.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Quick Actions
        Text(
            "快速操作",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(quickActions) { action ->
                QuickActionCard(action = action)
            }
        }
    }
}

@Composable
fun QuickActionCard(action: QuickAction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = action.onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                action.icon,
                fontSize = 36.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                Text(
                    action.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    action.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

data class QuickAction(
    val icon: String,
    val title: String,
    val description: String,
    val onClick: () -> Unit
)

@Preview(showBackground = true, name = "Home Screen Light")
@Composable
fun HomeScreenPreview() {
    InterviewQuizTheme {
        HomeScreen(
            onStartQuiz = {},
            onNavigate = {}
        )
    }
}

@Preview(showBackground = true, name = "Home Screen Dark")
@Composable
fun HomeScreenDarkPreview() {
    InterviewQuizTheme(darkTheme = true) {
        HomeScreen(
            onStartQuiz = {},
            onNavigate = {}
        )
    }
}
