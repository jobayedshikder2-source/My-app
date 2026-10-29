package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Message
import com.example.data.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageItem(
    message: Message,
    isSelf: Boolean,
    onReactionClick: (String) -> Unit,
    onSenderClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bubbleBg = if (isSelf) Color(0xFFE7FFDB) else Color.White
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    val senderColor = when (message.senderRole) {
        User.ROLE_ADMIN -> Color(0xFF1565C0)
        User.ROLE_PRESIDENT -> Color(0xFF6A1B9A)
        User.ROLE_LEADER -> Color(0xFF00695C)
        else -> Color(0xFF1B5E20)
    }

    val roleBadgeText = when (message.senderRole) {
        User.ROLE_ADMIN -> "অ্যাডমিন"
        User.ROLE_PRESIDENT -> "সভাপতি"
        User.ROLE_LEADER -> "নেতৃবৃন্দ"
        else -> null
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isSelf) 14.dp else 2.dp,
                bottomEnd = if (isSelf) 2.dp else 14.dp
            ),
            color = bubbleBg,
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(min = 100.dp, max = 290.dp)
                .testTag("chat_bubble_${message.id}")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                // Sender name and role badge if sent by other
                if (!isSelf) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(enabled = onSenderClick != null) {
                                onSenderClick?.invoke(message.senderId)
                            }
                    ) {
                        Text(
                            text = message.senderName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = senderColor
                        )

                        if (roleBadgeText != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(senderColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = roleBadgeText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = senderColor
                                )
                            }
                        }
                    }
                }

                // Image Attachment if present (supports local device Uri or web URL)
                if (!message.imageUrl.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 240.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0E0E0))
                    ) {
                        AsyncImage(
                            model = message.imageUrl,
                            contentDescription = "Message Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Video Attachment if present
                if (!message.videoUrl.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF263238)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = "Video Attachment",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Message Text in solid high-contrast black
                if (message.text.isNotEmpty()) {
                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF000000),
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Time and tick status
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = Color(0xFF667781)
                    )
                    if (isSelf) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = Color(0xFF53BDEB),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Reaction emojis if present
                if (message.reactions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF0F2F5))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .clickable { onReactionClick("👍") }
                    ) {
                        Text(
                            text = message.reactions,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
