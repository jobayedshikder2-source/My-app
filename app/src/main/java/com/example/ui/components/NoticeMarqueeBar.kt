package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Announcement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoticeMarqueeBar(
    announcement: Announcement?,
    modifier: Modifier = Modifier
) {
    var showDetailDialog by remember { mutableStateOf(false) }

    val displayText = announcement?.text?.ifBlank { null }
        ?: "📢 জরুরি নোটিশ: কমিউনিটি ডিজিটাল প্ল্যাটফর্মে স্বাগতম। সকল আপডেট জানতে নোটিশ বোর্ডে নজর রাখুন।"

    // Subtle pulse animation for loudspeaker icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_notice_bar")
            .clickable { showDetailDialog = true },
        color = Color(0xFFFFF8E1), // Warm amber notice background
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Loudspeaker indicator with badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE65100))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Notice Icon",
                        tint = Color.White,
                        modifier = Modifier
                            .size(16.dp)
                            .scale(scale)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "নোটিশ",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Smooth right-to-left marquee ticker text
            Text(
                text = displayText,
                color = Color(0xFF000000),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        velocity = 45.dp
                    )
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Expand hint icon
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "View Notice Details",
                tint = Color(0xFFEF6C00),
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (showDetailDialog) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateString = announcement?.updatedAt?.let { dateFormat.format(Date(it)) } ?: "সর্বশেষ আপডেট"

        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE082)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "জরুরি ঘোষণা ও নোটিশ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = displayText,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF5F5F5))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "প্রকাশক: ${announcement?.authorName ?: "কার্যনির্বাহী পরিষদ"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF616161)
                            )
                            Text(
                                text = "সময়: $dateString",
                                fontSize = 11.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showDetailDialog = false },
                    modifier = Modifier.testTag("dismiss_notice_dialog")
                ) {
                    Text("ঠিক আছে (OK)", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
