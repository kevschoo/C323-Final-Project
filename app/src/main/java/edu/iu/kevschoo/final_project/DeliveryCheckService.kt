package edu.iu.kevschoo.final_project

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import edu.iu.kevschoo.final_project.model.FoodOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeliveryCheckService : Service() {

    private val notificationChannelId = "delivery_service_channel"
    companion object {
        const val ACTION_SEND_TEST_NOTIFICATION = "SEND_TEST_NOTIFICATION"
    }
    /**
     * Returns null as this is a started service, not a bound service
     * @param intent The Intent used for service binding
     * @return Returns null as binding is not supported
     */
    override fun onBind(intent: Intent): IBinder? { return null }

    /**
     * Sends a test notification to the user
     */
    private fun sendTestNotification()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled())
            {
                Log.e("DeliveryCheckService", "Notifications are disabled")
                return
            }
        }

        createNotificationChannel()
        val builder = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Test Notification")
            .setContentText("This is a test notification.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) { notify(0, builder.build()) }
    }

    /**
     * Handles commands to start the service, either to send a test notification or handle order notifications
     * @param intent The Intent supplied to startService(Intent), as given
     * @param flags Additional data about this start request
     * @param startId A unique integer representing this specific request to start
     * @return The return value indicates what semantics the system should use for the service's current started state
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        intent?.let {
            when (it.action)
            {
                ACTION_SEND_TEST_NOTIFICATION -> sendTestNotification()
                else -> handleOrderNotifications(it)
            }
        }
        return START_STICKY
    }

    /**
     * Handles notifications related to order updates
     * @param intent Intent containing order details and actions
     */
    private fun handleOrderNotifications(intent: Intent)
    {
        val storageService = FirebaseStorageService()
        val orderId = intent.getStringExtra("ORDER_ID")
        val estimatedDeliveryTimeMillis = intent.getLongExtra("ESTIMATED_DELIVERY_TIME", -1)

        if (orderId != null && estimatedDeliveryTimeMillis != -1L)
        {
            val estimatedDeliveryTime = Date(estimatedDeliveryTimeMillis)
            sendOrderPlacedNotification(orderId, estimatedDeliveryTime)
            scheduleDeliveryNotification(orderId, estimatedDeliveryTimeMillis)
        }
        else
        { checkForDeliveredOrders(storageService) }
    }

    /**
     * Schedules a notification for when an order is estimated to be delivered
     * @param orderId ID of the order
     * @param deliveryTimeMillis Estimated delivery time in milliseconds
     */
    private fun scheduleDeliveryNotification(orderId: String, deliveryTimeMillis: Long)
    {
        val delayMillis = deliveryTimeMillis - System.currentTimeMillis()
        if (delayMillis > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(delayMillis)
                sendNotification("Order Delivered", "Your order has been delivered.")
            }
        }
    }

    /**
     * Checks for orders that have been delivered and sends notifications accordingly
     * @param storageService Instance of FirebaseStorageService to fetch user orders
     */
    private fun checkForDeliveredOrders(storageService: FirebaseStorageService)
    {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            CoroutineScope(Dispatchers.IO).launch {
                storageService.fetchUserOrders(it).collect { orders ->
                    orders.forEach { order ->
                        if (!order.isDelivered && order.travelTime <= Date())
                        {
                            sendNotification("Orders Delivered", "Your orders have been delivered.")
                            storageService.updateOrderDeliveryStatus(order.id)
                        }
                    }
                }
            }
        }
    }

    /**
     * Sends a notification with specified title and content
     * @param title Title of the notification
     * @param content Content text of the notification
     */
    private fun sendNotification(title: String, content: String)
    {
        try
        {
            val builder = NotificationCompat.Builder(this, notificationChannelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)) { notify(System.currentTimeMillis().toInt(), builder.build()) }
        }
        catch (e: SecurityException) { Log.e("DeliveryCheckService", "SecurityException in sending notification", e) }
    }

    /**
     * Sends a notification when an order is placed, including the estimated delivery time
     * @param orderId ID of the order that was placed
     * @param estimatedDeliveryTime Estimated delivery time of the order
     */
    fun sendOrderPlacedNotification(orderId: String, estimatedDeliveryTime: Date)
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("DeliveryCheckService", "SecurityException in sending notification")
            return
        }
        createNotificationChannel()

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("Order Placed Successfully")
            .setContentText("Your order #$orderId has been placed. Estimated delivery: ${formatDate(estimatedDeliveryTime)}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) { notify(orderId.hashCode(), notificationBuilder.build()) }
    }

    /**
     * Formats a Date object into a human-readable string
     * @param date The Date object to format
     * @return A formatted date string
     */
    private fun formatDate(date: Date): String
    {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Creates a notification channel for order updates if required by the Android version
     */
    private fun createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val channelName = "Order Updates"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(notificationChannelId, channelName, importance)
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}