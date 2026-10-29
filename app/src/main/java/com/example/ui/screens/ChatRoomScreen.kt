package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertEmoticon
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Message
import com.example.data.model.User
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.UserProfileDialog
import com.example.ui.viewmodel.CommunityViewModel
import com.example.util.CameraHelper.createCameraPhotoUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    groupType: String,
    viewModel: CommunityViewModel,
    modifier: Modifier = Modifier
) {
    val isImportant = groupType == Message.GROUP_IMPORTANT
    val currentUser by viewModel.currentUser.collectAsState()
    val generalMessages by viewModel.generalMessages.collectAsState()
    val importantMessages by viewModel.importantMessages.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    val messages = if (isImportant) importantMessages else generalMessages
    val listState = rememberLazyListState()

    val context = LocalContext.current
    val isOnline by viewModel.isOnline.collectAsState()
    val connectionType by viewModel.connectionType.collectAsState()

    var messageInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var selectedReactionMessage by remember { mutableStateOf<Message?>(null) }
    var selectedUserProfile by remember { mutableStateOf<User?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            selectedImageUri = tempCameraUri
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val uri = createCameraPhotoUri(context)
            tempCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            showCameraPermissionDialog = true
        }
    }

    val launchCameraCapture: () -> Unit = {
        val hasCamPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasCamPermission) {
            val uri = createCameraPhotoUri(context)
            tempCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val user = currentUser ?: User(
        name = "সদস্য",
        phoneNumber = "",
        district = "",
        thana = "",
        fatherName = "",
        status = User.STATUS_APPROVED
    )

    // Check permission to post
    val canPost = if (isImportant) user.canPostInImportantGroup else true

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showInfoDialog = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isImportant) Color(0xFF5E35B1) else Color(0xFF075E54)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isImportant) Icons.Default.Security else Icons.Default.Groups,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = if (isImportant) "Important Members Group" else "General Group Chat",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isOnline) Color(0xFF00E676) else Color(0xFFFF8A80))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOnline) connectionType else "অফলাইন (মেসেজ লোকালি জমা হচ্ছে)",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCEEAD6)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Group Info",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isImportant) Color(0xFF4A148C) else Color(0xFF008069)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFEFEAE2)) // WhatsApp chat wallpaper shade
                .imePadding()
                .navigationBarsPadding()
        ) {
            // Live Network Offline Warning
            if (!isOnline) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFF3CD),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "Offline",
                            tint = Color(0xFF856404),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ইন্টারনেট সংযোগ বিচ্ছিন্ন। সংযোগ পুনরুদ্ধার হলে বার্তা স্বয়ংক্রিয়ভাবে সার্ভারে সিঙ্ক হবে।",
                            fontSize = 12.sp,
                            color = Color(0xFF856404),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Group Notice banner if Important channel
            if (isImportant) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFEDE7F6)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF5E35B1),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "অফিশিয়াল চ্যানেল: শুধুমাত্র সভাপতি, সাধারণ সম্পাদক ও অ্যাডমিন পোস্ট করতে পারেন।",
                            fontSize = 11.sp,
                            color = Color(0xFF4A148C),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Message Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                // Date separator chip
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFFFFF).copy(alpha = 0.9f),
                            shadowElevation = 1.dp
                        ) {
                            Text(
                                text = "আজ (Today)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF54656F),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    ChatMessageItem(
                        message = msg,
                        isSelf = msg.senderId == user.id,
                        onReactionClick = { selectedReactionMessage = msg },
                        onSenderClick = { senderId ->
                            val member = allUsers.find { it.id == senderId } ?: User(
                                id = senderId,
                                name = msg.senderName,
                                role = msg.senderRole,
                                status = User.STATUS_APPROVED
                            )
                            selectedUserProfile = member
                        }
                    )
                }
            }

            // Attached Image Preview Chip if selected from device
            if (selectedImageUri != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB2DFDB))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Local Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ছবি সংযুক্ত করা হয়েছে (Image Attached)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF000000)
                            )
                            Text(
                                text = "বার্তার সাথে ছবিটি রিয়েল-টাইমে পাঠানো হবে",
                                fontSize = 11.sp,
                                color = Color(0xFF1F2937)
                            )
                        }
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier.testTag("remove_attached_image_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Image",
                                tint = Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }

            // Input Bar or Read-Only Notice
            if (canPost) {
                val canSend = messageInput.isNotBlank() || selectedImageUri != null

                // Active Input Row for allowed members
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF0F2F5),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Attachment Button (Opens options or photo picker)
                        IconButton(
                            onClick = { showAttachmentSheet = true },
                            modifier = Modifier.testTag("attach_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Attach Media",
                                tint = Color(0xFF008069)
                            )
                        }

                        // Direct Live Camera Capture Button
                        IconButton(
                            onClick = { launchCameraCapture() },
                            modifier = Modifier.testTag("camera_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera Photo",
                                tint = Color(0xFF008069)
                            )
                        }

                        // Message Text Field
                        OutlinedTextField(
                            value = messageInput,
                            onValueChange = { messageInput = it },
                            placeholder = {
                                Text(
                                    text = if (isImportant) "অফিশিয়াল নোটিশ লিখুন..." else "একটি বার্তা লিখুন (Type a message)...",
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("message_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF000000),
                                unfocusedTextColor = Color(0xFF000000),
                                focusedPlaceholderColor = Color(0xFF6B7280),
                                unfocusedPlaceholderColor = Color(0xFF6B7280),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFF008069),
                                unfocusedBorderColor = Color(0xFFB0BEC5)
                            ),
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (canSend) {
                                        viewModel.sendMessage(
                                            groupType = groupType,
                                            text = messageInput.trim(),
                                            imageUrl = selectedImageUri?.toString()
                                        )
                                        messageInput = ""
                                        selectedImageUri = null
                                    }
                                }
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Send Button FAB
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (isImportant) Color(0xFF5E35B1) else Color(0xFF008069))
                                .clickable {
                                    if (canSend) {
                                        viewModel.sendMessage(
                                            groupType = groupType,
                                            text = messageInput.trim(),
                                            imageUrl = selectedImageUri?.toString()
                                        )
                                        messageInput = ""
                                        selectedImageUri = null
                                    }
                                }
                                .testTag("send_message_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (canSend) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            } else {
                // Read-Only Restricted Banner for Regular Members in Important Group
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("read_only_banner"),
                    color = Color(0xFFF5F5F5),
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Read Only",
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "শুধুমাত্র অ্যাডমিন এবং নেতৃবৃন্দ এই গ্রুপে বার্তা পাঠাতে পারেন।",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF000000)
                        )
                    }
                }
            }
        }
    }

    // Media & Attachment Bottom Sheet
    if (showAttachmentSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "মিডিয়া বা ছবি সংযুক্ত করুন (Attach Media & Photos)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF000000)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOption(
                        icon = Icons.Default.AddPhotoAlternate,
                        label = "ডিভাইসের ছবি (Gallery)",
                        bgColor = Color(0xFF008069)
                    ) {
                        showAttachmentSheet = false
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }

                    AttachmentOption(
                        icon = Icons.Default.CameraAlt,
                        label = "ক্যামেরা (Camera)",
                        bgColor = Color(0xFFE91E63)
                    ) {
                        showAttachmentSheet = false
                        launchCameraCapture()
                    }

                    AttachmentOption(
                        icon = Icons.Default.VideoFile,
                        label = "ভিডিও (Video)",
                        bgColor = Color(0xFF009688)
                    ) {
                        showAttachmentSheet = false
                        viewModel.sendMessage(
                            groupType = groupType,
                            text = "কমিউনিটি সাধারণ সভার ভিডিও ডকুমেন্টারি"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Reaction Picker Dialog
    if (selectedReactionMessage != null) {
        val msg = selectedReactionMessage!!
        AlertDialog(
            onDismissRequest = { selectedReactionMessage = null },
            title = { Text("প্রতিক্রিয়া যুক্ত করুন (React)") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("👍", "❤️", "👏", "🤲", "🔥", "📌").forEach { emoji ->
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF0F2F5),
                            modifier = Modifier
                                .size(44.dp)
                                .clickable {
                                    viewModel.addReaction(msg, emoji)
                                    selectedReactionMessage = null
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedReactionMessage = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Group Information Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = if (isImportant) "Important Members Group" else "General Group Chat",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isImportant)
                            "এই চ্যানেলে শুধুমাত্র কেন্দ্রীয় সভাপতি, সাধারণ সম্পাদক ও অ্যাডমিন নোটিশ দিতে পারেন। এটি সব সদস্যের জন্য অফিশিয়াল সার্কুলার চ্যানেল।"
                        else
                            "এটি সকল নিবন্ধিত ও অনুমোদিত সদস্যের উন্মুক্ত সাধারণ যোগাযোগ গ্রুপ। এখানে টেক্সট, ফটো ও ভিডিও পাঠানো যাবে।",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "পোস্ট পারমিশন: " + if (canPost) "অনুমোদিত (Allowed)" else "শুধুমাত্র দেখার অনুমতি (View Only)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canPost) Color(0xFF008069) else Color(0xFFE65100)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }

    // Camera Permission Explanation Dialog
    if (showCameraPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showCameraPermissionDialog = false },
            title = {
                Text(
                    text = "ক্যামেরা ব্যবহারের অনুমতি প্রয়োজন",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000000)
                )
            },
            text = {
                Text(
                    text = "গ্রুপে সরাসরি লাইভ ছবি তুলে পাঠাতে ক্যামেরার অনুমতি প্রদান করুন।",
                    fontSize = 14.sp,
                    color = Color(0xFF1F2937)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCameraPermissionDialog = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("অনুমতি দিন (Grant)", fontWeight = FontWeight.Bold, color = Color(0xFF008069))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCameraPermissionDialog = false }) {
                    Text("বাতিল", color = Color(0xFF757575))
                }
            }
        )
    }

    // Detailed User Profile View Dialog on click
    selectedUserProfile?.let { profileUser ->
        UserProfileDialog(
            user = profileUser,
            isAdminViewer = user.role == User.ROLE_ADMIN,
            onDismiss = { selectedUserProfile = null },
            onApproveUser = { uId, role -> viewModel.approveUser(uId, role) },
            onRejectUser = { uId -> viewModel.rejectUser(uId) },
            onChangeRole = { uId, newRole -> viewModel.approveUser(uId, newRole) }
        )
    }
}

@Composable
fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF424242)
        )
    }
}
