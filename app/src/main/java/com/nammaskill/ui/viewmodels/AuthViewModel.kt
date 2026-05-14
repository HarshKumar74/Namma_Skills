package com.nammaskill.ui.viewmodels

import android.app.Application
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.FirebaseDatabase
import com.nammaskill.domain.model.AdminModel
import com.nammaskill.domain.model.UserModel
import com.nammaskill.domain.model.NotificationModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase,
    application: Application
) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    private val geocoder = Geocoder(application, Locale.getDefault())

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    val uid = currentUser.uid
                    // Check Admins first
                    val adminSnapshot = db.getReference("Admins").child(uid).get().await()
                    if (adminSnapshot.exists()) {
                        _userRole.value = "admin"
                        return@launch
                    }
                    // Then check Users
                    val userSnapshot = db.getReference("Users").child(uid).get().await()
                    if (userSnapshot.exists()) {
                        _userRole.value = "user"
                    }
                } catch (e: Exception) {
                    Log.e("Auth", "Error checking user role", e)
                }
            }
        }
    }

    fun login(email: String, pass: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                Log.d("Auth", "Attempting login for $email as $role")
                val result = auth.signInWithEmailAndPassword(email.trim(), pass.trim()).await()
                val uid = result.user?.uid ?: ""
                
                val ref = if (role == "admin") "Admins" else "Users"
                val snapshot = db.getReference(ref).child(uid).get().await()
                
                if (snapshot.exists()) {
                    Log.d("Auth", "Login successful for $uid")
                    _userRole.value = role
                    _authState.value = AuthState.Success(role)
                } else {
                    Log.w("Auth", "Account exists but role mismatch: $uid")
                    auth.signOut()
                    _userRole.value = null
                    _authState.value = AuthState.Error("This account is not registered as a $role.")
                }
            } catch (e: Exception) {
                Log.e("Auth", "Login failed", e)
                handleError(e)
            }
        }
    }

    fun userSignup(user: UserModel, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                Log.d("Auth", "Attempting User Signup for ${user.email}")
                val result = auth.createUserWithEmailAndPassword(user.email.trim(), pass.trim()).await()
                val uid = result.user?.uid ?: ""
                val newUser = user.copy(userId = uid)
                
                db.getReference("Users").child(uid).setValue(newUser).await()
                Log.d("Auth", "User Signup and DB write successful for $uid")
                _userRole.value = "user"
                _authState.value = AuthState.Success("user")
            } catch (e: Exception) {
                Log.e("Auth", "User Signup failed", e)
                handleError(e)
            }
        }
    }

    fun adminSignup(admin: AdminModel, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                Log.d("Auth", "Attempting Admin Signup for ${admin.email}")
                
                val coordinates = withContext(Dispatchers.IO) {
                    try {
                        val addresses = geocoder.getFromLocationName(admin.address, 1)
                        if (addresses != null && addresses.isNotEmpty()) {
                            Pair(addresses[0].latitude, addresses[0].longitude)
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }

                val result = auth.createUserWithEmailAndPassword(admin.email.trim(), pass.trim()).await()
                val uid = result.user?.uid ?: ""
                
                val newAdmin = admin.copy(
                    adminId = uid,
                    latitude = coordinates?.first ?: 12.9716,
                    longitude = coordinates?.second ?: 77.5946,
                    createdAt = System.currentTimeMillis()
                )
                
                db.getReference("Admins").child(uid).setValue(newAdmin).await()

                // Create Notification for new center
                val notifId = UUID.randomUUID().toString()
                val notification = NotificationModel(
                    id = notifId,
                    title = "New Skill Center!",
                    message = "${admin.skillCenterName} has joined Namma Skill. Check them out!",
                    type = "center"
                )
                db.getReference("Notifications").child(notifId).setValue(notification).await()

                Log.d("Auth", "Admin Signup and DB write successful for $uid")
                _userRole.value = "admin"
                _authState.value = AuthState.Success("admin")
            } catch (e: Exception) {
                Log.e("Auth", "Admin Signup failed", e)
                handleError(e)
            }
        }
    }

    private fun handleError(e: Exception) {
        val message = when (e) {
            is FirebaseAuthException -> {
                Log.e("Auth", "Firebase Auth Code: ${e.errorCode}")
                when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already in use."
                    "ERROR_INVALID_EMAIL" -> "The email address is badly formatted."
                    "ERROR_WEAK_PASSWORD" -> "The password must be at least 6 characters."
                    "ERROR_USER_NOT_FOUND" -> "No account found with this email."
                    "ERROR_WRONG_PASSWORD" -> "Incorrect password."
                    "CONFIGURATION_NOT_FOUND" -> "Firebase Auth is not configured. Please enable Email/Password in Console."
                    else -> e.localizedMessage ?: "Authentication failed."
                }
            }
            is FirebaseException -> e.localizedMessage ?: "Firebase error occurred."
            else -> e.localizedMessage ?: "An unexpected error occurred."
        }
        _authState.value = AuthState.Error(message)
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        auth.signOut()
        _userRole.value = null
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
