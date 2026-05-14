package com.nammaskill.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nammaskill.domain.model.Course

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val trade: String,
    val startDate: String,
    val duration: String,
    val eligibility: String,
    val hasJobGuarantee: Boolean
)

fun CourseEntity.toDomain() = Course(
    id = id,
    title = title,
    trade = trade,
    startDate = startDate,
    duration = duration,
    eligibility = eligibility,
    hasJobGuarantee = hasJobGuarantee
)

fun Course.toEntity() = CourseEntity(
    id = id,
    title = title,
    trade = trade,
    startDate = startDate,
    duration = duration,
    eligibility = eligibility,
    hasJobGuarantee = hasJobGuarantee
)
