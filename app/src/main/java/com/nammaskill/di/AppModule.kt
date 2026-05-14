package com.nammaskill.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.nammaskill.data.local.AppDatabase
import com.nammaskill.data.local.CourseDao
import com.nammaskill.data.remote.FirebaseDataSource
import com.nammaskill.data.repository.CourseRepositoryImpl
import com.nammaskill.domain.repository.CourseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.nammaskill.data.remote.GeminiApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return try {
            val db = FirebaseDatabase.getInstance()
            db.reference 
            db
        } catch (e: Exception) {
            FirebaseDatabase.getInstance("https://nammaskill-90298-default-rtdb.firebaseio.com/")
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "namma_skill_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideCourseDao(db: AppDatabase): CourseDao = db.courseDao

    @Provides
    @Singleton
    fun provideGeminiApiService(): GeminiApiService {
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCourseRepository(
        remoteDataSource: FirebaseDataSource,
        localDataSource: CourseDao
    ): CourseRepository {
        return CourseRepositoryImpl(remoteDataSource, localDataSource)
    }
}
