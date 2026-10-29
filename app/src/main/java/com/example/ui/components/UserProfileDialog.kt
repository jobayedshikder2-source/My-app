package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserProfileDialog(
    user: User,
    isAdminViewer: Boolean = false,
    onDismiss: () -> Unit,
    onApproveUser: ((String, String) -> Unit)? = null,
    onRejectUser: ((String) -> Unit)? = null,
    onChangeRole: ((String, String) -> Unit)? = null,
    onDeleteUser: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var showRoleMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
    val regDateString = dateFormat.format(Date(user.createdAt))

    val roleColor = when (user.role) {
        User.ROLE_ADMIN -> Color(0xFF1565C0)
        User.ROLE_PRESIDENT -> Color(0xFF6A1B9A)
        User.ROLE_LEADER -> Color(0xFF00897B)
        else -> Color(0xFF008069)
    }

    val statusColor = when (user.status) {
        User.STATUS_APPROVED -> Color(0xFF2E7D32)
        User.STATUS_REJECTED -> Color(0xFFC62828)
        else -> Color(0xFFE65100)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("user_profile_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(roleColor)
                        .padding(top = 18.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .testTag("close_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.take(2).uppercase(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = roleColor
                            )

                            if (user.isApproved) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2E7D32)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Member",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Full Name
                        Text(
                            text = user.name,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Role and Status Badges
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.22f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = user.roleBadge,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when (user.status) {
                                    User.STATUS_APPROVED -> Color(0xFFC8E6C9)
                                    User.STATUS_REJECTED -> Color(0xFFFFCDD2)
                                    else -> Color(0xFFFFE0B2)
                                }
                            ) {
                                Text(
                                    text = when (user.status) {
                                        User.STATUS_APPROVED -> "অনুমোদিত"
                                        User.STATUS_REJECTED -> "বাতিল"
                                        else -> "পেন্ডিং আবেদন"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Member Details Body
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "সদস্য পূর্ণ তথ্য (Member Information)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Phone Number with Copy & Dial Quick Actions
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0F4F8),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color(0xFF008069),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ফোন নম্বর", fontSize = 11.sp, color = Color(0xFF78909C))
                                Text(
                                    text = user.phoneNumber,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF263238)
                                )
                            }

                            // Copy Phone Button
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Phone Number", user.phoneNumber))
                                    Toast.makeText(context, "ফোন নম্বর কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Phone",
                                    tint = Color(0xFF546E7A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Dial Button
                            IconButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${user.phoneNumber}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, user.phoneNumber, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call",
                                    tint = Color(0xFF008069),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Demographics list
                    DetailInfoRow(
                        icon = Icons.Default.PersonOutline,
                        label = "বাপের নাম (Father's Name)",
                        value = user.fatherName.ifBlank { "—" }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))

                    DetailInfoRow(
                        icon = Icons.Default.LocationCity,
                        label = "জেলা (District)",
                        value = user.district
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))

                    DetailInfoRow(
                        icon = Icons.Default.Map,
                        label = "থানা / উপজেলা (Thana)",
                        value = user.thana
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))

                    DetailInfoRow(
                        icon = Icons.Default.DateRange,
                        label = "নিবন্ধন তারিখ (Registration Date)",
                        value = regDateString
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))

                    DetailInfoRow(
                        icon = Icons.Default.Security,
                        label = "চ্যানেল এক্সেস পারমিশন",
                        value = if (user.canPostInImportantGroup)
                            "উন্মুক্ত ও গুরুত্বপূর্ণ চ্যানেল উভয়টিতে পোস্টের অনুমতি"
                        else
                            "শুধুমাত্র সাধারণ চ্যাট রুমে বার্তা পোস্টের অনুমতি"
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))

                    DetailInfoRow(
                        icon = Icons.Default.Badge,
                        label = "সদস্য আইডি (Member ID)",
                        value = user.id
                    )

                    // Admin Actions Section if viewer is Admin
                    if (isAdminViewer) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "অ্যাডমিন অ্যাকশন (Admin Actions)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (user.isPending) {
                            // Quick Approve / Reject
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        onRejectUser?.invoke(user.id)
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
                                ) {
                                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("আবেদন বাতিল")
                                }

                                Button(
                                    onClick = {
                                        onApproveUser?.invoke(user.id, User.ROLE_MEMBER)
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                ) {
                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("অনুমোদন দিন")
                                }
                            }
                        } else {
                            // Role Modifier Button
                            Box(modifier = Modifier.fillMaxWidth()) {
                                FilledTonalButton(
                                    onClick = { showRoleMenu = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.WorkspacePremium, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("পদবী পরিবর্তন করুন (Change Role)")
                                }

                                DropdownMenu(
                                    expanded = showRoleMenu,
                                    onDismissRequest = { showRoleMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("সাধারণ সদস্য (Member)") },
                                        onClick = {
                                            onChangeRole?.invoke(user.id, User.ROLE_MEMBER)
                                            showRoleMenu = false
                                            onDismiss()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("কার্যনির্বাহী নেতৃবৃন্দ (Leader)") },
                                        onClick = {
                                            onChangeRole?.invoke(user.id, User.ROLE_LEADER)
                                            showRoleMenu = false
                                            onDismiss()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("সভাপতি / সহ-সভাপতি (President)") },
                                        onClick = {
                                            onChangeRole?.invoke(user.id, User.ROLE_PRESIDENT)
                                            showRoleMenu = false
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                        }

                        if (onDeleteUser != null && user.id != "admin_1") {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { showDeleteConfirmDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
                            ) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("এই অ্যাকাউন্ট মুছে ফেলুন (Delete Member)", fontSize = 12.sp)
                            }
                        }
                    }

                    if (showDeleteConfirmDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirmDialog = false },
                            title = { Text("নিশ্চিতকরণ") },
                            text = { Text("${user.name}-কে কি ডাটাবেজ থেকে স্থায়ীভাবে মুছে ফেলতে চান?") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showDeleteConfirmDialog = false
                                        onDeleteUser?.invoke(user.id)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                                ) {
                                    Text("হ্যাঁ, মুছুন")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                                    Text("বাতিল")
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64))
                    ) {
                        Text("বন্ধ করুন (Close)")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF546E7A),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF78909C),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121),
                lineHeight = 18.sp
            )
        }
    }
}
