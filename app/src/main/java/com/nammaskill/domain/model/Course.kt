package com.nammaskill.domain.model

data class Course(
    val id: String = "",
    val title: String = "",
    val trade: String = "",
    val startDate: String = "",
    val duration: String = "",
    val eligibility: String = "",
    val hasJobGuarantee: Boolean = false
)

//annotation class Course
