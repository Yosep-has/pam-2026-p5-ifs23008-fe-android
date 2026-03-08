package org.delcom.pam_p5_ifs23008.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_p5_ifs23008.R
import org.delcom.pam_p5_ifs23008.helper.ConstHelper
import org.delcom.pam_p5_ifs23008.helper.RouteHelper
import org.delcom.pam_p5_ifs23008.helper.ToolsHelper
import org.delcom.pam_p5_ifs23008.network.todos.data.ResponseUserData
import org.delcom.pam_p5_ifs23008.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23008.ui.components.LoadingUI
import org.delcom.pam_p5_ifs23008.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23008.ui.components.TopAppBarMenuItem
import org.delcom.pam_p5_ifs23008.ui.viewmodels.AuthLogoutUIState
import org.delcom.pam_p5_ifs23008.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23008.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23008.ui.viewmodels.ProfileUIState
import org.delcom.pam_p5_ifs23008.ui.viewmodels.TodoActionUIState
import org.delcom.pam_p5_ifs23008.ui.viewmodels.TodoViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateTodo by todoViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf<ResponseUserData?>(null) }
    var authToken by remember { mutableStateOf<String?>(null) }

    // Feedback pesan aksi
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var isActionError by remember { mutableStateOf(false) }

    // State form ubah info
    var showEditInfo by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editUsername by remember { mutableStateOf("") }
    var isSavingInfo by remember { mutableStateOf(false) }

    // State form ubah kata sandi
    var showEditPassword by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var isSavingPassword by remember { mutableStateOf(false) }

    // State ubah foto
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var isSavingPhoto by remember { mutableStateOf(false) }
    var photoTimestamp by remember { mutableStateOf("0") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri
            val token = authToken ?: return@rememberLauncherForActivityResult
            isSavingPhoto = true
            val filePart = ToolsHelper.uriToMultipart(context, uri, "file")
            todoViewModel.putProfilePhoto(token, filePart)
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        if (uiStateAuth.auth !is AuthUIState.Success) {
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }
        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
        if (uiStateTodo.profile is ProfileUIState.Success) {
            profile = (uiStateTodo.profile as ProfileUIState.Success).data
            editName = profile?.name ?: ""
            editUsername = profile?.username ?: ""
            isLoading = false
            return@LaunchedEffect
        }
        todoViewModel.getProfile(authToken ?: "")
    }

    LaunchedEffect(uiStateTodo.profile) {
        if (uiStateTodo.profile !is ProfileUIState.Loading) {
            isLoading = false
            if (uiStateTodo.profile is ProfileUIState.Success) {
                profile = (uiStateTodo.profile as ProfileUIState.Success).data
                editName = profile?.name ?: ""
                editUsername = profile?.username ?: ""
            } else {
                RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            }
        }
    }

    // Tangkap hasil ubah info akun
    LaunchedEffect(uiStateTodo.profileChange) {
        if (isSavingInfo && uiStateTodo.profileChange !is TodoActionUIState.Loading) {
            isSavingInfo = false
            if (uiStateTodo.profileChange is TodoActionUIState.Success) {
                isActionError = false
                actionMessage = "Informasi akun berhasil diperbarui"
                showEditInfo = false
                // Refresh profil
                todoViewModel.getProfile(authToken ?: "")
            } else if (uiStateTodo.profileChange is TodoActionUIState.Error) {
                isActionError = true
                actionMessage = (uiStateTodo.profileChange as TodoActionUIState.Error).message
            }
        }
    }

    // Tangkap hasil ubah kata sandi
    LaunchedEffect(uiStateTodo.profileChangePassword) {
        if (isSavingPassword && uiStateTodo.profileChangePassword !is TodoActionUIState.Loading) {
            isSavingPassword = false
            if (uiStateTodo.profileChangePassword is TodoActionUIState.Success) {
                isActionError = false
                actionMessage = "Kata sandi berhasil diubah. Silakan login kembali."
                showEditPassword = false
                oldPassword = ""
                newPassword = ""
            } else if (uiStateTodo.profileChangePassword is TodoActionUIState.Error) {
                isActionError = true
                actionMessage = (uiStateTodo.profileChangePassword as TodoActionUIState.Error).message
            }
        }
    }

    // Tangkap hasil ubah foto
    LaunchedEffect(uiStateTodo.profileChangePhoto) {
        if (isSavingPhoto && uiStateTodo.profileChangePhoto !is TodoActionUIState.Loading) {
            isSavingPhoto = false
            if (uiStateTodo.profileChangePhoto is TodoActionUIState.Success) {
                isActionError = false
                actionMessage = "Foto profil berhasil diperbarui"
                // Update timestamp agar Coil memuat ulang gambar terbaru
                photoTimestamp = System.currentTimeMillis().toString()
            } else if (uiStateTodo.profileChangePhoto is TodoActionUIState.Error) {
                isActionError = true
                actionMessage = (uiStateTodo.profileChangePhoto as TodoActionUIState.Error).message
            }
        }
    }

    fun onLogout(token: String) {
        isLoading = true
        authViewModel.logout(token)
    }

    LaunchedEffect(uiStateAuth.authLogout) {
        if (uiStateAuth.authLogout !is AuthLogoutUIState.Loading) {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
        }
    }

    if (isLoading || profile == null) {
        LoadingUI()
        return
    }

    val menuItems = listOf(
        TopAppBarMenuItem(
            text = "Profile",
            icon = Icons.Filled.Person,
            route = ConstHelper.RouteNames.Profile.path
        ),
        TopAppBarMenuItem(
            text = "Logout",
            icon = Icons.AutoMirrored.Filled.Logout,
            route = null,
            onClick = { onLogout(authToken ?: "") }
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBarComponent(
            navController = navController,
            title = "Profile",
            showBackButton = false,
            customMenuItems = menuItems
        )

        Box(modifier = Modifier.weight(1f)) {
            ProfileUI(
                profile = profile!!,
                photoTimestamp = photoTimestamp,
                isSavingPhoto = isSavingPhoto,
                onPickPhoto = { photoPickerLauncher.launch("image/*") },

                actionMessage = actionMessage,
                isActionError = isActionError,
                onClearMessage = { actionMessage = null },

                // Form ubah info akun
                showEditInfo = showEditInfo,
                editName = editName,
                editUsername = editUsername,
                isSavingInfo = isSavingInfo,
                onToggleEditInfo = {
                    showEditInfo = !showEditInfo
                    if (!showEditInfo) {
                        editName = profile!!.name
                        editUsername = profile!!.username
                    }
                },
                onEditNameChange = { editName = it },
                onEditUsernameChange = { editUsername = it },
                onSaveInfo = {
                    isSavingInfo = true
                    actionMessage = null
                    todoViewModel.putProfile(authToken ?: "", editName, editUsername)
                },

                // Form ubah kata sandi
                showEditPassword = showEditPassword,
                oldPassword = oldPassword,
                newPassword = newPassword,
                oldPasswordVisible = oldPasswordVisible,
                newPasswordVisible = newPasswordVisible,
                isSavingPassword = isSavingPassword,
                onToggleEditPassword = {
                    showEditPassword = !showEditPassword
                    if (!showEditPassword) { oldPassword = ""; newPassword = "" }
                },
                onOldPasswordChange = { oldPassword = it },
                onNewPasswordChange = { newPassword = it },
                onToggleOldVisible = { oldPasswordVisible = !oldPasswordVisible },
                onToggleNewVisible = { newPasswordVisible = !newPasswordVisible },
                onSavePassword = {
                    isSavingPassword = true
                    actionMessage = null
                    todoViewModel.putProfilePassword(authToken ?: "", oldPassword, newPassword)
                },
            )
        }

        BottomNavComponent(navController = navController)
    }
}

@Composable
fun ProfileUI(
    profile: ResponseUserData,
    photoTimestamp: String = "0",
    isSavingPhoto: Boolean = false,
    onPickPhoto: () -> Unit = {},

    actionMessage: String? = null,
    isActionError: Boolean = false,
    onClearMessage: () -> Unit = {},

    showEditInfo: Boolean = false,
    editName: String = "",
    editUsername: String = "",
    isSavingInfo: Boolean = false,
    onToggleEditInfo: () -> Unit = {},
    onEditNameChange: (String) -> Unit = {},
    onEditUsernameChange: (String) -> Unit = {},
    onSaveInfo: () -> Unit = {},

    showEditPassword: Boolean = false,
    oldPassword: String = "",
    newPassword: String = "",
    oldPasswordVisible: Boolean = false,
    newPasswordVisible: Boolean = false,
    isSavingPassword: Boolean = false,
    onToggleEditPassword: () -> Unit = {},
    onOldPasswordChange: (String) -> Unit = {},
    onNewPasswordChange: (String) -> Unit = {},
    onToggleOldVisible: () -> Unit = {},
    onToggleNewVisible: () -> Unit = {},
    onSavePassword: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // ── Header: Foto Profil ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Foto profil (klik untuk ganti)
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = ToolsHelper.getUserImage(profile.id, photoTimestamp),
                        contentDescription = "Photo Profil",
                        placeholder = painterResource(R.drawable.img_placeholder),
                        error = painterResource(R.drawable.img_placeholder),
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color.White, CircleShape)
                            .clickable { onPickPhoto() },
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onPickPhoto() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSavingPhoto) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Ganti foto",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = profile.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "@${profile.username}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Pesan aksi (sukses / error) ──────────────────────────────────────
        if (actionMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActionError)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = actionMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isActionError)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onClearMessage) {
                        Text("✕", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Kartu: Ubah Informasi Akun ───────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header kartu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Informasi Akun",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onToggleEditInfo) {
                        Text(if (showEditInfo) "Batal" else "Ubah")
                    }
                }

                if (!showEditInfo) {
                    // Tampilan read-only
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileInfoRow(label = "Nama", value = profile.name)
                    Spacer(modifier = Modifier.height(4.dp))
                    ProfileInfoRow(label = "Username", value = "@${profile.username}")
                } else {
                    // Form edit
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editName,
                        onValueChange = onEditNameChange,
                        label = { Text("Nama") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = onEditUsernameChange,
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSaveInfo,
                        enabled = !isSavingInfo && editName.isNotBlank() && editUsername.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSavingInfo) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Simpan Perubahan")
                    }
                }
            }
        }

        // ── Kartu: Ubah Kata Sandi ───────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kata Sandi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onToggleEditPassword) {
                        Text(if (showEditPassword) "Batal" else "Ubah")
                    }
                }

                if (showEditPassword) {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Kata sandi lama
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = onOldPasswordChange,
                        label = { Text("Kata Sandi Lama") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (oldPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = onToggleOldVisible) {
                                Icon(
                                    imageVector = if (oldPasswordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Kata sandi baru
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = onNewPasswordChange,
                        label = { Text("Kata Sandi Baru") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = onToggleNewVisible) {
                                Icon(
                                    imageVector = if (newPasswordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSavePassword,
                        enabled = !isSavingPassword && oldPassword.isNotBlank() && newPassword.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSavingPassword) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Simpan Kata Sandi")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}