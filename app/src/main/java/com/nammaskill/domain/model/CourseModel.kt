package com.nammaskill.domain.model

data class CourseModel(
    val courseId: String = "",
    val courseName: String = "",
    val courseDescription: String = "",
    val adminId: String = "",
    val duration: String = "",
    val startDate: String = "",
    val hasJobGuarantee: Boolean = false,
    val createdAt: Long = 0L
)
