package com.nammaskill.data.repository

import com.nammaskill.data.local.CourseDao
import com.nammaskill.data.local.toDomain
import com.nammaskill.data.local.toEntity
import com.nammaskill.data.remote.FirebaseDataSource
import com.nammaskill.data.util.Resource
import com.nammaskill.domain.model.Course
import com.nammaskill.domain.repository.CourseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val remoteDataSource: FirebaseDataSource,
    private val localDataSource: CourseDao
) : CourseRepository {
    override fun getCourses(): Flow<Resource<List<Course>>> = flow {
        emit(Resource.Loading())
        
        try {
            // First try to load from local cache
            val localCourses = localDataSource.getAllCourses().map { it.toDomain() }
            if (localCourses.isNotEmpty()) {
                emit(Resource.Success(localCourses))
            }

            // Fetch from remote
            val remoteCourses = remoteDataSource.getCourses()
            if (remoteCourses.isNotEmpty()) {
                // Update local cache
                localDataSource.clearAll()
                localDataSource.insertCourses(remoteCourses.map { it.toEntity() })
                
                // Emit fresh data
                emit(Resource.Success(remoteCourses))
            } else if (localCourses.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            val localCourses = try { localDataSource.getAllCourses().map { it.toDomain() } } catch (ex: Exception) { emptyList() }
            if (localCourses.isEmpty()) {
                emit(Resource.Error(e.message ?: "An unknown error occurred"))
            } else {
                emit(Resource.Success(localCourses))
            }
        }
    }.flowOn(Dispatchers.IO)
}
