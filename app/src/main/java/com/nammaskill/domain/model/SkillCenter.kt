package com.nammaskill.domain.model

data class SkillCenter(
    val id: String,
    val name: String,
    val address: String,
    val district: String,
    val trades: List<String>,
    val latitude: Double,
    val longitude: Double
)
