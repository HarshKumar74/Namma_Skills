package com.nammaskill.domain.model

data class SuccessStoryModel(
    val id: String = "",
    val name: String = "",
    val story: String = "",
    val images: List<String> = emptyList(),
    val adminId: String = "",
    val centerName: String = ""
)
