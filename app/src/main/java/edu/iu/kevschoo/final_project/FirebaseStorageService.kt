package edu.iu.kevschoo.final_project

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import edu.iu.kevschoo.final_project.model.Food
import edu.iu.kevschoo.final_project.model.FoodOrder
import edu.iu.kevschoo.final_project.model.Restaurant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Calendar
import java.util.Date

class FirebaseStorageService : StorageService
{
    private val storageInstance = Firebase.storage("gs://c323-projects.appspot.com")
    private val storageReference = storageInstance.reference
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val userId: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    /**
     * Retrieves a flow of food orders made by a specific user from Firestore
     * @param userId The ID of the user whose orders are to be fetched
     * @return A flow of a list of FoodOrder objects
     */
    override fun fetchUserOrders(userId: String): Flow<List<FoodOrder>> = callbackFlow {
        val userOrdersRef = firestore.collection("orders").whereEqualTo("userID", userId)
        val subscription = userOrdersRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty) {
                val orders = snapshot.toObjects(FoodOrder::class.java)
                trySend(orders).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    /**
     * Fetches a single food order by its ID from Firestore
     * @param orderId The ID of the order to fetch
     * @return The FoodOrder object if found, otherwise null
     */
    override fun fetchOrderById(orderId: String): FoodOrder?
    {
        if (orderId.isBlank())
        {
            Log.e("FirebaseStorageService", "Order ID is blank")
            return null
        }

        var foodOrder: FoodOrder? = null
        val task = firestore.collection("orders").document(orderId).get()

        try
        {
            val documentSnapshot = task.result
            if (documentSnapshot != null && documentSnapshot.exists())
            { foodOrder = documentSnapshot.toObject(FoodOrder::class.java) }
        }
        catch (e: Exception)
        { Log.e("FirebaseStorageService", "Error fetching order", e) }
        return foodOrder
    }

    /**
     * Uploads a food order to Firestore under a specific user's ID
     * @param userId The ID of the user to associate the order with
     * @param foodOrder The FoodOrder object to upload
     */
    override fun uploadUserFoodOrder(userId: String, foodOrder: FoodOrder)
    {
        val newOrderRef = firestore.collection("orders").document()
        val orderWithId = foodOrder.copy(id = newOrderRef.id, userID = userId)

        newOrderRef.set(orderWithId)
            .addOnSuccessListener {
                Log.d("FirebaseStorageService", "Order uploaded successfully")
                FirebaseFirestore.getInstance().collection("users").document(userId)
                    .update("orderHistory", FieldValue.arrayUnion(newOrderRef.id))
                    .addOnSuccessListener { Log.d("FirebaseStorageService", "Order ID added to user profile") }
                    .addOnFailureListener { e -> Log.e("FirebaseStorageService", "Error updating user profile", e) }
            }
            .addOnFailureListener { e -> Log.e("FirebaseStorageService", "Error uploading order", e) }
    }

    /**
     * Uploads a user's profile picture to Firebase Storage and updates the user's profile in Firestore
     * @param userId The ID of the user
     * @param imageUri The URI of the image to upload
     * @param onComplete A callback function to execute upon completion, passing the Uri of the uploaded image
     */
    override fun uploadUserProfilePicture(userId: String, imageUri: Uri, onComplete: (Uri?) -> Unit)
    {
        val fileRef = storageReference.child("DeliveryAppProfilePictures/$userId/profile_picture")
        val uploadTask = fileRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                firestore.collection("users").document(userId)
                    .update("profilePictureURL", uri.toString())
                    .addOnSuccessListener { onComplete(uri) }
                    .addOnFailureListener { onComplete(null) }
            }
        }.addOnFailureListener { exception ->
            Log.e("FirebaseStorageService", "Image upload failed", exception)
            onComplete(null)
        }
    }

    /**
     * Retrieves a flow of the URL of a user's profile picture from Firestore
     * @param userId The ID of the user
     * @return A flow of the URL as a String
     */
    override fun fetchUserProfilePicture(userId: String): Flow<String> = callbackFlow {
        firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists())
                {
                    val url = snapshot.getString("profilePictureURL") ?: ""
                    trySend(url).isSuccess
                }
            }
        awaitClose()
    }

    /**
     * Retrieves a flow of available restaurants from Firestore
     * @return A flow of a list of Restaurant objects
     */
    override fun fetchRestaurants(): Flow<List<Restaurant>> = callbackFlow {
        val restaurantsRef = firestore.collection("restaurants")
        val subscription = restaurantsRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty) {
                val restaurants = snapshot.toObjects(Restaurant::class.java)
                trySend(restaurants).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    /**
     * Retrieves a flow of foods offered by a specific restaurant from Firestore
     * @param restaurantID The ID of the restaurant
     * @return A flow of a list of Food objects
     */
    override fun fetchRestaurantFoods(restaurantID: String): Flow<List<Food>> = callbackFlow {
        val foodsRef = firestore.collection("restaurants").document(restaurantID).collection("menu")
        val subscription = foodsRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty)
            {
                val foods = snapshot.toObjects(Food::class.java)
                trySend(foods).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    /**
     * Retrieves a flow of food orders made by a user within a specified date range from Firestore
     * @param userId The ID of the user
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return A flow of a list of FoodOrder objects
     */
    override fun fetchUserOrdersForDate(userId: String, startDate: Date, endDate: Date): Flow<List<FoodOrder>> = callbackFlow {
        val startOfDay = Calendar.getInstance().apply {
            time = startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endOfDay = Calendar.getInstance().apply {
            time = endDate
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        Log.d("FirebaseStorageService", "Fetching orders for date range: $startOfDay to $endOfDay")

        val userOrdersRef = firestore.collection("orders")
            .whereEqualTo("userID", userId)
            .whereGreaterThanOrEqualTo("orderDate", startOfDay)
            .whereLessThan("orderDate", endOfDay)

        val subscription = userOrdersRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty)
            {
                val orders = snapshot.toObjects(FoodOrder::class.java)
                Log.d("FirebaseStorageService", "Orders fetched: ${orders.size}")
                trySend(orders).isSuccess
            }
            else
            {
                Log.d("FirebaseStorageService", "No orders found for date range")
                trySend(emptyList<FoodOrder>()).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    /**
     * Retrieves a flow of all available foods from Firestore
     * @return A flow of a list of Food objects
     */
    override fun fetchAllFood(): Flow<List<Food>> = callbackFlow {
        val foodsRef = firestore.collection("foods")
        val subscription = foodsRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty)
            {
                val foods = snapshot.toObjects(Food::class.java)
                trySend(foods).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    /**
     * Retrieves a flow of information about a specific food item from Firestore
     * @param foodID The ID of the food item
     * @return A flow of a Food object or null if not found
     */
    override fun fetchFood(foodID: String): Flow<Food?> = callbackFlow {
        val foodRef = firestore.collection("foods").document(foodID)
        val subscription = foodRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists())
            {
                val food = snapshot.toObject(Food::class.java)
                trySend(food).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    /**
     * Creates a new food item in Firestore and executes a callback upon completion
     * @param food The Food object to create
     * @param onComplete A callback function to execute upon completion, passing the ID of the created food item
     */
    override fun createFood(food: Food, onComplete: (String?) -> Unit)
    {
        val newFoodRef = firestore.collection("foods").document()
        val foodWithId = food.copy(id = newFoodRef.id)

        newFoodRef.set(foodWithId)
            .addOnSuccessListener { onComplete(newFoodRef.id) }
            .addOnFailureListener { e ->
                Log.e("FirebaseStorageService", "Error creating food", e)
                onComplete(null)
            }
    }

    /**
     * Adds a new restaurant to Firestore
     * @param restaurant The Restaurant object to create
     */
    override fun createRestaurant(restaurant: Restaurant)
    {
        val newRestaurantRef = firestore.collection("restaurants").document()
        val restaurantWithId = restaurant.copy(id = newRestaurantRef.id)

        newRestaurantRef.set(restaurantWithId)
            .addOnSuccessListener { Log.d("FirebaseStorageService", "Restaurant created successfully with ID: ${newRestaurantRef.id}") }
            .addOnFailureListener { e -> Log.e("FirebaseStorageService", "Error creating restaurant", e) }
    }

    /**
     * Retrieves a flow of information about a specific restaurant by its ID from Firestore
     * @param restaurantId The ID of the restaurant
     * @return A flow of a Restaurant object or null if not found
     */
    override fun fetchRestaurantById(restaurantId: String): Flow<Restaurant?> = callbackFlow {
        val restaurantRef = firestore.collection("restaurants").document(restaurantId)
        restaurantRef.get().addOnSuccessListener { documentSnapshot ->
            val restaurant = documentSnapshot.toObject(Restaurant::class.java)
            trySend(restaurant).isSuccess
        }
        awaitClose()
    }

    /**
     * Updates the delivery status of a specific order in Firestore
     * @param orderId The ID of the order
     */
    override fun updateOrderDeliveryStatus(orderId: String) {
        val orderRef = firestore.collection("orders").document(orderId)
        orderRef.update("delivered", true)
            .addOnSuccessListener { Log.d("FirebaseStorageService", "Order status updated to delivered for order ID: $orderId") }
            .addOnFailureListener { e -> Log.e("FirebaseStorageService", "Error updating order status", e) }
    }
}
