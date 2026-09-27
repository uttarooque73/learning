package com.uttarooque73.netguard.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import com.uttarooque73.netguard.security.SavedContact
import com.uttarooque73.netguard.security.UserContactStore

@Composable
fun LoginScreen(
    email: String,
    password: String,
    error: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onCreateAccount: () -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }
    AuthShell("Welcome to NetGuard") {
        OutlinedTextField(email, onEmailChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(password, onPasswordChange, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation())
        TextButton(onClick = { showPassword = !showPassword }) { Text(if (showPassword) "Hide password" else "Show password") }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LoadingButton(onClick = onLogin, modifier = Modifier.fillMaxWidth(), enabled = email.isNotBlank() && password.isNotBlank()) { Text("Login") }
        LoadingTextButton(onClick = onCreateAccount) { Text("Create account") }
    }
}

@Composable
fun SignUpScreen(
    displayName: String,
    email: String,
    password: String,
    confirmPassword: String,
    error: String?,
    onDisplayNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSignUp: () -> Unit,
    onLogin: () -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }
    AuthShell("Create your NetGuard account") {
        OutlinedTextField(displayName, onDisplayNameChange, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(email, onEmailChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(password, onPasswordChange, label = { Text("Password (8+ characters)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation())
        OutlinedTextField(confirmPassword, onConfirmPasswordChange, label = { Text("Confirm password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation())
        TextButton(onClick = { showPassword = !showPassword }) { Text(if (showPassword) "Hide passwords" else "Show passwords") }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LoadingButton(onClick = onSignUp, modifier = Modifier.fillMaxWidth(), enabled = displayName.isNotBlank() && email.isNotBlank() && password.length >= 8 && password == confirmPassword) { Text("Create account") }
        LoadingTextButton(onClick = onLogin) { Text("Already have an account? Login") }
    }
}

@Composable
private fun AuthShell(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("NETGUARD", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.size(8.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.size(20.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
        }
    }
}

@Composable
fun ProfileScreen(
    profile: com.uttarooque73.netguard.security.UserProfile,
    displayName: String,
    selectedImage: Uri?,
    error: String?,
    onDisplayNameChange: (String) -> Unit,
    onSelectImage: () -> Unit,
    onSave: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("My Profile", style = MaterialTheme.typography.headlineSmall)
        val path = profile.imagePath
        val bitmap = selectedImage?.let { runCatching {
            androidx.compose.ui.platform.LocalContext.current.contentResolver.openInputStream(it).use { input ->
                BitmapFactory.decodeStream(input)
            }
        }.getOrNull() } ?: path?.let { BitmapFactory.decodeFile(it) }
        bitmap?.let {
            Image(it.asImageBitmap(), null, Modifier.size(112.dp).clip(CircleShape), contentScale = ContentScale.Crop)
        }
        Text("Account: " + profile.email)
        OutlinedTextField(displayName, onDisplayNameChange, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth())
        LoadingButton(onClick = onSelectImage) { Text("Choose profile image") }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Photo storage", style = MaterialTheme.typography.titleMedium)
                Text("NetGuard uses Android's system photo picker and stores the selected image in app-private storage. No broad storage permission is required.")
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LoadingButton(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text("Save profile") }
        LoadingTextButton(onClick = onLogout) { Text("Log out") }
    }
}


@Composable
fun ContactNumbersScreen(
    savedContacts: List<SavedContact>,
    deviceContacts: List<SavedContact>,
    error: String?,
    onAddContact: (SavedContact) -> Unit,
    onRemoveContact: (String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var showSavedOnly by remember { mutableStateOf(false) }
    val savedNumbers = remember(savedContacts) {
        savedContacts.map { UserContactStore.normalizePhone(it.phoneNumber) }.toSet()
    }
    val query = search.trim()
    val filteredDeviceContacts = deviceContacts.filter { contact ->
        query.isBlank() ||
            contact.name.contains(query, ignoreCase = true) ||
            contact.phoneNumber.contains(query, ignoreCase = true)
    }
    val filteredSavedContacts = savedContacts.filter { contact ->
        query.isBlank() ||
            contact.name.contains(query, ignoreCase = true) ||
            contact.phoneNumber.contains(query, ignoreCase = true)
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Mobile Numbers", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Select trusted contact numbers for quick access in NetGuard. Only selected name and number values are stored locally.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        error?.let {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(it, Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search contacts") },
            placeholder = { Text("Name or mobile number") }
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = !showSavedOnly,
                onClick = { showSavedOnly = false },
                label = { Text("Contacts") }
            )
            FilterChip(
                selected = showSavedOnly,
                onClick = { showSavedOnly = true },
                label = { Text("Selected (${savedContacts.size})") }
            )
        }

        if (showSavedOnly) {
            if (filteredSavedContacts.isEmpty()) {
                EmptyContactState("No selected numbers match your search.")
            } else {
                LazyColumn(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredSavedContacts, key = { it.id }) { contact ->
                        ContactNumberCard(contact, true, { onRemoveContact(contact.id) }, "Remove")
                    }
                }
            }
        } else if (deviceContacts.isEmpty()) {
            EmptyContactState(
                if (error != null) "Contacts could not be loaded. Check the Contacts permission."
                else "No phone numbers were found in your contacts."
            )
        } else if (filteredDeviceContacts.isEmpty()) {
            EmptyContactState("No contacts match \"$query\".")
        } else {
            LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredDeviceContacts, key = { it.id + "|" + it.phoneNumber }) { contact ->
                    val selected = UserContactStore.normalizePhone(contact.phoneNumber) in savedNumbers
                    ContactNumberCard(
                        contact,
                        selected,
                        { if (!selected) onAddContact(contact) },
                        if (selected) "Added" else "Add"
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactNumberCard(
    contact: SavedContact,
    selected: Boolean,
    onAction: () -> Unit,
    actionLabel: String
) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(contact.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    contact.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                AssistChip(onClick = {}, label = { Text(actionLabel) })
            } else {
                LoadingButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

@Composable
private fun EmptyContactState(message: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("No numbers to show", style = MaterialTheme.typography.titleMedium)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
