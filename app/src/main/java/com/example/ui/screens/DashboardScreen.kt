package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Message
import com.example.data.model.User
import com.example.ui.components.GroupChatCard
import com.example.ui.components.NoticeMarqueeBar
import com.example.ui.components.UserProfileDialog
import com.example.ui.viewmodel.CommunityViewModel
import com.example.ui.viewmodel.ScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: CommunityViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val activeAnnouncement by viewModel.activeAnnouncement.collectAsState()
    val lastGeneralMessage by viewModel.lastGeneralMessage.collectAsState()
    val lastImportantMessage by viewModel.lastImportantMessage.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val pendingUsers by viewModel.pendingUsers.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val connectionType by viewModel.connectionType.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showSwitchUserDialog by remember { mutableStateOf(false) }
    var showDirectoryDialog by remember { mutableStateOf(false) }
    var selectedMemberForProfile by remember { mutableStateOf<User?>(null) }

    val user = currentUser ?: User(
        name = "সদস্য",
        phoneNumber = "01XXXXXXXXX",
        district = "ঢাকা",
        thana = "মিরপুর",
        fatherName = "",
        status = User.STATUS_APPROVED
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF008069),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Community Hub",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                text = "কমিউনিটি যোগাযোগ নেটওয়ার্ক",
                                fontSize = 11.sp,
                                color = Color(0xFFB2DFDB)
                            )
                        }
                    }
                },
                actions = {
                    // Current User role chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF005C4B),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showProfileDialog = true }
                            .padding(end = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = user.name.split(" ").firstOrNull() ?: "সদস্য",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Top Menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("dashboard_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("আমার প্রোফাইল (My Profile)") },
                                leadingIcon = { Icon(Icons.Default.Person, null) },
                                onClick = {
                                    showMenu = false
                                    showProfileDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("সদস্য ডিরেক্টরি (Member Directory)") },
                                leadingIcon = { Icon(Icons.Default.Verified, null) },
                                onClick = {
                                    showMenu = false
                                    showDirectoryDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("ব্যবহারকারী পরিবর্তন (Switch Account)") },
                                leadingIcon = { Icon(Icons.Default.SwitchAccount, null) },
                                onClick = {
                                    showMenu = false
                                    showSwitchUserDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("অ্যাডমিন প্যানেল (Admin Panel)") },
                                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null) },
                                onClick = {
                                    showMenu = false
                                    if (user.role == User.ROLE_ADMIN) {
                                        viewModel.navigateTo(ScreenState.AdminPanel)
                                    } else {
                                        viewModel.navigateTo(ScreenState.AdminLogin)
                                    }
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("ওয়েব কোড এক্সপ্লোরার (Web Code)") },
                                leadingIcon = { Icon(Icons.Default.Code, null) },
                                onClick = {
                                    showMenu = false
                                    viewModel.navigateTo(ScreenState.WebCodeViewer)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("লগআউট (Logout)", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    viewModel.logout()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF008069)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F2F5))
                .verticalScroll(rememberScrollState())
        ) {
            // Requirement 3: Top Notice Bar (Scrolling Marquee Bar)
            NoticeMarqueeBar(
                announcement = activeAnnouncement,
                modifier = Modifier.fillMaxWidth()
            )

            // Real-Time Network Connectivity Indicator Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (isOnline) Color(0xFFE8F5E9) else Color(0xFFFFF3CD),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color(0xFF2E7D32) else Color(0xFFD97706))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOnline) "নেটওয়ার্ক: $connectionType" else "অফলাইন - ইন্টারনেট সংযোগ বিচ্ছিন্ন",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOnline) Color(0xFF1B5E20) else Color(0xFF856404)
                        )
                    }

                    Text(
                        text = if (isOnline) "সক্রিয়" else "লোকাল মোড",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnline) Color(0xFF2E7D32) else Color(0xFFD97706)
                    )
                }
            }

            // Hero Community Banner Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF004D40))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.community_hero_banner_1791464325467),
                    contentDescription = "Community Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Group List Section
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "কমিউনিটি গ্রুপসমূহ (Community Channels)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000000),
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Requirement 3 (a): General Group Chat - Placed below marquee
                GroupChatCard(
                    groupType = Message.GROUP_GENERAL,
                    title = "General Community Chat",
                    banglaTitle = "সাধারণ উন্মুক্ত চ্যাট রুম",
                    description = "সকল অনুমোদিত সদস্য বার্তা, ছবি ও ভিডিও আদান-প্রদান করতে পারেন।",
                    lastMessage = lastGeneralMessage,
                    isRestricted = false,
                    onClick = {
                        viewModel.navigateTo(ScreenState.ChatRoom(Message.GROUP_GENERAL))
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Requirement 3 (b): Important Members Group - Placed below general group
                GroupChatCard(
                    groupType = Message.GROUP_IMPORTANT,
                    title = "Important Members & Leaders",
                    banglaTitle = "নেতৃবৃন্দ ও কার্যনির্বাহী পরিষদ চ্যানেল",
                    description = "শুধুমাত্র অ্যাডমিন ও দায়িত্বশীল নেতৃবৃন্দ পোস্ট করতে পারবেন। সদস্যরা দেখতে পারবেন।",
                    lastMessage = lastImportantMessage,
                    isRestricted = true,
                    onClick = {
                        viewModel.navigateTo(ScreenState.ChatRoom(Message.GROUP_IMPORTANT))
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Stats & Community Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "কমিউনিটি স্ট্যাটাস (Community Stats)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "লাইভ ডেটাবেস",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            count = "${allUsers.count { it.isApproved }}",
                            label = "অনুমোদিত সদস্য",
                            color = Color(0xFF008069)
                        )
                        StatItem(
                            count = "${pendingUsers.size}",
                            label = "পেন্ডিং আবেদন",
                            color = Color(0xFFE65100)
                        )
                        StatItem(
                            count = "${allUsers.map { it.district }.distinct().size}",
                            label = "জেলা অন্তর্ভুক্ত",
                            color = Color(0xFF1565C0)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showDirectoryDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("open_directory_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE8F5E9),
                            contentColor = Color(0xFF008069)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "সদস্য তালিকা ও প্রোফাইল দেখুন (Member Directory)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Admin Shortcut Banner (if current user is admin, direct access)
            if (user.role == User.ROLE_ADMIN) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clickable { viewModel.navigateTo(ScreenState.AdminPanel) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "অ্যাডমিন ড্যাশবোর্ড সচল রয়েছে",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0D47A1)
                            )
                            Text(
                                text = "${pendingUsers.size} টি অপেক্ষমাণ আবেদন ও নোটিশ নিয়ন্ত্রণ করুন",
                                fontSize = 11.sp,
                                color = Color(0xFF1976D2)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 1. Current User Profile Dialog
    if (showProfileDialog) {
        UserProfileDialog(
            user = user,
            isAdminViewer = user.role == User.ROLE_ADMIN,
            onDismiss = { showProfileDialog = false }
        )
    }

    // 2. Selected Member Profile Dialog from Member List / Directory
    selectedMemberForProfile?.let { member ->
        UserProfileDialog(
            user = member,
            isAdminViewer = user.role == User.ROLE_ADMIN,
            onDismiss = { selectedMemberForProfile = null },
            onChangeRole = { uId, newRole ->
                viewModel.approveUser(uId, newRole)
                selectedMemberForProfile = null
            },
            onApproveUser = { uId, role ->
                viewModel.approveUser(uId, role)
                selectedMemberForProfile = null
            },
            onRejectUser = { uId ->
                viewModel.rejectUser(uId)
                selectedMemberForProfile = null
            }
        )
    }

    // 3. Member Directory Dialog (Displays detailed member information when name is clicked)
    if (showDirectoryDialog) {
        AlertDialog(
            onDismissRequest = { showDirectoryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF008069),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "কমিউনিটি সদস্য ডিরেক্টরি",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "যেকোনো সদস্যের নামের ওপর ট্যাপ করে তার পূর্ণ প্রোফাইল ও বিস্তারিত তথ্য দেখুন:",
                        fontSize = 12.sp,
                        color = Color(0xFF616161)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val approvedMembers = allUsers.filter { it.isApproved }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        approvedMembers.forEach { member ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF9F9F9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedMemberForProfile = member
                                        showDirectoryDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (member.role) {
                                                    User.ROLE_ADMIN -> Color(0xFF1565C0)
                                                    User.ROLE_PRESIDENT -> Color(0xFF6A1B9A)
                                                    User.ROLE_LEADER -> Color(0xFF00897B)
                                                    else -> Color(0xFF008069)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = member.name.take(1),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = member.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF111B21)
                                        )
                                        Text(
                                            text = "${member.district} • ${member.thana}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF757575)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFE8F5E9)
                                    ) {
                                        Text(
                                            text = "প্রোফাইল >",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF008069),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDirectoryDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    // 4. Switch Account Dialog
    if (showSwitchUserDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchUserDialog = false },
            title = {
                Text(
                    text = "টেস্ট অ্যাকাউন্ট পরিবর্তন করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "যেকোনো ভূমিকায় সুইচ করে অ্যাপ্লিকেশনের পারমিশন পরীক্ষা করুন:",
                        fontSize = 12.sp,
                        color = Color(0xFF616161)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    allUsers.forEach { member ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.switchUserQuick(member.id)
                                    showSwitchUserDialog = false
                                },
                            color = if (member.id == user.id) Color(0xFFE0F2F1) else Color(0xFFF5F5F5)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (member.role) {
                                                User.ROLE_ADMIN -> Color(0xFF1565C0)
                                                User.ROLE_PRESIDENT -> Color(0xFF6A1B9A)
                                                User.ROLE_LEADER -> Color(0xFF00897B)
                                                else -> Color(0xFF43A047)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = member.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${member.roleBadge} | ${member.district}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF757575)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSwitchUserDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF546E7A),
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF263238)
        )
    }
}

@Composable
fun StatItem(count: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1F2937)
        )
    }
}
