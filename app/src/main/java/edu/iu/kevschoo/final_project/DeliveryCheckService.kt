package edu.iu.kevschoo.final_project

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import edu.iu.kevschoo.final_project.model.FoodOrder
import java.util.Date

class DeliveryCheckService : Service() {

    private val notificationChannelId = "delivery_service_channel"

    override fun onBind(intent: Intent): IBinder? { return null }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Thread { checkDeliveryStatus() }.start()
        return START_STICKY
    }

    private fun checkDeliveryStatus()
    {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        while (true)
        {
            val orderId = sharedPreferences.getString("currentOrderId", null)
            val isDelivered = sharedPreferences.getBoolean("isOrderDelivered", false)
            val deliveryTime = sharedPreferences.getLong("deliveryTime", -1)

            if (orderId != null && isDelivered && deliveryTime <= System.currentTimeMillis())
            { sendNotification(orderId) }

            try { Thread.sleep(60000) }
            catch (e: InterruptedException) { break }
        }
    }

    private fun sendNotification(orderId: String)
    {
        try
        {
            val builder = NotificationCompat.Builder(this, notificationChannelId)
                .setContentTitle("Order Delivered")
                .setContentText("Your order ${orderId} has been delivered.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)) {
                if (ContextCompat.checkSelfPermission(this@DeliveryCheckService, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
                { notify(orderId.hashCode(), builder.build()) }
                else { }
            }
        }
        catch (e: SecurityException) { Log.e("DeliveryCheckService", "SecurityException in sending notification", e) }
    }
}