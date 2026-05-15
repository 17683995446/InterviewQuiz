
package com.interviewquiz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.interviewquiz.ui.theme.*

@Composable
fun ResultScreen(
    correctCount: Int,
    totalCount: Int,
    onRestart: () -> Unit,
    onBackHome: () -> Unit
) {
    val percentage = if (totalCount > 0) {
        (correctCount.toFloat() / totalCount.toFloat() * 100).toInt()
    } else {
        0
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("测试结果", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackHome) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ResultHeader(percentage = percentage)
            ScoreCard(correct = correctCount, total = totalCount, percentage = percentage)
            SummaryMessage(percentage = percentage)
            Spacer(modifier = Modifier.weight(1f))
            ActionButtons(onRestart = onRestart, onBackHome = onBackHome)
        }
    }
}

@Composable
fun ResultHeader(percentage: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val emoji = when {
            percentage &gt;= 80 -&gt; "🎉"
            percentage &gt;= 60 -&gt; "👏"
            percentage &gt;= 40 -&gt; "💪"
            else -&gt; "📚"
        }
        
        Text(emoji, fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Quiz Complete!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ScoreCard(correct: Int, total: Int, percentage: Int) {
    val color = when {
        percentage &gt;= 80 -&gt; Green
        percentage &gt;= 60 -&gt; Orange
        else -&gt; Red
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$percentage%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "$correct of $total correct",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun SummaryMessage(percentage: Int) {
    val message = when {
        percentage &gt;= 80 -&gt; "Excellent! You're well-prepared for your interview!"
        percentage &gt;= 60 -&gt; "Good job! Keep practicing to improve further!"
        percentage &gt;= 40 -&gt; "Not bad! Focus on understanding the explanations!"
        else -&gt; "Keep going! Review the explanations and try again!"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(20.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 28.sp
        )
    }
}

@Composable
fun ActionButtons(onRestart: () -&gt; Unit, onBackHome: () -&gt; Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again", fontWeight = FontWeight.Bold)
        }
        
        OutlinedButton(
            onClick = onBackHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Home, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back to Home", fontWeight = FontWeight.Bold)
        }
    }
}
