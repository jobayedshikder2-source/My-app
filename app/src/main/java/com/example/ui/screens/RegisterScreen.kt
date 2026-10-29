package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CommunityViewModel
import com.example.ui.viewmodel.ScreenState

val BANGLADESH_DISTRICTS = listOf(
    "ঢাকা (Dhaka)",
    "চট্টগ্রাম (Chittagong)",
    "সিলেট (Sylhet)",
    "রাজশাহী (Rajshahi)",
    "খুলনা (Khulna)",
    "বরিশাল (Barishal)",
    "রংপুর (Rangpur)",
    "ময়মনসিংহ (Mymensingh)",
    "কুমিল্লা (Cumilla)",
    "গাজীপুর (Gazipur)",
    "নারায়ণগঞ্জ (Narayanganj)",
    "বগুড়া (Bogura)",
    "কক্সবাজার (Cox's Bazar)",
    "যশোর (Jashore)",
    "ব্রাহ্মণবাড়িয়া (Brahmanbaria)",
    "দিনাজপুর (Dinajpur)",
    "পাবনা (Pabna)",
    "ফেনী (Feni)",
    "নোয়াখালী (Noakhali)",
    "টাঙ্গাইল (Tangail)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: CommunityViewModel,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("ঢাকা (Dhaka)") }
    var thana by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isDistrictDropdownOpen by remember { mutableStateOf(false) }

    val isRegistering by viewModel.isRegistering.collectAsState()
    val showSuccessDialog by viewModel.registrationSuccessDialog.collectAsState()
    val googleRegData by viewModel.googleRegistrationData.collectAsState()

    // Pre-populate name if coming from Google Sign-In
    androidx.compose.runtime.LaunchedEffect(googleRegData) {
        googleRegData?.let {
            if (name.isBlank() && it.name.isNotBlank()) {
                name = it.name
            }
        }
    }

    val textFieldColors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF000000),
        unfocusedTextColor = Color(0xFF000000),
        focusedLabelColor = Color(0xFF008069),
        unfocusedLabelColor = Color(0xFF1F2937),
        focusedPlaceholderColor = Color(0xFF6B7280),
        unfocusedPlaceholderColor = Color(0xFF6B7280),
        focusedLeadingIconColor = Color(0xFF008069),
        unfocusedLeadingIconColor = Color(0xFF008069),
        focusedBorderColor = Color(0xFF008069),
        unfocusedBorderColor = Color(0xFF9CA3AF),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "নতুন সদস্য নিবন্ধন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("register_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Google Account Connection Info Banner if coming from Google
            if (googleRegData != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "G",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4285F4)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "গুগল অ্যাকাউন্ট সংযুক্ত হয়েছে",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF000000)
                            )
                            Text(
                                text = "${googleRegData?.email ?: ""}\nনিবন্ধন সম্পন্ন করতে বাকি তথ্যগুলো পূরণ করুন।",
                                fontSize = 11.sp,
                                color = Color(0xFF1F2937),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("registration_form_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "সদস্য তথ্য ফরম (Member Information)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF000000)
                    )
                    Text(
                        text = "সকল তথ্য সঠিকভাবে পূরণ করুন। অ্যাডমিন অনুমোদনের পর আপনি গ্রুপে যুক্ত হতে পারবেন।",
                        fontSize = 12.sp,
                        color = Color(0xFF1F2937)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 1. Full Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; validationError = null },
                        label = { Text("পূর্ণ নাম (Full Name)*", color = Color(0xFF000000), fontWeight = FontWeight.Medium) },
                        placeholder = { Text("পূর্ণ নাম লিখুন", color = Color(0xFF6B7280)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF008069)
                            )
                        },
                        singleLine = true,
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_name")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Phone Number
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it; validationError = null },
                        label = { Text("ফোন নম্বর (Phone Number)*", color = Color(0xFF000000), fontWeight = FontWeight.Medium) },
                        placeholder = { Text("01XXXXXXXXX", color = Color(0xFF6B7280)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color(0xFF008069)
                            )
                        },
                        singleLine = true,
                        colors = textFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_phone")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. District (জেলা) Dropdown / Picker
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = district,
                            onValueChange = { district = it; validationError = null },
                            label = { Text("জেলা (District)*", color = Color(0xFF000000), fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationCity,
                                    contentDescription = null,
                                    tint = Color(0xFF008069)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isDistrictDropdownOpen = true }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select District",
                                        tint = Color(0xFF000000)
                                    )
                                }
                            },
                            singleLine = true,
                            colors = textFieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_district")
                        )

                        DropdownMenu(
                            expanded = isDistrictDropdownOpen,
                            onDismissRequest = { isDistrictDropdownOpen = false }
                        ) {
                            BANGLADESH_DISTRICTS.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item, color = Color(0xFF000000), fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        district = item
                                        isDistrictDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Thana (থানা)
                    OutlinedTextField(
                        value = thana,
                        onValueChange = { thana = it; validationError = null },
                        label = { Text("থানা / উপজেলা (Thana)*", color = Color(0xFF000000), fontWeight = FontWeight.Medium) },
                        placeholder = { Text("থানা বা উপজেলার নাম লিখুন", color = Color(0xFF6B7280)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = Color(0xFF008069)
                            )
                        },
                        singleLine = true,
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_thana")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. Father's Name (বাপের নাম)
                    OutlinedTextField(
                        value = fatherName,
                        onValueChange = { fatherName = it; validationError = null },
                        label = { Text("বাপের নাম (Father's Name)*", color = Color(0xFF000000), fontWeight = FontWeight.Medium) },
                        placeholder = { Text("পিতার নাম লিখুন", color = Color(0xFF6B7280)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PersonOutline,
                                contentDescription = null,
                                tint = Color(0xFF008069)
                            )
                        },
                        singleLine = true,
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_father_name")
                    )

                    if (validationError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = validationError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (name.isBlank() || phoneNumber.isBlank() || district.isBlank() || thana.isBlank() || fatherName.isBlank()) {
                                validationError = "সকল প্রয়োজনীয় ঘর পূরণ করুন (All fields are required)"
                                return@Button
                            }
                            viewModel.register(
                                name = name,
                                phone = phoneNumber,
                                district = district,
                                thana = thana,
                                fatherName = fatherName
                            ) { success, msg ->
                                if (!success) {
                                    validationError = msg
                                }
                            }
                        },
                        enabled = !isRegistering,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_registration_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008069))
                    ) {
                        if (isRegistering) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = "নিবন্ধন সম্পন্ন করুন (Submit Registration)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { viewModel.navigateTo(ScreenState.Login) },
                modifier = Modifier.testTag("already_registered_button")
            ) {
                Text(
                    text = "ইতিমধ্যে নিবন্ধিত? লগইন করুন (Already registered? Login)",
                    color = Color(0xFF008069),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Success Dialog as specified: "Registration successful. Waiting for Admin approval."
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRegistrationDialog() },
            icon = {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "নিবন্ধন সফল হয়েছে!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1B5E20)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Registration successful. Waiting for Admin approval.",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "আপনার তথ্যাদি অ্যাডমিন প্যানেলে পাঠানো হয়েছে। অ্যাডমিন অনুমোদন প্রদান করলে আপনার ফোন নম্বর দিয়ে লগইন করতে পারবেন।",
                        fontSize = 13.sp,
                        color = Color(0xFF424242),
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissRegistrationDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008069)),
                    modifier = Modifier.testTag("dialog_ok_button")
                ) {
                    Text("লগইন পাতায় ফিরে যান (Go to Login)")
                }
            }
        )
    }
}
