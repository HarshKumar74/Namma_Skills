package com.nammaskill.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.nammaskill.domain.model.AdminModel
import com.nammaskill.domain.model.SuccessStoryModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SuccessStoryViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _successStories = MutableStateFlow<List<SuccessStoryModel>>(emptyList())
    val successStories: StateFlow<List<SuccessStoryModel>> = _successStories

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchAllSuccessStories()
    }

    fun fetchAllSuccessStories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.getReference("SuccessStories").get().await()
                val stories = mutableListOf<SuccessStoryModel>()
                snapshot.children.forEach { adminNode ->
                    adminNode.children.forEach { storyNode ->
                        storyNode.getValue(SuccessStoryModel::class.java)?.let { stories.add(it) }
                    }
                }
                _successStories.value = stories.sortedByDescending { it.id } // Sort by id or timestamp if available
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addSuccessStory(
        name: String, 
        story: String, 
        imageUris: List<Uri>, 
        onResult: (Boolean, String) -> Unit
    ) {
        val adminId = auth.currentUser?.uid ?: return
        val storyId = UUID.randomUUID().toString()
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch Admin/Center Name
                val adminSnapshot = db.getReference("Admins").child(adminId).get().await()
                val admin = adminSnapshot.getValue(AdminModel::class.java)
                val centerName = admin?.skillCenterName ?: "Namma Skill Center"

                val imageUrls = mutableListOf<String>()
                
                // Upload images to Firebase Storage
                for (uri in imageUris) {
                    val fileName = "success_stories/${adminId}/${UUID.randomUUID()}"
                    val ref = storage.getReference(fileName)
                    ref.putFile(uri).await()
                    val downloadUrl = ref.downloadUrl.await().toString()
                    imageUrls.add(downloadUrl)
                }

                val successStory = SuccessStoryModel(
                    id = storyId,
                    name = name,
                    story = story,
                    images = imageUrls,
                    adminId = adminId,
                    centerName = centerName
                )
                db.getReference("SuccessStories").child(adminId).child(storyId).setValue(successStory).await()
                
                onResult(true, "Success story added successfully")
                fetchAllSuccessStories()
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to add success story")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSuccessStory(adminId: String, storyId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                db.getReference("SuccessStories").child(adminId).child(storyId).removeValue().await()
                onResult(true, "Success story deleted successfully")
                fetchAllSuccessStories()
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to delete success story")
            }
        }
    }
}
