package com.nammaskill.domain.repository

import com.nammaskill.data.util.Resource
import com.nammaskill.domain.model.Course
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    fun getCourses(): Flow<Resource<List<Course>>>
}
