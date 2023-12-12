package edu.iu.kevschoo.final_project.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.util.Date

data class FoodOrder(
    val id: String = "",

    @PropertyName("userID")val userID: String = "",
    @PropertyName("restaurantID")val restaurantID: String = "",
    @PropertyName("cost")val cost: Float = 0f,
    @PropertyName("orderDate")val orderDate: Date = Date(),
    @PropertyName("delivered")val isDelivered: Boolean = false,
    val travelTime: Date = Date(),
    val foodID: List<String> = listOf(),
    val foodAmount: List<Int> = listOf(),
    val addressOriginList: List<String> = listOf(),
    val addressDestinationList: List<String> = listOf(),
    val specialInstructions: String = "",
    val addressName: String = "",
    ){
    fun getRemainingDeliveryTime(currentTime: Date = Date()): String {
        if (isDelivered) {
            return "Delivered"
        }
        val remainingTime = travelTime.time - currentTime.time
        if (remainingTime <= 0) {
            return "Arriving"
        }
        val hours = remainingTime / (1000 * 60 * 60)
        val minutes = (remainingTime % (1000 * 60 * 60)) / (1000 * 60)
        return String.format("%d hours, %d min", hours, minutes)
    }
}


