package com.nammaskill.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.nammaskill.domain.model.NotificationModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val db: FirebaseDatabase,
    application: Application
) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("notif_prefs", Context.MODE_PRIVATE)
    
    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _hasUnread = MutableStateFlow(false)
    val hasUnread: StateFlow<Boolean> = _hasUnread.asStateFlow()

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.getReference("Notifications").get().await()
                val list = snapshot.children.mapNotNull { it.getValue(NotificationModel::class.java) }
                    .sortedByDescending { it.timestamp }
                _notifications.value = list
                
                checkUnreadStatus(list)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun checkUnreadStatus(list: List<NotificationModel>) {
        if (list.isEmpty()) {
            _hasUnread.value = false
            return
        }
        val lastSeenTimestamp = sharedPrefs.getLong("last_seen_ts", 0L)
        val newestTimestamp = list.first().timestamp
        _hasUnread.value = newestTimestamp > lastSeenTimestamp
    }

    fun markAllAsRead() {
        val newestTimestamp = _notifications.value.firstOrNull()?.timestamp ?: return
        sharedPrefs.edit {
            putLong("last_seen_ts", newestTimestamp)
        }
        _hasUnread.value = false
    }
}
