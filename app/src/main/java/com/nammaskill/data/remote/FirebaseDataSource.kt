package com.nammaskill.data.remote

import com.google.firebase.database.FirebaseDatabase
import com.nammaskill.domain.model.Course
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseDataSource @Inject constructor(
    private val db: FirebaseDatabase
) {
    suspend fun getCourses(): List<Course> {
        return try {
            // Migrated from Firestore "courses" collection to RTDB "GlobalCourses" node
            val snapshot = db.getReference("GlobalCourses").get().await()
            snapshot.children.mapNotNull { it.getValue(Course::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
