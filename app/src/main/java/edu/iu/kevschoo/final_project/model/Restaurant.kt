package edu.iu.kevschoo.final_project.model

data class Restaurant(
    val id: String = "",
    val name: String = "",
    val pictureList: List<String> = listOf(),
    val menu: List<String> = listOf(), //id of food
    val address: String = "", //use geocoder to convert to real location coords
)
