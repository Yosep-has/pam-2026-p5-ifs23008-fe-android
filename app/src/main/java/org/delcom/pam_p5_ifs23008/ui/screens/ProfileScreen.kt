package org.delcom.pam_p5_ifs23008.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_p5_ifs23008.R
import org.delcom.pam_p5_ifs23008.helper.ConstHelper
import org.delcom.pam_p5_ifs23008.helper.RouteHelper
import org.delcom.pam_p5_ifs23008.helper.ToolsHelper
import org.delcom.pam_p5_ifs23008.helper.ToolsHelper.uriToMultipart
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

    // Edit info (nama, username, about)
    var showEditInfo by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editUsername by remember { mutableStateOf("") }
    var editAbout by remember { mutableStateOf("") }
    var isSavingInfo by remember { mutableStateOf(false) }

    // Foto profil
    var isSavingPhoto by remember { mutableStateOf(false) }
    var photoTimestamp by remember { mutableStateOf("0") }

    // ── PickVisualMedia (sama dengan TodosDetailScreen) ───────────────────────
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val token = authToken ?: return@rememberLauncherForActivityResult
            isSavingPhoto = true
            val filePart = uriToMultipart(context, uri, "file")
            todoViewModel.putUserMePhoto(token, filePart)
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
            val p = (uiStateTodo.profile as ProfileUIState.Success).data
            profile = p; editName = p.name; editUsername = p.username; editAbout = p.about ?: ""
            isLoading = false
            return@LaunchedEffect
        }
        todoViewModel.getProfile(authToken ?: "")
    }

    LaunchedEffect(uiStateTodo.profile) {
        if (uiStateTodo.profile !is ProfileUIState.Loading) {
            isLoading = false
            if (uiStateTodo.profile is ProfileUIState.Success) {
                val p = (uiStateTodo.profile as ProfileUIState.Success).data
                profile = p; editName = p.name; editUsername = p.username; editAbout = p.about ?: ""
            } else {
                RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            }
        }
    }

    // Observe hasil simpan profil (nama/username/about)
    LaunchedEffect(uiStateTodo.profileChange) {
        if (isSavingInfo && uiStateTodo.profileChange !is TodoActionUIState.Loading) {
            isSavingInfo = false
            if (uiStateTodo.profileChange is TodoActionUIState.Success) {
                isActionError = false
                actionMessage = "Informasi akun berhasil diperbarui"
                showEditInfo = false
                todoViewModel.getProfile(authToken ?: "")
            } else if (uiStateTodo.profileChange is TodoActionUIState.Error) {
                isActionError = true
                actionMessage = (uiStateTodo.profileChange as TodoActionUIState.Error).message
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

    if (isLoading || profile == null) { LoadingUI(); return }

    val menuItems = listOf(
        TopAppBarMenuItem(text = "Profile", icon = Icons.Filled.Person,
            route = ConstHelper.RouteNames.Profile.path),
        TopAppBarMenuItem(text = "Logout", icon = Icons.AutoMirrored.Filled.Logout,
            route = null, onClick = { onLogout(authToken ?: "") })
    )

    Column(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBarComponent(navController = navController, title = "Profile",
            showBackButton = false, customMenuItems = menuItems)
        Box(modifier = Modifier.weight(1f)) {
            ProfileUI(
                profile = profile!!,
                photoTimestamp = photoTimestamp,
                isSavingPhoto = isSavingPhoto,
                onPickPhoto = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                actionMessage = actionMessage,
                isActionError = isActionError,
                onClearMessage = { actionMessage = null },
                showEditInfo = showEditInfo,
                editName = editName,
                editUsername = editUsername,
                editAbout = editAbout,
                isSavingInfo = isSavingInfo,
                onToggleEditInfo = {
                    showEditInfo = !showEditInfo
                    if (!showEditInfo) {
                        editName = profile!!.name
                        editUsername = profile!!.username
                        editAbout = profile!!.about ?: ""
                    }
                },
                onEditNameChange = { editName = it },
                onEditUsernameChange = { editUsername = it },
                onEditAboutChange = { editAbout = it },
                onSaveInfo = {
                    isSavingInfo = true
                    actionMessage = null
                    todoViewModel.putProfile(authToken ?: "", editName, editUsername,
                        editAbout.ifBlank { null })
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
    editAbout: String = "",
    isSavingInfo: Boolean = false,
    onToggleEditInfo: () -> Unit = {},
    onEditNameChange: (String) -> Unit = {},
    onEditUsernameChange: (String) -> Unit = {},
    onEditAboutChange: (String) -> Unit = {},
    onSaveInfo: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)
    ) {
        // ── Header: Foto ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = ToolsHelper.getUserImage(profile.id, photoTimestamp),
                        contentDescription = "Photo Profil",
                        placeholder = painterResource(R.drawable.img_placeholder),
                        error = painterResource(R.drawable.img_placeholder),
                        modifier = Modifier.size(110.dp).clip(CircleShape)
                            .border(3.dp, Color.White, CircleShape).clickable { onPickPhoto() },
                        contentScale = ContentScale.Crop
                    )
                    // Badge kamera
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary).clickable { onPickPhoto() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSavingPhoto) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Ganti foto",
                                tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = profile.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(text = "@${profile.username}", fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                // Tampilkan about di bawah username jika ada
                if (!profile.about.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = profile.about,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        }

        // ── Pesan aksi ───────────────────────────────────────────────────────
        if (actionMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActionError) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = actionMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isActionError) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onClearMessage) { Text("✕", fontSize = 12.sp) }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Kartu: Informasi Akun + Tentang ──────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Informasi Akun", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    TextButton(onClick = onToggleEditInfo) {
                        Text(if (showEditInfo) "Batal" else "Ubah")
                    }
                }

                if (!showEditInfo) {
                    // Tampilan read-only
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileInfoRow("Nama", profile.name)
                    Spacer(modifier = Modifier.height(4.dp))
                    ProfileInfoRow("Username", "@${profile.username}")
                    Spacer(modifier = Modifier.height(4.dp))
                    ProfileInfoRow("Tentang", profile.about?.ifBlank { "-" } ?: "-")
                } else {
                    // Form edit
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editName, onValueChange = onEditNameChange,
                        label = { Text("Nama") }, modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editUsername, onValueChange = onEditUsernameChange,
                        label = { Text("Username") }, modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editAbout,
                        onValueChange = onEditAboutChange,
                        label = { Text("Tentang (opsional)") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        maxLines = 4,
                        minLines = 2,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done),
                        placeholder = { Text("Ceritakan sedikit tentang dirimu...") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSaveInfo,
                        enabled = !isSavingInfo && editName.isNotBlank() && editUsername.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSavingInfo) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Simpan Perubahan")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(80.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}
