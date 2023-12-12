package edu.iu.kevschoo.final_project

import android.net.Uri
import edu.iu.kevschoo.final_project.model.Food
import edu.iu.kevschoo.final_project.model.FoodOrder
import edu.iu.kevschoo.final_project.model.Restaurant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Date

interface StorageService {

    /** Retrieves a flow of food orders made by a specific user */
    fun fetchUserOrders(userId: String): Flow<List<FoodOrder>>

    /** Uploads a food order for a specific user */
    fun uploadUserFoodOrder(userId: String, foodOrder: FoodOrder)

    /** Uploads a user's profile picture and executes a callback upon completion */
    fun uploadUserProfilePicture(userId: String, imageUri: Uri, onComplete: (Uri?) -> Unit)

    /** Retrieves a flow of the URL of a user's profile picture */
    fun fetchUserProfilePicture(userId: String): Flow<String>

    /** Retrieves a flow of available restaurants */
    fun fetchRestaurants(): Flow<List<Restaurant>>

    /** Retrieves a flow of food orders made by a user within a specified date range */
    fun fetchUserOrdersForDate(userId: String, startDate: Date, endDate: Date): Flow<List<FoodOrder>>

    /** Retrieves a flow of foods offered by a specific restaurant */
    fun fetchRestaurantFoods(restaurantID: String): Flow<List<Food>>

    /** Retrieves a flow of all available foods */
    fun fetchAllFood(): Flow<List<Food>>

    /** Retrieves a flow of information about a specific food item */
    fun fetchFood(foodID: String): Flow<Food?>

    /** Retrieves a specific food order by its ID, or null if not found */
    fun fetchOrderById(orderId: String): FoodOrder?

    /** Creates a new food item and executes a callback upon completion */
    fun createFood(food: Food, onComplete: (String?) -> Unit)

    /** Adds a new restaurant to the system */
    fun createRestaurant(restaurant: Restaurant)

    /** Retrieves a flow of information about a specific restaurant by its ID */
    fun fetchRestaurantById(restaurantId: String): Flow<Restaurant?>

    /** Updates the delivery status of a specific order */
    fun updateOrderDeliveryStatus(orderId: String)
}