package com.nammaskill.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammaskill.domain.model.Course
import com.nammaskill.domain.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.nammaskill.data.util.Resource

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val repository: CourseRepository
) : ViewModel() {

    private val _course = MutableStateFlow<Resource<Course>>(Resource.Loading())
    val course: StateFlow<Resource<Course>> = _course

    fun loadCourse(courseId: String) {
        viewModelScope.launch {
            repository.getCourses().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val foundCourse = resource.data?.find { it.id == courseId }
                        if (foundCourse != null) {
                            _course.value = Resource.Success(foundCourse)
                        } else {
                            _course.value = Resource.Error("Course not found")
                        }
                    }
                    is Resource.Error -> _course.value = Resource.Error(resource.message ?: "Error")
                    is Resource.Loading -> _course.value = Resource.Loading()
                }
            }
        }
    }

    fun applyForCourse(name: String, phone: String) {
        // Simulate application submission
        viewModelScope.launch {
            // In a real app, this would be an API call
            // FR-NS-11: Simulate creation of Candidate Summary
        }
    }
}
