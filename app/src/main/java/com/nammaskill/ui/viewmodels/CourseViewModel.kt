package com.nammaskill.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.nammaskill.data.util.Resource
import com.nammaskill.domain.model.Course
import com.nammaskill.domain.model.CourseModel
import com.nammaskill.domain.model.NotificationModel
import com.nammaskill.domain.model.AdminModel
import com.nammaskill.domain.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val repository: CourseRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase
) : ViewModel() {

    // Filter Options
    val trades = listOf("All", "Electrician", "Sewing", "Coding", "Welding")
    val durations = listOf("All", "1 Month", "3 Months", "6 Months", "1 Year")

    // --- Global Courses State (For CourseListScreen) ---
    private val _allGlobalCourses = MutableStateFlow<List<Course>>(emptyList())
    private val _globalTradeFilter = MutableStateFlow("All")
    private val _globalSearchQuery = MutableStateFlow("")
    private val _globalDurationFilter = MutableStateFlow("All")
    private val _isDataFetched = MutableStateFlow(false)

    val selectedTrade = _globalTradeFilter.asStateFlow()
    val searchQuery = _globalSearchQuery.asStateFlow()
    val selectedDuration = _globalDurationFilter.asStateFlow()

    private val _coursesState = MutableStateFlow<Resource<List<Course>>>(Resource.Loading())
    val coursesState: StateFlow<Resource<List<Course>>> = _coursesState

    init {
        // Collect global courses from repository
        viewModelScope.launch {
            repository.getCourses().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _allGlobalCourses.value = result.data ?: emptyList()
                        _isDataFetched.value = true
                    }
                    is Resource.Error -> {
                        if (!_isDataFetched.value) {
                            _coursesState.value = result
                        }
                    }
                    is Resource.Loading -> {
                        if (!_isDataFetched.value) {
                            _coursesState.value = Resource.Loading()
                        }
                    }
                }
            }
        }

        // Reactive filtering for global courses
        combine(
            _allGlobalCourses,
            _globalTradeFilter,
            _globalSearchQuery,
            _globalDurationFilter,
            _isDataFetched
        ) { courses, trade, query, duration, fetched ->
            if (!fetched) return@combine null

            val q = query.trim().lowercase()
            courses.filter { course ->
                val matchesSearch = q.isEmpty() || 
                                  course.title.lowercase().contains(q) || 
                                  course.trade.lowercase().contains(q)
                
                val matchesTrade = trade == "All" || course.trade.trim().equals(trade, ignoreCase = true)
                val matchesDuration = isDurationMatch(course.duration, duration)
                
                matchesSearch && matchesTrade && matchesDuration
            }
        }.filterNotNull().onEach { filteredList ->
            _coursesState.value = Resource.Success(filteredList)
        }.launchIn(viewModelScope)
    }

    fun onTradeSelected(trade: String) { _globalTradeFilter.value = trade }
    fun onSearchQueryChanged(query: String) { _globalSearchQuery.value = query }
    fun onDurationSelected(duration: String) { _globalDurationFilter.value = duration }

    // --- Skill Center Courses State (For UserCenterScreens) ---
    private val _centerCourses = MutableStateFlow<List<CourseModel>>(emptyList())
    private val _centerSearchQuery = MutableStateFlow("")
    private val _centerDurationFilter = MutableStateFlow("All")

    val centerSearchQuery = _centerSearchQuery.asStateFlow()
    val centerDurationFilter = _centerDurationFilter.asStateFlow()

    val filteredCenterCourses = combine(
        _centerCourses,
        _centerSearchQuery,
        _centerDurationFilter
    ) { courses, query, duration ->
        val q = query.trim().lowercase()
        courses.filter { course ->
            val matchesSearch = q.isEmpty() || 
                              course.courseName.lowercase().contains(q) ||
                              course.courseDescription.lowercase().contains(q)
            
            val matchesDuration = isDurationMatch(course.duration, duration)
            matchesSearch && matchesDuration
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _adminCourses = MutableStateFlow<List<CourseModel>>(emptyList())
    val adminCourses: StateFlow<List<CourseModel>> = _adminCourses

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private fun isDurationMatch(courseDuration: String?, filterDuration: String): Boolean {
        if (filterDuration == "All" || filterDuration.isBlank()) return true
        if (courseDuration.isNullOrBlank()) return false
        
        val c = courseDuration.lowercase().trim()
        val f = filterDuration.lowercase().trim()
        
        fun normalize(s: String) = s.replace(" ", "").removeSuffix("s")
        if (normalize(c) == normalize(f)) return true

        val cNum = Regex("\\d+").find(c)?.value
        val fNum = Regex("\\d+").find(f)?.value
        
        if (cNum != null && fNum != null) {
            if (cNum == fNum) {
                val cIsYear = c.contains("year") || c.contains("yr")
                val fIsYear = f.contains("year") || f.contains("yr")
                return cIsYear == fIsYear
            }
            return false 
        }
        
        return c.contains(f) || f.contains(c)
    }

    // --- Actions ---
    fun fetchAdminCourses() {
        val adminId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.getReference("Courses").child(adminId).get().await()
                val courses = snapshot.children.mapNotNull { it.getValue(CourseModel::class.java) }
                _adminCourses.value = courses
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchCoursesForCenter(adminId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.getReference("Courses").child(adminId).get().await()
                val courses = snapshot.children.mapNotNull { it.getValue(CourseModel::class.java) }
                _centerCourses.value = courses
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onCenterSearchChanged(query: String) { _centerSearchQuery.value = query }
    fun onCenterDurationFilterChanged(duration: String) { _centerDurationFilter.value = duration }

    fun addCourse(
        name: String, 
        desc: String, 
        duration: String, 
        hasJobGuarantee: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        val adminId = auth.currentUser?.uid ?: return
        val courseId = UUID.randomUUID().toString()
        val course = CourseModel(
            courseId = courseId, 
            courseName = name, 
            courseDescription = desc, 
            adminId = adminId, 
            duration = duration, 
            createdAt = System.currentTimeMillis(),
            hasJobGuarantee = hasJobGuarantee
        )

        viewModelScope.launch {
            try {
                // Save Course
                db.getReference("Courses").child(adminId).child(courseId).setValue(course).await()
                
                // Get Admin Name for notification
                val adminSnapshot = db.getReference("Admins").child(adminId).get().await()
                val admin = adminSnapshot.getValue(AdminModel::class.java)
                val centerName = admin?.skillCenterName ?: "A Skill Center"

                // Create Notification
                val notifId = UUID.randomUUID().toString()
                val notification = NotificationModel(
                    id = notifId,
                    title = "New Course Available!",
                    message = "Course '$name' was just added at $centerName.",
                    type = "course"
                )
                db.getReference("Notifications").child(notifId).setValue(notification).await()

                onResult(true, "Course added successfully")
                fetchAdminCourses() 
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to add course")
            }
        }
    }

    fun deleteCourse(courseId: String, onResult: (Boolean, String) -> Unit) {
        val adminId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.getReference("Courses").child(adminId).child(courseId).removeValue().await()
                onResult(true, "Course deleted successfully")
                fetchAdminCourses()
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to delete course")
            }
        }
    }
}
