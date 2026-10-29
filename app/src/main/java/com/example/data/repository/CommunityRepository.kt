package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.CommunityDatabase
import com.example.data.model.Announcement
import com.example.data.model.Message
import com.example.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.example.util.NotificationHelper
import com.example.util.NetworkMonitor

sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    object PendingApproval : LoginResult()
    object Rejected : LoginResult()
    object NotFound : LoginResult()
    object WrongPassword : LoginResult()
    data class NeedsRegistration(val googleUid: String, val name: String, val email: String) : LoginResult()
}

class CommunityRepository(private val context: Context) {
    private val localDb = CommunityDatabase.getDatabase(context)
    private val userDao = localDb.userDao()
    private val announcementDao = localDb.announcementDao()
    private val messageDao = localDb.messageDao()

    private val firestore: FirebaseFirestore = try {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    } catch (e: Exception) {
        Log.w("CommunityRepository", "Custom DB ID lookup failed, using default: ${e.message}")
        FirebaseFirestore.getInstance()
    }

    private val auth = Firebase.auth
    val networkMonitor = NetworkMonitor(context)
    private val notifiedMessageIds = java.util.Collections.synchronizedSet(mutableSetOf<String>())
    @Volatile
    private var isFirstGeneralLoad = true
    @Volatile
    private var isFirstImportantLoad = true

    init {
        try {
            val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(com.google.firebase.firestore.FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            firestore.firestoreSettings = settings
        } catch (e: Exception) {
            Log.w("CommunityRepository", "Could not configure firestoreSettings: ${e.message}")
        }

        CoroutineScope(Dispatchers.IO).launch {
            // Delete all examples on startup to ensure a clean state
            cleanupAllExampleData()
            seedInitialAdminOnlyIfEmpty()
        }
    }

    // Real-time Firestore Users observation with Room local fallback
    val allUsers: Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CommunityRepository", "Error observing users from Firestore", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
                        .sortedByDescending { it.createdAt }
                    trySend(users)

                    CoroutineScope(Dispatchers.IO).launch {
                        userDao.insertUsers(users)
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    val pendingUsers: Flow<List<User>> = allUsers.map { list ->
        list.filter { it.status == User.STATUS_PENDING }
    }

    val activeAnnouncement: Flow<Announcement?> = callbackFlow {
        val listener = firestore.collection("announcements")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CommunityRepository", "Error observing active announcement", error)
                    return@addSnapshotListener
                }
                val active = snapshot?.documents?.mapNotNull { it.toObject(Announcement::class.java) }
                    ?.maxByOrNull { it.updatedAt }
                trySend(active)
            }
        awaitClose { listener.remove() }
    }

    val allAnnouncements: Flow<List<Announcement>> = callbackFlow {
        val listener = firestore.collection("announcements")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val list = snapshot?.documents?.mapNotNull { it.toObject(Announcement::class.java) }
                    ?.sortedByDescending { it.updatedAt } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // Real-time Firestore Messages stream
    fun getMessages(groupType: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection("messages")
            .whereEqualTo("groupType", groupType)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CommunityRepository", "Error observing messages for $groupType", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                        .sortedBy { it.timestamp }
                    trySend(list)

                    val isFirstLoad = if (groupType == Message.GROUP_IMPORTANT) isFirstImportantLoad else isFirstGeneralLoad

                    if (!isFirstLoad) {
                        for (msg in list) {
                            if (!notifiedMessageIds.contains(msg.id)) {
                                notifiedMessageIds.add(msg.id)
                                val groupTitle = if (msg.groupType == Message.GROUP_IMPORTANT) "Important Members Group" else "General Group Chat"
                                NotificationHelper.showMessageNotification(
                                    context = context,
                                    senderName = "${msg.senderName} (${msg.roleBadge})",
                                    messageText = if (msg.text.isNotBlank()) msg.text else "📷 ছবি পাঠিয়েছেন",
                                    groupTitle = groupTitle
                                )
                            }
                        }
                    } else {
                        for (msg in list) {
                            notifiedMessageIds.add(msg.id)
                        }
                        if (groupType == Message.GROUP_IMPORTANT) {
                            isFirstImportantLoad = false
                        } else {
                            isFirstGeneralLoad = false
                        }
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        messageDao.insertMessages(list)
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    fun getLastMessage(groupType: String): Flow<Message?> =
        getMessages(groupType).map { it.lastOrNull() }

    fun getMessageCount(groupType: String): Flow<Int> =
        getMessages(groupType).map { it.size }

    // Google Sign-In user verification against Firestore
    suspend fun checkGoogleUser(firebaseUser: com.google.firebase.auth.FirebaseUser): LoginResult {
        val uid = firebaseUser.uid
        val email = firebaseUser.email ?: ""
        val name = firebaseUser.displayName ?: "সদস্য"

        try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val user = doc.toObject(User::class.java) ?: User(id = uid, name = name, email = email)
                // If developer or designated admin email, ensure immediate admin status
                if (email.equals("jobayedshikder2@gmail.com", ignoreCase = true)) {
                    val adminUser = user.copy(status = User.STATUS_APPROVED, role = User.ROLE_ADMIN)
                    firestore.collection("users").document(uid).set(adminUser).await()
                    userDao.insertUser(adminUser)
                    return LoginResult.Success(adminUser)
                }

                return when (user.status) {
                    User.STATUS_APPROVED -> LoginResult.Success(user)
                    User.STATUS_PENDING -> LoginResult.PendingApproval
                    User.STATUS_REJECTED -> LoginResult.Rejected
                    else -> LoginResult.PendingApproval
                }
            } else {
                // If developer or designated admin email, auto-register as approved Admin
                if (email.equals("jobayedshikder2@gmail.com", ignoreCase = true)) {
                    val adminUser = User(
                        id = uid,
                        name = name.ifBlank { "প্রধান অ্যাডমিন" },
                        phoneNumber = "01700000001",
                        district = "ঢাকা (Dhaka)",
                        thana = "রমনা (Ramna)",
                        fatherName = "মৌলভী আব্দুর রহমান",
                        email = email,
                        status = User.STATUS_APPROVED,
                        role = User.ROLE_ADMIN,
                        createdAt = System.currentTimeMillis()
                    )
                    firestore.collection("users").document(uid).set(adminUser).await()
                    userDao.insertUser(adminUser)
                    return LoginResult.Success(adminUser)
                }

                return LoginResult.NeedsRegistration(uid, name, email)
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error checking Google user in Firestore", e)
            return LoginResult.NeedsRegistration(uid, name, email)
        }
    }

    suspend fun registerGoogleUser(
        uid: String,
        name: String,
        phone: String,
        district: String,
        thana: String,
        fatherName: String,
        email: String
    ): Result<User> {
        return try {
            val cleanPhone = phone.trim()
            val newUser = User(
                id = uid,
                name = name.trim(),
                phoneNumber = cleanPhone,
                district = district.trim(),
                thana = thana.trim(),
                fatherName = fatherName.trim(),
                email = email.trim(),
                status = User.STATUS_PENDING,
                role = User.ROLE_MEMBER,
                createdAt = System.currentTimeMillis()
            )
            firestore.collection("users").document(uid).set(newUser).await()
            userDao.insertUser(newUser)
            Result.success(newUser)
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error registering Google user in Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun registerUser(
        name: String,
        phone: String,
        email: String = "",
        password: String = "",
        district: String,
        thana: String,
        fatherName: String
    ): Result<User> {
        val cleanPhone = phone.trim()
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        var uid = auth.currentUser?.uid
        if (uid == null && cleanEmail.contains("@") && cleanPassword.length >= 6) {
            try {
                val authResult = auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword).await()
                uid = authResult.user?.uid
            } catch (e: Exception) {
                Log.w("CommunityRepository", "Firebase email registration note: ${e.message}")
            }
        }

        val finalUid = uid ?: ("user_" + UUID.randomUUID().toString().take(8))
        val newUser = User(
            id = finalUid,
            name = name.trim(),
            phoneNumber = cleanPhone,
            email = if (cleanEmail.isNotBlank()) cleanEmail else (auth.currentUser?.email ?: ""),
            password = cleanPassword,
            district = district.trim(),
            thana = thana.trim(),
            fatherName = fatherName.trim(),
            status = User.STATUS_PENDING,
            role = User.ROLE_MEMBER,
            createdAt = System.currentTimeMillis()
        )
        try {
            firestore.collection("users").document(finalUid).set(newUser).await()
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error saving user to Firestore", e)
        }
        userDao.insertUser(newUser)
        return Result.success(newUser)
    }

    suspend fun loginWithEmailOrPhone(identifier: String, password: String): LoginResult {
        val cleanId = identifier.trim()
        val cleanPass = password.trim()

        // 1. Direct Admin Access
        if ((cleanId.equals("admin", ignoreCase = true) || cleanId.equals("admin@community.com", ignoreCase = true)) &&
            cleanPass == "admin123"
        ) {
            val adminUser = getUserById("admin_1") ?: User(
                id = "admin_1",
                name = "অ্যাডমিন (Admin Controller)",
                phoneNumber = "01700000000",
                email = "admin@community.com",
                district = "ঢাকা (Dhaka)",
                thana = "রমনা (Ramna)",
                fatherName = "মৌলভী আব্দুর রহমান",
                status = User.STATUS_APPROVED,
                role = User.ROLE_ADMIN
            )
            return LoginResult.Success(adminUser)
        }

        // 2. Firebase Auth sign-in if email provided
        if (cleanId.contains("@") && cleanPass.isNotEmpty()) {
            try {
                auth.signInWithEmailAndPassword(cleanId, cleanPass).await()
            } catch (e: Exception) {
                Log.w("CommunityRepository", "Firebase auth sign in note: ${e.message}")
            }
        }

        // 3. Query Firestore
        try {
            var user: User? = null
            if (cleanId.contains("@")) {
                val emailQuery = firestore.collection("users")
                    .whereEqualTo("email", cleanId)
                    .limit(1)
                    .get()
                    .await()
                user = emailQuery.documents.firstOrNull()?.toObject(User::class.java)
            } else {
                val phoneQuery = firestore.collection("users")
                    .whereEqualTo("phoneNumber", cleanId)
                    .limit(1)
                    .get()
                    .await()
                user = phoneQuery.documents.firstOrNull()?.toObject(User::class.java)
            }

            // Fallback to local DB
            if (user == null) {
                user = userDao.getUserByIdentifier(cleanId)
                    ?: userDao.getUserByPhone(cleanId)
                    ?: userDao.getUserByEmail(cleanId)
            }

            if (user == null) {
                return LoginResult.NotFound
            }

            // Password check if password is saved for this user
            if (user.password.isNotBlank() && cleanPass.isNotEmpty() && user.password != cleanPass) {
                return LoginResult.WrongPassword
            }

            return when (user.status) {
                User.STATUS_APPROVED -> LoginResult.Success(user)
                User.STATUS_PENDING -> LoginResult.PendingApproval
                User.STATUS_REJECTED -> LoginResult.Rejected
                else -> LoginResult.PendingApproval
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error in loginWithEmailOrPhone", e)
            val localUser = userDao.getUserByIdentifier(cleanId)
                ?: userDao.getUserByPhone(cleanId)
                ?: userDao.getUserByEmail(cleanId)
                ?: return LoginResult.NotFound

            if (localUser.password.isNotBlank() && cleanPass.isNotEmpty() && localUser.password != cleanPass) {
                return LoginResult.WrongPassword
            }

            return when (localUser.status) {
                User.STATUS_APPROVED -> LoginResult.Success(localUser)
                User.STATUS_PENDING -> LoginResult.PendingApproval
                User.STATUS_REJECTED -> LoginResult.Rejected
                else -> LoginResult.PendingApproval
            }
        }
    }

    suspend fun loginWithPhone(phoneNumber: String): LoginResult {
        return loginWithEmailOrPhone(phoneNumber, "")
    }

    suspend fun getUserById(userId: String): User? {
        try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (doc.exists()) {
                return doc.toObject(User::class.java)
            }
        } catch (e: Exception) {
            Log.w("CommunityRepository", "Error reading user from Firestore", e)
        }
        return userDao.getUserById(userId)
    }

    suspend fun approveUser(userId: String, role: String = User.ROLE_MEMBER) {
        try {
            firestore.collection("users").document(userId)
                .update("status", User.STATUS_APPROVED, "role", role).await()
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error approving user in Firestore", e)
        }
        userDao.updateUserStatus(userId, User.STATUS_APPROVED)
        userDao.updateUserRole(userId, role)
    }

    suspend fun rejectUser(userId: String) {
        try {
            firestore.collection("users").document(userId)
                .update("status", User.STATUS_REJECTED).await()
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error rejecting user in Firestore", e)
        }
        userDao.updateUserStatus(userId, User.STATUS_REJECTED)
    }

    suspend fun updateAnnouncement(
        text: String,
        authorName: String = "Admin / কেন্দ্রীয় পরিষদ",
        authorRole: String = "অ্যাডমিন"
    ) {
        val announcement = Announcement(
            id = "announcement_" + UUID.randomUUID().toString().take(8),
            text = text.trim(),
            isActive = true,
            authorName = authorName,
            authorRole = authorRole,
            priority = "high",
            updatedAt = System.currentTimeMillis()
        )
        try {
            val activeDocs = firestore.collection("announcements")
                .whereEqualTo("active", true).get().await()
            for (doc in activeDocs.documents) {
                doc.reference.update("active", false).await()
            }
            firestore.collection("announcements").document(announcement.id).set(announcement).await()
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error updating announcement in Firestore", e)
        }
        announcementDao.deactivateAllAnnouncements()
        announcementDao.insertAnnouncement(announcement)
        NotificationHelper.showMessageNotification(
            context = context,
            senderName = "📢 জরুরি নোটিশ ($authorRole)",
            messageText = text.trim(),
            groupTitle = "অফিশিয়াল সার্কুলার চ্যানেল"
        )
    }

    suspend fun sendMessage(
        groupType: String,
        sender: User,
        text: String,
        imageUrl: String? = null,
        videoUrl: String? = null
    ): Boolean {
        if (groupType == Message.GROUP_IMPORTANT && !sender.canPostInImportantGroup) {
            return false
        }

        val message = Message(
            id = "msg_" + UUID.randomUUID().toString().take(10),
            groupType = groupType,
            senderId = sender.id,
            senderName = sender.name,
            senderRole = sender.role,
            text = text.trim(),
            imageUrl = imageUrl,
            videoUrl = videoUrl,
            timestamp = System.currentTimeMillis(),
            reactions = ""
        )

        try {
            firestore.collection("messages").document(message.id).set(message).await()
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error writing message to Firestore", e)
        }
        notifiedMessageIds.add(message.id)
        messageDao.insertMessage(message)

        // Show immediate real-time WhatsApp-style notification
        NotificationHelper.showMessageNotification(
            context = context,
            senderName = "${sender.name} (${sender.roleBadge})",
            messageText = if (text.isNotBlank()) text.trim() else "📷 ছবি পাঠানো হয়েছে",
            groupTitle = if (groupType == Message.GROUP_IMPORTANT) "Important Members Group" else "General Group Chat"
        )
        return true
    }

    suspend fun addReaction(messageId: String, currentReactions: String, reactionEmoji: String) {
        val updated = if (currentReactions.isEmpty()) {
            reactionEmoji
        } else {
            "$currentReactions $reactionEmoji"
        }
        try {
            firestore.collection("messages").document(messageId)
                .update("reactions", updated).await()
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error adding reaction to Firestore", e)
        }
        messageDao.updateMessageReactions(messageId, updated)
    }

    suspend fun deleteAllExamples() {
        try {
            val exampleUsers = listOf("leader_1", "leader_2", "member_1", "pending_1")
            for (uid in exampleUsers) {
                firestore.collection("users").document(uid).delete().await()
            }
            val docs = firestore.collection("messages").get().await()
            for (doc in docs.documents) {
                if (doc.id.startsWith("msg_gen_") || doc.id.startsWith("msg_imp_")) {
                    doc.reference.delete().await()
                }
            }
            firestore.collection("announcements").document("announcement_seed").delete().await()
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error deleting examples from Firestore", e)
        }
        userDao.deleteExampleUsers()
        messageDao.deleteExampleMessages()
        announcementDao.deleteExampleAnnouncements()
    }

    suspend fun deleteAllMessages() {
        try {
            val docs = firestore.collection("messages").get().await()
            for (doc in docs.documents) {
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error clearing messages from Firestore", e)
        }
        messageDao.deleteAllMessages()
    }

    suspend fun deleteUser(userId: String) {
        try {
            firestore.collection("users").document(userId).delete().await()
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error deleting user from Firestore", e)
        }
        userDao.deleteUser(userId)
    }

    suspend fun deleteMessage(messageId: String) {
        try {
            firestore.collection("messages").document(messageId).delete().await()
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error deleting message from Firestore", e)
        }
        messageDao.deleteMessage(messageId)
    }

    private suspend fun cleanupAllExampleData() {
        userDao.deleteExampleUsers()
        messageDao.deleteExampleMessages()
        announcementDao.deleteExampleAnnouncements()
    }

    private suspend fun seedInitialAdminOnlyIfEmpty() {
        val existingAdmin = userDao.getUserById("admin_1")
        if (existingAdmin == null) {
            val adminUser = User(
                id = "admin_1",
                name = "কেন্দ্রীয় অ্যাডমিন (Admin)",
                phoneNumber = "01700000001",
                district = "ঢাকা (Dhaka)",
                thana = "রমনা (Ramna)",
                fatherName = "মৌলভী আব্দুর রহমান",
                status = User.STATUS_APPROVED,
                role = User.ROLE_ADMIN,
                email = "jobayedshikder2@gmail.com",
                createdAt = System.currentTimeMillis()
            )
            try {
                firestore.collection("users").document(adminUser.id).set(adminUser).await()
            } catch (e: Exception) {
                Log.w("CommunityRepository", "Could not seed admin to Firestore: ${e.message}")
            }
            userDao.insertUser(adminUser)
        }
    }
}
