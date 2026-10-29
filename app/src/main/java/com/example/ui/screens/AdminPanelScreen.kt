package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.components.UserProfileDialog
import com.example.ui.viewmodel.CommunityViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: CommunityViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val pendingUsers by viewModel.pendingUsers.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val activeAnnouncement by viewModel.activeAnnouncement.collectAsState()
    val allAnnouncements by viewModel.allAnnouncements.collectAsState()

    var newNoticeText by remember { mutableStateOf(activeAnnouncement?.text ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "অ্যাডমিন কন্ট্রোল সেন্টার",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Admin Management Dashboard",
                            fontSize = 11.sp,
                            color = Color(0xFFBBDEFB)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("admin_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F2F5))
        ) {
            // Tabs: User Approvals, Announcement Control, Group Management
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1E88E5),
                contentColor = Color.White,
                edgePadding = 8.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("সদস্য অনুমোদন")
                            if (pendingUsers.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFF5722)
                                ) {
                                    Text(
                                        text = "${pendingUsers.size}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        newNoticeText = activeAnnouncement?.text ?: ""
                    },
                    text = { Text("জরুরি নোটিশ নিয়ন্ত্রণ") }
                )

                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("গ্রুপ ব্যবস্থাপনা") }
                )
            }

            when (selectedTab) {
                0 -> UserApprovalTab(viewModel, pendingUsers, allUsers)
                1 -> AnnouncementControlTab(viewModel, activeAnnouncement?.text ?: "", allAnnouncements)
                2 -> GroupManagementTab(viewModel, allUsers)
            }
        }
    }
}

@Composable
fun UserApprovalTab(
    viewModel: CommunityViewModel,
    pendingUsers: List<User>,
    allUsers: List<User>
) {
    var showFilter by remember { mutableStateOf("pending") } // "pending", "all", "approved"
    var viewingUser by remember { mutableStateOf<User?>(null) }
    var showDeleteExamplesDialog by remember { mutableStateOf(false) }

    val displayUsers = when (showFilter) {
        "pending" -> pendingUsers
        "approved" -> allUsers.filter { it.isApproved }
        else -> allUsers
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Quick Action: Delete All Examples banner
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFFEBEE),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "এক্সাম্পল ও ডেমো ডেটা ব্যবস্থাপনা",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB71C1C)
                        )
                        Text(
                            text = "সকল পরীক্ষামূলক বার্তা ও সদস্য মুছে ফেলুন",
                            fontSize = 10.sp,
                            color = Color(0xFFC62828)
                        )
                    }
                }

                Button(
                    onClick = { showDeleteExamplesDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("সব মুছুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status filter pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                label = "পেন্ডিং (${pendingUsers.size})",
                selected = showFilter == "pending",
                onClick = { showFilter = "pending" }
            )
            FilterChip(
                label = "অনুমোদিত (${allUsers.count { it.isApproved }})",
                selected = showFilter == "approved",
                onClick = { showFilter = "approved" }
            )
            FilterChip(
                label = "সকল সদস্য (${allUsers.size})",
                selected = showFilter == "all",
                onClick = { showFilter = "all" }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (displayUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.HowToReg,
                        contentDescription = null,
                        tint = Color(0xFFB0BEC5),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "কোনো সদস্য নেই",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78909C)
                    )
                    Text(
                        text = "নতুন নিবন্ধন আসলে এখানে প্রদর্শিত হবে।",
                        fontSize = 12.sp,
                        color = Color(0xFF90A4AE)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayUsers, key = { it.id }) { user ->
                    PendingUserCard(
                        user = user,
                        viewModel = viewModel,
                        onViewProfile = { viewingUser = user }
                    )
                }
            }
        }
    }

    if (showDeleteExamplesDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteExamplesDialog = false },
            title = { Text("সকল এক্সাম্পল ডেটা মুছবেন?") },
            text = {
                Text("ডাটাবেজ থেকে সকল এক্সাম্পল ব্যবহারকারী, পরীক্ষামূলক বার্তা এবং প্রাক-লোডকৃত নোটিশ অবিলম্বে মুছে ফেলা হবে। শুধুমাত্র আসল অ্যাডমিন সংরক্ষিত থাকবে।")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteExamplesDialog = false
                        viewModel.deleteAllExamples()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("হ্যাঁ, সব এক্সাম্পল মুছুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteExamplesDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    viewingUser?.let { selectedMember ->
        UserProfileDialog(
            user = selectedMember,
            isAdminViewer = true,
            onDismiss = { viewingUser = null },
            onApproveUser = { uId, role ->
                viewModel.approveUser(uId, role)
                viewingUser = null
            },
            onRejectUser = { uId ->
                viewModel.rejectUser(uId)
                viewingUser = null
            },
            onChangeRole = { uId, newRole ->
                viewModel.approveUser(uId, newRole)
                viewingUser = null
            },
            onDeleteUser = { uId ->
                viewModel.deleteUser(uId)
                viewingUser = null
            }
        )
    }
}

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color(0xFF1565C0) else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) Color(0xFF1565C0) else Color(0xFFCFD8DC)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else Color(0xFF455A64),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun PendingUserCard(
    user: User,
    viewModel: CommunityViewModel,
    onViewProfile: (() -> Unit)? = null
) {
    var showRoleMenu by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val regDate = dateFormat.format(Date(user.createdAt))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_card_${user.id}")
            .clickable { onViewProfile?.invoke() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewProfile?.invoke() }
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (user.isApproved) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (user.isApproved) Color(0xFF2E7D32) else Color(0xFFE65100),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111B21)
                    )
                    Text(
                        text = "ফোন: ${user.phoneNumber}",
                        fontSize = 12.sp,
                        color = Color(0xFF546E7A)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (user.status) {
                        User.STATUS_APPROVED -> Color(0xFFE8F5E9)
                        User.STATUS_REJECTED -> Color(0xFFFFEBEE)
                        else -> Color(0xFFFFF3E0)
                    }
                ) {
                    Text(
                        text = when (user.status) {
                            User.STATUS_APPROVED -> "অনুমোদিত"
                            User.STATUS_REJECTED -> "বাতিল"
                            else -> "পেন্ডিং"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (user.status) {
                            User.STATUS_APPROVED -> Color(0xFF2E7D32)
                            User.STATUS_REJECTED -> Color(0xFFC62828)
                            else -> Color(0xFFE65100)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // User detailed information as required: Father's Name, District, Thana
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF8F9FA),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("বাপের নাম: ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161))
                        Text(user.fatherName.ifBlank { "—" }, fontSize = 12.sp, color = Color(0xFF212121))
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("জেলা: ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161))
                        Text(user.district, fontSize = 12.sp, color = Color(0xFF212121))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("থানা: ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161))
                        Text(user.thana, fontSize = 12.sp, color = Color(0xFF212121))
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "আবেদনের সময়: $regDate",
                        fontSize = 11.sp,
                        color = Color(0xFF757575)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Approve (with role picker) & Reject
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (user.isPending) {
                    OutlinedButton(
                        onClick = { viewModel.rejectUser(user.id) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                        modifier = Modifier.testTag("reject_button_${user.id}")
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("বাতিল (Reject)", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        Button(
                            onClick = { showRoleMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.testTag("approve_button_${user.id}")
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("অনুমোদন (Approve)", fontSize = 12.sp)
                        }

                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("সাধারণ সদস্য (General Member)") },
                                onClick = {
                                    viewModel.approveUser(user.id, User.ROLE_MEMBER)
                                    showRoleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("দায়িত্বশীল নেতৃবৃন্দ (Leader)") },
                                onClick = {
                                    viewModel.approveUser(user.id, User.ROLE_LEADER)
                                    showRoleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("সভাপতি / সহ-সভাপতি (President)") },
                                onClick = {
                                    viewModel.approveUser(user.id, User.ROLE_PRESIDENT)
                                    showRoleMenu = false
                                }
                            )
                        }
                    }
                } else if (user.isApproved) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ভূমিকা: ${user.roleBadge}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF008069)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (user.id != "admin_1") {
                                IconButton(
                                    onClick = { viewModel.deleteUser(user.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Member",
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            TextButton(
                                onClick = { onViewProfile?.invoke() },
                                modifier = Modifier.testTag("view_profile_btn_${user.id}")
                            ) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("প্রোফাইল দেখুন", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementControlTab(
    viewModel: CommunityViewModel,
    currentNotice: String,
    historyList: List<com.example.data.model.Announcement>
) {
    var noticeInput by remember { mutableStateOf(currentNotice) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "সর্বজনীন জরুরি নোটিশ নিয়ন্ত্রণ (Notice Control)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111B21)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "এখানে নোটিশ পরিবর্তন করলে তা সরাসরি সকল সদস্যের শীর্ষ স্ক্রোলিং মারকুই বারে লাইভ প্রদর্শিত হবে।",
                    fontSize = 12.sp,
                    color = Color(0xFF616161)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = noticeInput,
                    onValueChange = { noticeInput = it },
                    label = { Text("নোটিশের টেক্সট (Announcement Text)", color = Color(0xFF000000), fontWeight = FontWeight.Medium) },
                    placeholder = { Text("📢 জরুরি নোটিশ লিখুন...", color = Color(0xFF6B7280)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("announcement_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF000000),
                        unfocusedTextColor = Color(0xFF000000),
                        focusedPlaceholderColor = Color(0xFF6B7280),
                        unfocusedPlaceholderColor = Color(0xFF6B7280),
                        focusedBorderColor = Color(0xFF1565C0),
                        unfocusedBorderColor = Color(0xFF90A4AE),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Notice Templates
                Text(
                    text = "দ্রুত টেমপ্লেট নির্বাচন করুন:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TemplateChip("সাধারণ সভা") {
                        noticeInput = "📢 জরুরি নোটিশ: আগামী শুক্রবার বিকেল ৪টায় কমিউনিটি মিলনায়তনে সাধারণ সভা অনুষ্ঠিত হবে।"
                    }
                    TemplateChip("চাঁদা আদায়") {
                        noticeInput = "📢 বিজ্ঞপ্তি: চলতি মাসের মাসিক চাঁদা আগামী ১০ তারিখের মধ্যে পরিশোধ করার অনুরোধ করা যাচ্ছে।"
                    }
                    TemplateChip("ঈদ শুভেচ্ছা") {
                        noticeInput = "🌙 ঈদ মোবারক! কমিউনিটির সকল সদস্য ও শুভানুধ্যায়ীদের আন্তরিক শুভেচ্ছা ও অভিনন্দন।"
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.updateAnnouncement(noticeInput) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("update_announcement_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "নোটিশ হালনাগাদ করুন (Update Notice)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TemplateChip(title: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFE3F2FD),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = Color(0xFF1565C0),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun GroupManagementTab(viewModel: CommunityViewModel, allUsers: List<User>) {
    var viewingLeader by remember { mutableStateOf<User?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "গ্রুপ পারমিশন ও চ্যানেল ব্যবস্থাপনা",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111B21)
                )
                Spacer(modifier = Modifier.height(12.dp))

                GroupSettingItem(
                    name = "General Community Chat",
                    bangla = "সাধারণ উন্মুক্ত গ্রুপ",
                    policy = "সকল অনুমোদিত সদস্য পোস্ট করতে পারেন",
                    color = Color(0xFF008069)
                )

                Spacer(modifier = Modifier.height(10.dp))

                GroupSettingItem(
                    name = "Important Members & Leaders",
                    bangla = "নেতৃবৃন্দ ও কার্যনির্বাহী পরিষদ চ্যানেল",
                    policy = "শুধুমাত্র অ্যাডমিন, সভাপতি ও নেতৃবৃন্দ পোস্ট করতে পারেন (সদস্যরা ভিউ-অনলি)",
                    color = Color(0xFF5E35B1)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "নির্ধারিত নেতৃবৃন্দের তালিকা (Designated Leaders)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "যেকোনো নেতার নামের ওপর ট্যাপ করে বিস্তারিত প্রোফাইল দেখুন",
                    fontSize = 11.sp,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(10.dp))

                val leaders = allUsers.filter { it.canPostInImportantGroup }
                leaders.forEach { leader ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewingLeader = leader },
                        color = Color(0xFFF9F9F9)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF5E35B1),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(leader.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("${leader.roleBadge} | ${leader.district}", fontSize = 11.sp, color = Color(0xFF757575))
                            }
                            Text(
                                text = "প্রোফাইল >",
                                fontSize = 11.sp,
                                color = Color(0xFF5E35B1),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    viewingLeader?.let { leader ->
        UserProfileDialog(
            user = leader,
            isAdminViewer = true,
            onDismiss = { viewingLeader = null },
            onChangeRole = { uId, newRole ->
                viewModel.approveUser(uId, newRole)
                viewingLeader = null
            }
        )
    }
}

@Composable
fun GroupSettingItem(name: String, bangla: String, policy: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
            Text(bangla, fontSize = 11.sp, color = Color(0xFF424242))
            Spacer(modifier = Modifier.height(4.dp))
            Text("নীতিমালা: $policy", fontSize = 11.sp, color = Color(0xFF616161))
        }
    }
}
