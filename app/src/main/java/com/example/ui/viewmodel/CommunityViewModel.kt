package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Announcement
import com.example.data.model.Message
import com.example.data.model.User
import com.example.data.repository.CommunityRepository
import com.example.data.repository.LoginResult
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ScreenState {
    object Login : ScreenState()
    object Register : ScreenState()
    object AdminLogin : ScreenState()
    object Dashboard : ScreenState()
    data class ChatRoom(val groupType: String) : ScreenState()
    object AdminPanel : ScreenState()
    object WebCodeViewer : ScreenState()
}

class CommunityViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CommunityRepository(application)

    private val _currentScreen = MutableStateFlow<ScreenState>(ScreenState.Login)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _googleRegistrationData = MutableStateFlow<LoginResult.NeedsRegistration?>(null)
    val googleRegistrationData: StateFlow<LoginResult.NeedsRegistration?> = _googleRegistrationData.asStateFlow()

    // Screen navigation stack for back handling
    private val backStack = mutableListOf<ScreenState>()

    // Real-time Messages from Firestore
    val generalMessages: StateFlow<List<Message>> = repository.getMessages(Message.GROUP_GENERAL)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val importantMessages: StateFlow<List<Message>> = repository.getMessages(Message.GROUP_IMPORTANT)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastGeneralMessage: StateFlow<Message?> = repository.getLastMessage(Message.GROUP_GENERAL)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val lastImportantMessage: StateFlow<Message?> = repository.getLastMessage(Message.GROUP_IMPORTANT)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Real-time Announcements from Firestore
    val activeAnnouncement: StateFlow<Announcement?> = repository.activeAnnouncement
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allAnnouncements: StateFlow<List<Announcement>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-time Users from Firestore
    val pendingUsers: StateFlow<List<User>> = repository.pendingUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Network status
    val isOnline: StateFlow<Boolean> = repository.networkMonitor.isOnline
    val connectionType: StateFlow<String> = repository.networkMonitor.connectionType

    // UI Feedback state
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _isRegistering = MutableStateFlow(false)
    val isRegistering: StateFlow<Boolean> = _isRegistering.asStateFlow()

    private val _registrationSuccessDialog = MutableStateFlow(false)
    val registrationSuccessDialog: StateFlow<Boolean> = _registrationSuccessDialog.asStateFlow()

    init {
        // Check if user is already signed in with Firebase Auth on start
        viewModelScope.launch {
            val fbUser = Firebase.auth.currentUser
            if (fbUser != null) {
                handleGoogleUser(fbUser)
            }
        }
    }

    fun handleGoogleUser(firebaseUser: FirebaseUser) {
        viewModelScope.launch {
            val result = repository.checkGoogleUser(firebaseUser)
            when (result) {
                is LoginResult.Success -> {
                    _currentUser.value = result.user
                    _isAdminLoggedIn.value = (result.user.role == User.ROLE_ADMIN)
                    _currentScreen.value = ScreenState.Dashboard
                    _uiMessage.value = "স্বাগতম ${result.user.name}!"
                }
                is LoginResult.PendingApproval -> {
                    _uiMessage.value = "আপনার নিবন্ধন অপেক্ষমাণ রয়েছে। অ্যাডমিন অনুমোদনের পর ড্যাশবোর্ড সচল হবে।"
                }
                is LoginResult.Rejected -> {
                    _uiMessage.value = "আপনার আবেদন বাতিল করা হয়েছে। অ্যাডমিনের সাথে যোগাযোগ করুন।"
                }
                is LoginResult.NeedsRegistration -> {
                    _googleRegistrationData.value = result
                    _currentScreen.value = ScreenState.Register
                }
                is LoginResult.NotFound -> {
                    _currentScreen.value = ScreenState.Register
                }
            }
        }
    }

    fun navigateTo(screen: ScreenState) {
        backStack.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (backStack.isNotEmpty()) {
            _currentScreen.value = backStack.removeAt(backStack.size - 1)
            return true
        }
        return false
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun dismissRegistrationDialog() {
        _registrationSuccessDialog.value = false
        _currentScreen.value = ScreenState.Login
    }

    fun login(identifier: String, password: String = "", onResult: (LoginResult) -> Unit) {
        viewModelScope.launch {
            val result = repository.loginWithEmailOrPhone(identifier, password)
            when (result) {
                is LoginResult.Success -> {
                    _currentUser.value = result.user
                    _isAdminLoggedIn.value = (result.user.role == User.ROLE_ADMIN)
                    _currentScreen.value = ScreenState.Dashboard
                    _uiMessage.value = "স্বাগতম ${result.user.name}!"
                }
                is LoginResult.WrongPassword -> {
                    _uiMessage.value = "ভুল পাসওয়ার্ড প্রদান করেছেন।"
                }
                is LoginResult.PendingApproval -> {
                    _uiMessage.value = "আপনার আবেদন অপেক্ষমাণ রয়েছে। অ্যাডমিন অনুমোদনের পর লগইন করতে পারবেন।"
                }
                is LoginResult.Rejected -> {
                    _uiMessage.value = "আপনার নিবন্ধন আবেদন প্রত্যাখ্যান করা হয়েছে।"
                }
                is LoginResult.NotFound -> {
                    _uiMessage.value = "কোনো অ্যাকাউন্ট পাওয়া যায়নি। অনুগ্রহ করে প্রথমে নিবন্ধন করুন।"
                }
                is LoginResult.NeedsRegistration -> {
                    _googleRegistrationData.value = result
                    _currentScreen.value = ScreenState.Register
                }
            }
            onResult(result)
        }
    }

    fun register(
        name: String,
        phone: String,
        email: String = "",
        password: String = "",
        district: String,
        thana: String,
        fatherName: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        if (name.isBlank() || phone.isBlank() || district.isBlank() || thana.isBlank() || fatherName.isBlank()) {
            onComplete(false, "সকল প্রয়োজনীয় তথ্য পূরণ করা আবশ্যক / All required fields must be filled")
            return
        }

        val googleData = _googleRegistrationData.value
        viewModelScope.launch {
            _isRegistering.value = true
            val result = if (googleData != null) {
                repository.registerGoogleUser(
                    uid = googleData.googleUid,
                    name = name,
                    phone = phone,
                    district = district,
                    thana = thana,
                    fatherName = fatherName,
                    email = if (email.isNotBlank()) email else googleData.email
                )
            } else {
                repository.registerUser(
                    name = name,
                    phone = phone,
                    email = email,
                    password = password,
                    district = district,
                    thana = thana,
                    fatherName = fatherName
                )
            }
            _isRegistering.value = false

            if (result.isSuccess) {
                _registrationSuccessDialog.value = true
                _googleRegistrationData.value = null
                onComplete(true, "Registration successful. Waiting for Admin approval.")
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "নিবন্ধন ব্যর্থ হয়েছে"
                _uiMessage.value = errorMsg
                onComplete(false, errorMsg)
            }
        }
    }

    fun adminLogin(user: String, pass: String): Boolean {
        if (user.trim() == "admin" && pass.trim() == "admin123") {
            _isAdminLoggedIn.value = true
            viewModelScope.launch {
                val adminUser = repository.getUserById("admin_1")
                    ?: User(
                        id = "admin_1",
                        name = "কেন্দ্রীয় অ্যাডমিন (Admin)",
                        phoneNumber = "01700000001",
                        district = "ঢাকা (Dhaka)",
                        thana = "রমনা (Ramna)",
                        fatherName = "মৌলভী আব্দুর রহমান",
                        status = User.STATUS_APPROVED,
                        role = User.ROLE_ADMIN
                    )
                _currentUser.value = adminUser
            }
            _currentScreen.value = ScreenState.AdminPanel
            _uiMessage.value = "অ্যাডমিন প্যানেলে স্বাগতম!"
            return true
        }
        _uiMessage.value = "ভুল ইউজারনেম অথবা পাসওয়ার্ড (ডিফল্ট: admin / admin123)"
        return false
    }

    fun logout() {
        Firebase.auth.signOut()
        _currentUser.value = null
        _isAdminLoggedIn.value = false
        _currentScreen.value = ScreenState.Login
        backStack.clear()
        _uiMessage.value = "লগআউট সম্পন্ন হয়েছে"
    }

    fun switchUserQuick(userId: String) {
        viewModelScope.launch {
            val user = repository.getUserById(userId)
            if (user != null) {
                _currentUser.value = user
                _isAdminLoggedIn.value = (user.role == User.ROLE_ADMIN)
                _uiMessage.value = "অ্যাকাউন্ট পরিবর্তিত: ${user.name}"
            }
        }
    }

    fun approveUser(userId: String, role: String = User.ROLE_MEMBER) {
        viewModelScope.launch {
            repository.approveUser(userId, role)
            _uiMessage.value = "সদস্য সফলভাবে অনুমোদিত হয়েছে!"
        }
    }

    fun rejectUser(userId: String) {
        viewModelScope.launch {
            repository.rejectUser(userId)
            _uiMessage.value = "আবেদন প্রত্যাখ্যান করা হয়েছে।"
        }
    }

    fun updateAnnouncement(newNotice: String) {
        if (newNotice.isBlank()) return
        viewModelScope.launch {
            val author = _currentUser.value?.name ?: "অ্যাডমিন"
            val role = _currentUser.value?.roleBadge ?: "অ্যাডমিন"
            repository.updateAnnouncement(newNotice, author, role)
            _uiMessage.value = "জরুরি নোটিশ সফলভাবে আপডেট করা হয়েছে!"
        }
    }

    fun sendMessage(groupType: String, text: String, imageUrl: String? = null, videoUrl: String? = null) {
        val user = _currentUser.value ?: return
        if (text.isBlank() && imageUrl == null && videoUrl == null) return

        viewModelScope.launch {
            val sent = repository.sendMessage(groupType, user, text, imageUrl, videoUrl)
            if (!sent) {
                _uiMessage.value = "শুধুমাত্র অ্যাডমিন ও অনুমোদিত নেতৃবৃন্দ এই গ্রুপে পোস্ট করতে পারেন!"
            }
        }
    }

    fun addReaction(message: Message, emoji: String) {
        viewModelScope.launch {
            repository.addReaction(message.id, message.reactions, emoji)
        }
    }

    fun deleteAllExamples(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteAllExamples()
            _uiMessage.value = "সকল এক্সাম্পল ডেটা সফলভাবে মুছে ফেলা হয়েছে!"
            onComplete()
        }
    }

    fun deleteAllMessages() {
        viewModelScope.launch {
            repository.deleteAllMessages()
            _uiMessage.value = "সকল বার্তা মুছে ফেলা হয়েছে!"
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUser(userId)
            _uiMessage.value = "ব্যবহারকারীকে তালিকা থেকে মুছে ফেলা হয়েছে।"
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
            _uiMessage.value = "বার্তাটি মুছে ফেলা হয়েছে।"
        }
    }
}
