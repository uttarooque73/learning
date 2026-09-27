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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    AuthShell("Welcome to NetGuard") {
        OutlinedTextField(email, onEmailChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, onPasswordChange, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("Login") }
        TextButton(onClick = onCreateAccount) { Text("Create account") }
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
    AuthShell("Create your NetGuard account") {
        OutlinedTextField(displayName, onDisplayNameChange, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(email, onEmailChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, onPasswordChange, label = { Text("Password (8+ characters)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(confirmPassword, onConfirmPasswordChange, label = { Text("Confirm password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = onSignUp, modifier = Modifier.fillMaxWidth()) { Text("Create account") }
        TextButton(onClick = onLogin) { Text("Already have an account? Login") }
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
        Button(onClick = onSelectImage) { Text("Choose profile image") }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Photo storage", style = MaterialTheme.typography.titleMedium)
                Text("NetGuard uses Android's system photo picker and stores the selected image in app-private storage. No broad storage permission is required.")
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text("Save profile") }
        TextButton(onClick = onLogout) { Text("Log out") }
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
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Mobile Numbers", style = MaterialTheme.typography.headlineSmall)
        Text("Choose numbers from your Android contacts. NetGuard stores only the selected name and number locally.")
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (deviceContacts.isEmpty()) {
            Text("No phone contacts are available, or Contacts permission has not been granted.")
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(deviceContacts, key = { it.id + it.phoneNumber }) { contact ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(contact.name, style = MaterialTheme.typography.titleSmall)
                                Text(contact.phoneNumber)
                            }
                            Button(onClick = { onAddContact(contact) }) { Text("Add") }
                        }
                    }
                }
            }
        }
        if (savedContacts.isNotEmpty()) {
            Text("Selected numbers", style = MaterialTheme.typography.titleMedium)
            savedContacts.forEach { contact ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(contact.name + " — " + contact.phoneNumber, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onRemoveContact(contact.id) }) { Text("Remove") }
                }
            }
        }
    }
}
