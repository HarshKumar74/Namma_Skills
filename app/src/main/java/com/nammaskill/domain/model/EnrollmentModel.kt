package com.nammaskill.domain.model

data class EnrollmentModel(
    val userId: String = "",
    val userName: String = "",
    val age: String = "",
    val gender: String = "",
    val email: String = "",
    val phone: String = "",
    val aadhaar: String = "",
    val enrolledCourse: String = "",
    val skillCenterName: String = "",
    val adminId: String = ""
)
