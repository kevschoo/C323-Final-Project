package edu.iu.kevschoo.final_project.model

import java.util.Date

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val signUpDate: Date = Date(),
    val profilePictureURL: String = "",
    val orderHistory: List<String> = listOf(),
)