package edu.iu.kevschoo.final_project

import android.app.Application
import android.location.Geocoder
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.launch
import edu.iu.kevschoo.final_project.model.Food
import edu.iu.kevschoo.final_project.model.FoodOrder
import edu.iu.kevschoo.final_project.model.Restaurant
import java.io.IOException
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SharedViewModel(application: Application) : AndroidViewModel(application)
{

    private val accountService: AccountService = FirebaseAccountService()
    private val storageService: StorageService = FirebaseStorageService()

    private val _authenticationState = MutableLiveData<AuthenticationState>()
    val authenticationState: LiveData<AuthenticationState>
        get() = _authenticationState

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = _userEmail

    private val _userProfilePictureUrl = MutableLiveData<String>()
    val userProfilePictureUrl: LiveData<String> = _userProfilePictureUrl

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: MutableLiveData<Uri?> = _selectedImageUri

    private val _allRestaurants = MutableLiveData<List<Restaurant>>(emptyList())
    val allRestaurants: LiveData<List<Restaurant>> = _allRestaurants

    private val _userRecentOrders = MutableLiveData<List<FoodOrder>>(emptyList())
    val userRecentOrders: LiveData<List<FoodOrder>> = _userRecentOrders

    private val _recentRestaurants = MutableLiveData<List<Restaurant>>(emptyList())
    val recentRestaurants: LiveData<List<Restaurant>> = _recentRestaurants

    private val _filteredRestaurants = MutableLiveData<List<Restaurant>>(emptyList())
    val filteredRestaurants: LiveData<List<Restaurant>> = _filteredRestaurants

    private val _selectedRestaurant = MutableLiveData<Restaurant?>()
    val selectedRestaurant: LiveData<Restaurant?> = _selectedRestaurant

    private val _currentRestaurantFood = MutableLiveData<List<Food>>()
    val currentRestaurantFood: LiveData<List<Food>> = _currentRestaurantFood

    private val _currentFoodOrder = MutableLiveData<FoodOrder?>()
    val currentFoodOrder: LiveData<FoodOrder?> = _currentFoodOrder

    private val _allFoods = MutableLiveData<List<Food>>(emptyList())
    val allFoods: LiveData<List<Food>> = _allFoods

    private val _allUserOrders = MutableLiveData<List<FoodOrder>>()
    val allUserOrders: LiveData<List<FoodOrder>> = _allUserOrders

    private val _totalSpentOnSelectedDate = MutableLiveData<Float>()
    val totalSpentOnSelectedDate: LiveData<Float> = _totalSpentOnSelectedDate

    private val _filteredOrders = MutableLiveData<List<FoodOrder>>()
    val filteredOrders: LiveData<List<FoodOrder>> = _filteredOrders

    private val _weeklySpendingData = MutableLiveData<List<Float>>(List(7) { 0f })
    val weeklySpendingData: LiveData<List<Float>> = _weeklySpendingData

    private val _selectedDate = MutableLiveData<Date>()
    val selectedDate: LiveData<Date> = _selectedDate

    private val _selectedDateOrderCount = MutableLiveData<Int>()
    val selectedDateOrderCount: LiveData<Int> = _selectedDateOrderCount

    private val _selectedDateTotalCost = MutableLiveData<Float>()
    val selectedDateTotalCost: LiveData<Float> = _selectedDateTotalCost

    init
    {
        viewModelScope.launch {
            accountService.currentUser.collect { user ->
                if (user != null)
                {
                    _authenticationState.value = AuthenticationState.AUTHENTICATED
                    user.id?.let { userId -> viewModelScope.launch {} }
                }
                else { _authenticationState.value = AuthenticationState.UNAUTHENTICATED }
            }
        }
        fetchAllRestaurants()
        fetchUserRecentOrders()
        fetchAllFoods()
        fetchAllUserOrders()
    }

    fun getUserOrderDates(): LiveData<List<Date>>
    {
        val orderDatesLiveData = MutableLiveData<List<Date>>()
        viewModelScope.launch {
            val userOrders = _allUserOrders.value.orEmpty().filter { it.userID == FirebaseAuth.getInstance().currentUser?.uid }
            val orderDates = userOrders.map { it.orderDate }.distinct()
            orderDatesLiveData.postValue(orderDates)
        }
        return orderDatesLiveData
    }


    fun updateWeeklySpendingData()
    {
        viewModelScope.launch {
            val spending = MutableList(7) { 0f }
            var fetchCount = 0

            for (i in 6 downTo 0)
            {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                val date = cal.time
                val index = 6 - i

                getTotalSpentOnDate(date).observeForever { daySpending ->
                    spending[index] = daySpending
                    fetchCount++

                    if (fetchCount == 7) { _weeklySpendingData.postValue(spending) }
                }
            }
        }
    }

    fun filterOrdersByRestaurantName(query: String?)
    {
        val filteredList = if (!query.isNullOrEmpty())
        {
            _allUserOrders.value?.filter { order ->
                val restaurantName = _allRestaurants.value?.find { it.id == order.restaurantID }?.name ?: ""
                restaurantName.contains(query, ignoreCase = true)
            }
        }
        else { _allUserOrders.value }
        _filteredOrders.postValue(filteredList.orEmpty())
    }

    fun selectRestaurant(restaurant: Restaurant)
    {
        _selectedRestaurant.value = restaurant
        _currentFoodOrder.value = FoodOrder(restaurantID = restaurant.id)
    }

    private fun fetchAllFoods() { viewModelScope.launch { storageService.fetchAllFood().collect { foods -> _allFoods.postValue(foods) } } }

    fun fetchCurrentRestaurantFood(restaurantId: String)
    {
        viewModelScope.launch {
            val menuItems = _allRestaurants.value?.find { it.id == restaurantId }?.menu.orEmpty()
            val filteredFoods = _allFoods.value?.filter { it.id in menuItems }.orEmpty()

            _currentRestaurantFood.postValue(filteredFoods)
            Log.d("SharedViewModel", "Fetched ${filteredFoods.size} food items for restaurant $restaurantId")
        }
    }

    fun updateFoodOrder(foodId: String, quantity: Int)
    {
        val currentOrder = _currentFoodOrder.value ?: FoodOrder(restaurantID = _selectedRestaurant.value?.id ?: "")
        val newFoodIds = currentOrder.foodID.toMutableList()
        val newFoodAmounts = currentOrder.foodAmount.toMutableList()

        val index = newFoodIds.indexOf(foodId)
        if (index != -1)
        {
            if (quantity > 0) { newFoodAmounts[index] = quantity }
            else {
                newFoodIds.removeAt(index)
                newFoodAmounts.removeAt(index)
            }
        }
        else if (quantity > 0)
        {
            newFoodIds.add(foodId)
            newFoodAmounts.add(quantity)
        }

        _currentFoodOrder.value = currentOrder.copy(foodID = newFoodIds, foodAmount = newFoodAmounts)
    }


    fun confirmOrder(destinationCoordinates: Pair<Double, Double>?, onSuccess: () -> Unit, onFailure: (String) -> Unit)
    {
        val currentOrder = _currentFoodOrder.value
        if (currentOrder?.foodID.isNullOrEmpty())
        {
            onFailure("Order is empty.")
            return
        }

        val restaurantAddress = _selectedRestaurant.value?.address ?: ""
        val originCoordinates = getCoordinatesFromAddress(restaurantAddress)

        val totalCost = calculateTotalCost()
        val distance = if (originCoordinates != null && destinationCoordinates != null) { calculateDistance(originCoordinates, destinationCoordinates) } else { 100.0 }

        val deliveryTimeMinutes = (distance / 100) * (5..100).random()
        val estimatedArrivalTime = Calendar.getInstance().apply { add(Calendar.MINUTE, deliveryTimeMinutes.toInt()) }.time

        val updatedOrder = currentOrder?.copy(
            cost = totalCost,
            orderDate = Date(),
            addressOriginList = originCoordinates?.let { listOf("${it.first}", "${it.second}") } ?: listOf(),
            addressDestinationList = destinationCoordinates?.let { listOf("${it.first}", "${it.second}") } ?: listOf(),
            travelTime = estimatedArrivalTime,
            isDelivered = false
        )

        _currentFoodOrder.value = updatedOrder
        submitOrderToFirebase(updatedOrder, onSuccess, onFailure)
    }

    fun getTotalSpentOnDate(date: Date): LiveData<Float>
    {
        val totalSpentLiveData = MutableLiveData<Float>()

        viewModelScope.launch {
            val startOfDay = getStartOfDay(date)
            val endOfDay = getEndOfDay(date)
            storageService.fetchUserOrdersForDate(FirebaseAuth.getInstance().currentUser?.uid ?: "", startOfDay, endOfDay).collect { orders ->
                val totalSpent = if (orders.isEmpty()) 0f else orders.sumCosts()
                totalSpentLiveData.postValue(totalSpent)
            }
        }

        return totalSpentLiveData
    }

    fun List<FoodOrder>.sumCosts(): Float
    {
        var sum = 0f
        for (order in this) { sum += order.cost ?: 0f }
        return sum
    }

    private fun getStartOfDay(date: Date): Date
    {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    private fun getEndOfDay(date: Date): Date
    {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.time
    }


    private fun calculateDistance(origin: Pair<Double, Double>, destination: Pair<Double, Double>): Double
    {
        val earthRadius = 6371.0

        val latDistance = Math.toRadians(destination.first - origin.first)
        val lonDistance = Math.toRadians(destination.second - origin.second)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(origin.first)) * Math.cos(Math.toRadians(destination.first)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        val distance = earthRadius * c

        val height = 0.0

        return Math.sqrt(Math.pow(distance, 2.0) + Math.pow(height, 2.0)) / 1.609
    }

    private fun getCoordinatesFromAddress(address: String): Pair<Double, Double>?
    {
        if (address.isEmpty()) return null

        return try
        {
            val geocoder = Geocoder(getApplication<Application>().applicationContext, Locale.getDefault())
            val addressResults = geocoder.getFromLocationName(address, 1)
            if (addressResults?.isNotEmpty() == true)
            {
                val location = addressResults.first()
                Pair(location.latitude, location.longitude)
            }
            else null
        }
        catch (e: IOException)
        {
            Log.e("SharedViewModel", "Geocoder IO Exception", e)
            null
        }
    }



    private fun submitOrderToFirebase(order: FoodOrder?, onSuccess: () -> Unit, onFailure: (String) -> Unit)
    {
        viewModelScope.launch {
            try
            {
                order?.let {
                    storageService.uploadUserFoodOrder(FirebaseAuth.getInstance().currentUser?.uid ?: "", it)
                    onSuccess()
                } ?: onFailure("Order is null.")
            }
            catch (e: Exception) { onFailure(e.message ?: "Error uploading order.") }
        }
    }


    fun calculateTotalCost(): Float
    {
        val currentOrder = _currentFoodOrder.value ?: return 0f
        val allFoodsMap = _allFoods.value?.associateBy { it.id } ?: return 0f

        var totalCost = 0f
        for (i in currentOrder.foodID.indices)
        {
            val foodId = currentOrder.foodID[i]
            val quantity = currentOrder.foodAmount.getOrElse(i) { 0 }
            val foodCost = allFoodsMap[foodId]?.cost ?: 0f
            totalCost += foodCost * quantity
        }
        return totalCost
    }


    fun removeFoodFromOrder(foodId: String)
    {
        val currentOrder = _currentFoodOrder.value ?: return
        val newFoodIds = currentOrder.foodID.toMutableList()
        val newFoodAmounts = currentOrder.foodAmount.toMutableList()

        val index = newFoodIds.indexOf(foodId)
        if (index != -1)
        {
            newFoodIds.removeAt(index)
            newFoodAmounts.removeAt(index)
        }

        _currentFoodOrder.value = currentOrder.copy(foodID = newFoodIds, foodAmount = newFoodAmounts)
    }



    private fun fetchAllRestaurants()
    {
        viewModelScope.launch { storageService.fetchRestaurants().collect { restaurants -> _allRestaurants.postValue(restaurants) } }
    }


    fun reorderOrder(foodOrder: FoodOrder, onNavigate: () -> Unit)
    {
        _currentFoodOrder.postValue(foodOrder)
        val restaurantId = foodOrder.restaurantID
        _selectedRestaurant.postValue(allRestaurants.value?.find { it.id == restaurantId })
        onNavigate()
    }

    fun trackOrder(foodOrder: FoodOrder, onNavigate: () -> Unit)
    {
        _currentFoodOrder.postValue(foodOrder)
        onNavigate()
    }

    fun reFetchOrders( ){ fetchAllUserOrders() }

    private fun fetchAllUserOrders()
    {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            storageService.fetchUserOrders(userId).collect { orders ->
                Log.d("SharedViewModel", "Fetched all user orders")
                _allUserOrders.postValue(orders)
                filterOrdersForCalendar()
            }
        }
    }

    private fun filterOrdersForCalendar()
    {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        _filteredOrders.postValue(_allUserOrders.value.orEmpty().filter { it.userID == userId })
    }

    private fun fetchUserRecentOrders()
    {
        viewModelScope.launch {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId != null)
            {
                storageService.fetchUserOrders(currentUserId).collect { orders ->
                    Log.d("SharedViewModel", "Total recent user orders fetched: ${orders.size}")
                    _userRecentOrders.postValue(orders)
                    updateRecentRestaurants()
                }
            }
        }
    }

    private fun updateRecentRestaurants()
    {
        val recentOrderIds = _userRecentOrders.value?.map { it.restaurantID }?.distinct().orEmpty()
        val allRestaurants = _allRestaurants.value.orEmpty()

        val matchingRestaurants = allRestaurants.filter { restaurant -> recentOrderIds.contains(restaurant.id) }

        _recentRestaurants.postValue(if (matchingRestaurants.isNotEmpty()) matchingRestaurants else allRestaurants.take(5))
    }

    fun filterRestaurants(query: String?)
    {
        val filteredList = _allRestaurants.value?.filter { it.name.contains(query ?: "", ignoreCase = true) } ?: emptyList()

        _filteredRestaurants.postValue(filteredList)
    }

    //
    // Login Stuff
    //

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    fun signIn(email: String, password: String)
    {
        viewModelScope.launch {
            try
            {
                accountService.signIn(email, password)
            }
            catch (e: FirebaseAuthException)
            {
                _authenticationState.value = AuthenticationState.INVALID_AUTHENTICATION
                _errorMessage.value = handleFirebaseAuthException(e)
            }
            catch (e: Exception)
            {
                _errorMessage.value = "An unexpected error occurred. Please try again later."
                Log.e("SharedViewModel", "Sign In Error: ", e)
            }
        }
    }

    fun selectImageUri(uri: Uri) { _selectedImageUri.value = uri }

    fun signUp(name: String, email: String, password: String)
    {
        viewModelScope.launch {
            try
            {
                accountService.signUp(name, email, password).let {
                    _selectedImageUri.value?.let { uri ->
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@let
                        uploadUserProfilePicture(userId, uri) { uploadedUri -> Log.e("SharedViewModel", "Image Uploaded ") }
                    }
                }
            }
            catch (e: FirebaseAuthException) {
                _authenticationState.value = AuthenticationState.INVALID_AUTHENTICATION
                _errorMessage.value = handleFirebaseAuthException(e)
            }
            catch (e: Exception)
            {
                _errorMessage.value = "An unexpected error occurred. Please try again later."
                Log.e("SharedViewModel", "Sign Up Error: ", e)
            }
        }
    }

    fun uploadUserProfilePicture(userId: String, imageUri: Uri, onComplete: (Uri?) -> Unit)
    {
        viewModelScope.launch {
            storageService.uploadUserProfilePicture(userId, imageUri) { uploadedUri ->
                if (uploadedUri != null)
                { onComplete(uploadedUri) }
                else
                { onComplete(null) }
            }
        }
    }

    private fun handleFirebaseAuthException(e: FirebaseAuthException): String
    {
        return when (e.errorCode)
        {
            "ERROR_INVALID_EMAIL" -> "The email address is badly formatted."
            "ERROR_USER_DISABLED" -> "The user account has been disabled."
            "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD" -> "Invalid email or password."
            else -> "An unknown error occurred. Please try again."
        }
    }

    fun signOut()
    {
        viewModelScope.launch {
            // Reset user-specific LiveData
            _selectedImageUri.value = null
            _selectedRestaurant.value = null
            _currentFoodOrder.value = null
            _allUserOrders.value = emptyList()
            _userRecentOrders.value = emptyList()
            _recentRestaurants.value = emptyList()
            _filteredRestaurants.value = emptyList()
            _selectedDateOrderCount.value = 0
            _selectedDateTotalCost.value = 0f
            _weeklySpendingData.value = List(7) { 0f }

            _userProfilePictureUrl.value = ""

            // Change authentication state and sign out from Firebase
            if (_authenticationState.value != AuthenticationState.UNAUTHENTICATED) {
                _authenticationState.value = AuthenticationState.UNAUTHENTICATED
                accountService.signOut()
            }
        }
    }

    fun fetchUserData()
    {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            accountService.currentUser.collect { user ->
                if (user != null)
                {
                    _userName.value = user.name
                    _userEmail.value = user.email
                    storageService.fetchUserProfilePicture(userId).collect { url -> _userProfilePictureUrl.value = url }
                }
            }
        }
    }

    fun createFood(food: Food, onComplete: (String?) -> Unit) {
        viewModelScope.launch { storageService.createFood(food) { foodId -> onComplete(foodId) } }
    }

    fun createRestaurant(restaurant: Restaurant)
    {
        viewModelScope.launch { storageService.createRestaurant(restaurant) }
    }




    fun updateProfilePicture(imageUri: Uri)
    {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            storageService.uploadUserProfilePicture(userId, imageUri) { uploadedUri ->
                if (uploadedUri != null) {
                    _userProfilePictureUrl.value = uploadedUri.toString()
                    Log.e("SharedViewModel", "Profile Picture Updated")
                }
                else {
                    Log.e("SharedViewModel", "Failed to Update Profile Picture")
                }
            }
        }
    }

}
enum class AuthenticationState
{
    AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
}