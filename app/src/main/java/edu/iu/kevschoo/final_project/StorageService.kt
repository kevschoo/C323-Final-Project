package edu.iu.kevschoo.final_project

import android.net.Uri
import edu.iu.kevschoo.final_project.model.Food
import edu.iu.kevschoo.final_project.model.FoodOrder
import edu.iu.kevschoo.final_project.model.Restaurant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Date

interface StorageService {

    fun fetchUserOrders(userId: String): Flow<List<FoodOrder>>

    fun uploadUserFoodOrder(userId: String, foodOrder: FoodOrder)

    fun uploadUserProfilePicture(userId: String, imageUri: Uri, onComplete: (Uri?) -> Unit)

    fun fetchUserProfilePicture(userId: String): Flow<String>

    fun fetchRestaurants(): Flow<List<Restaurant>>

    fun fetchUserOrdersForDate(userId: String, startDate: Date, endDate: Date): Flow<List<FoodOrder>>

    fun fetchRestaurantFoods(restaurantID: String): Flow<List<Food>>

    fun fetchAllFood(): Flow<List<Food>>

    fun fetchFood(foodID: String): Flow<Food?>

    fun fetchOrderById(orderId: String): FoodOrder?

    fun createFood(food: Food, onComplete: (String?) -> Unit)

    fun createRestaurant(restaurant: Restaurant)

    fun fetchRestaurantById(restaurantId: String): Flow<Restaurant?>
}