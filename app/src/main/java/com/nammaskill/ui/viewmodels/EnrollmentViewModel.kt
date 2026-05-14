package com.nammaskill.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.nammaskill.domain.model.AdminModel
import com.nammaskill.domain.model.CourseModel
import com.nammaskill.domain.model.EnrollmentModel
import com.nammaskill.domain.model.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EnrollmentViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase
) : ViewModel() {

    private val _skillCenters = MutableStateFlow<List<AdminModel>>(emptyList())
    val skillCenters: StateFlow<List<AdminModel>> = _skillCenters

    private val _selectedCourseFilter = MutableStateFlow("All")
    val selectedCourseFilter: StateFlow<String> = _selectedCourseFilter

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Map of adminId to list of courses they offer
    private val _centerCoursesMap = MutableStateFlow<Map<String, List<CourseModel>>>(emptyMap())

    val filteredSkillCenters = combine(_skillCenters, _selectedCourseFilter, _searchQuery, _centerCoursesMap) { centers, filter, query, coursesMap ->
        centers.filter { center ->
            val matchesFilter = if (filter == "All") true 
                               else coursesMap[center.adminId]?.any { it.courseName.trim().equals(filter, ignoreCase = true) } ?: false
            
            val matchesQuery = if (query.isEmpty()) true 
                              else center.skillCenterName.contains(query, ignoreCase = true) || 
                                   coursesMap[center.adminId]?.any { it.courseName.contains(query, ignoreCase = true) } ?: false
            
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCoursesForFilter = _centerCoursesMap.map { map ->
        val courses = map.values.flatten()
            .map { it.courseName.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() } // Ensure uniqueness regardless of case
            .sortedBy { it.lowercase() }
        listOf("All") + courses
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    private val _enrolledStudents = MutableStateFlow<List<EnrollmentModel>>(emptyList())
    val enrolledStudents: StateFlow<List<EnrollmentModel>> = _enrolledStudents

    private val _currentAdmin = MutableStateFlow<AdminModel?>(null)
    val currentAdmin: StateFlow<AdminModel?> = _currentAdmin

    private val _selectedStudent = MutableStateFlow<EnrollmentModel?>(null)
    val selectedStudent: StateFlow<EnrollmentModel?> = _selectedStudent

    fun onFilterChanged(filter: String) {
        _selectedCourseFilter.value = filter
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun fetchSkillCenters() {
        viewModelScope.launch {
            try {
                val centers = withContext(Dispatchers.IO) {
                    val snapshot = db.getReference("Admins").get().await()
                    snapshot.children.mapNotNull { it.getValue(AdminModel::class.java) }
                }
                _skillCenters.value = centers
                
                // Also fetch all courses to build the filter map
                fetchCourseMapping()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private suspend fun fetchCourseMapping() {
        withContext(Dispatchers.IO) {
            try {
                val snapshot = db.getReference("Courses").get().await()
                val mapping = mutableMapOf<String, List<CourseModel>>()
                snapshot.children.forEach { adminSnapshot ->
                    val adminId = adminSnapshot.key ?: return@forEach
                    val courses = adminSnapshot.children.mapNotNull { 
                        it.getValue(CourseModel::class.java)
                    }
                    mapping[adminId] = courses
                }
                _centerCoursesMap.value = mapping
            } catch (e: Exception) {
            }
        }
    }

    fun enrollUser(admin: AdminModel, course: CourseModel, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val enrollment = withContext(Dispatchers.IO) {
                    val userSnapshot = db.getReference("Users").child(uid).get().await()
                    val user = userSnapshot.getValue(UserModel::class.java) ?: return@withContext null

                    EnrollmentModel(
                        userId = uid,
                        userName = user.name,
                        age = user.age,
                        gender = user.gender,
                        email = user.email,
                        phone = user.phone,
                        aadhaar = user.aadhaar,
                        enrolledCourse = course.courseName,
                        skillCenterName = admin.skillCenterName,
                        adminId = admin.adminId
                    )
                }

                if (enrollment != null) {
                    withContext(Dispatchers.IO) {
                        db.getReference("Enrollments")
                            .child(admin.adminId)
                            .child(course.courseId)
                            .child(uid)
                            .setValue(enrollment)
                            .await()
                    }
                    onComplete("Successfully Enrolled")
                } else {
                    onComplete("Enrollment Failed: User data not found")
                }
            } catch (e: Exception) {
                onComplete("Enrollment Failed: ${e.message}")
            }
        }
    }

    fun fetchCurrentAdmin() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            val admin = withContext(Dispatchers.IO) {
                val snapshot = db.getReference("Admins").child(uid).get().await()
                snapshot.getValue(AdminModel::class.java)
            }
            _currentAdmin.value = admin
        }
    }

    fun fetchEnrolledStudents(courseId: String) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            val students = withContext(Dispatchers.IO) {
                val snapshot = db.getReference("Enrollments")
                    .child(uid)
                    .child(courseId)
                    .get()
                    .await()
                snapshot.children.mapNotNull { it.getValue(EnrollmentModel::class.java) }
            }
            _enrolledStudents.value = students
        }
    }

    fun setSelectedStudent(student: EnrollmentModel) {
        _selectedStudent.value = student
    }
}
