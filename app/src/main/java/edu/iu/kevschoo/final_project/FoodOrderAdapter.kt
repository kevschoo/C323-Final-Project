package edu.iu.kevschoo.final_project

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.iu.kevschoo.final_project.model.Food
import edu.iu.kevschoo.final_project.model.FoodOrder
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView

class FoodOrderAdapter(
    private val orders: List<FoodOrder>,
    private val allFoods: List<Food>,
    private val getRestaurantName: (String) -> String?,
    private val onReorderClicked: (FoodOrder) -> Unit,
    private val onTrackClicked: (FoodOrder) -> Unit
) : RecyclerView.Adapter<FoodOrderAdapter.ViewHolder>() {
    var showReorderButton: Boolean = true
    var showTrackButton: Boolean = true

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        val tvStoreName: TextView = view.findViewById(R.id.tvStoreName)
        val tvFoodList: TextView = view.findViewById(R.id.tvFoodList)
        val tvTotalCost: TextView = view.findViewById(R.id.tvTotalCost)
        val tvOrderDateTime: TextView = view.findViewById(R.id.tvOrderDateTime)
        val btnReorder: Button = view.findViewById(R.id.reorder)
        val btnTrack: Button = view.findViewById(R.id.btnTrack)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_foodorder, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val order = orders[position]
        val restaurantName = getRestaurantName(order.restaurantID) ?: "Unknown Restaurant"
        holder.tvStoreName.text = restaurantName

        val foodItemsStringBuilder = StringBuilder()
        order.foodID.forEachIndexed { index, foodId ->
            val food = allFoods.find { it.id == foodId }
            val quantity = order.foodAmount.getOrElse(index) { 0 }
            val foodName = food?.name ?: "Unnamed Food"
            val foodPrice = food?.cost ?: 0f
            foodItemsStringBuilder.append("$foodName - Quantity: $quantity - Price: ${quantity * foodPrice}\n")
        }

        holder.tvFoodList.text = foodItemsStringBuilder.toString()
        holder.tvTotalCost.text = "Total Cost: $${order.cost}"
        holder.tvOrderDateTime.text = "Date, Time: ${order.orderDate}"
        holder.btnReorder.setOnClickListener { onReorderClicked(order) }
        holder.btnTrack.setOnClickListener { onTrackClicked(order) }
        holder.btnReorder.visibility = if (showReorderButton) View.VISIBLE else View.GONE
        holder.btnTrack.visibility = if (showTrackButton) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = orders.size
}
