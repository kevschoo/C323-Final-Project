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

    /**
     * ViewHolder for food order items, holding the view elements.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        val tvStoreName: TextView = view.findViewById(R.id.tvStoreName)
        val tvFoodList: TextView = view.findViewById(R.id.tvFoodList)
        val tvTotalCost: TextView = view.findViewById(R.id.tvTotalCost)
        val tvOrderDateTime: TextView = view.findViewById(R.id.tvOrderDateTime)
        val btnReorder: Button = view.findViewById(R.id.reorder)
        val btnTrack: Button = view.findViewById(R.id.btnTrack)
        val tvTransitTime: TextView = view.findViewById(R.id.tvTransitTime)
        val tvInstructions: TextView = view.findViewById(R.id.tvInstructions)
        val tvDestination: TextView = view.findViewById(R.id.tvDestination)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    /**
     * Creates new ViewHolder instances for RecyclerView items
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds the View for each item
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_foodorder, parent, false)
        return ViewHolder(view)
    }

    /**
     * Binds the data at the specified position into the ViewHolder
     * @param holder The ViewHolder that should be updated to represent the contents of the item at the given position
     * @param position The position of the item within the adapter's data set
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val order = orders[position]
        val restaurantName = getRestaurantName(order.restaurantID) ?: "Unknown Restaurant"
        holder.tvStoreName.text = restaurantName
        holder.tvTransitTime.text = "Remaining Delivery Time: ${order.getRemainingDeliveryTime()}"
        holder.tvInstructions.text = "Instructions: ${order.specialInstructions}"
        holder.tvDestination.text = "Destination: ${order.addressName}"
        holder.tvStatus.text = "Delivered: ${order.isDelivered}"
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
        holder.tvOrderDateTime.text = "Order Date: ${order.orderDate}"
        holder.btnReorder.setOnClickListener { onReorderClicked(order) }
        holder.btnTrack.setOnClickListener { onTrackClicked(order) }
        holder.btnReorder.visibility = if (showReorderButton) View.VISIBLE else View.GONE
        holder.btnTrack.visibility = if (showTrackButton) View.VISIBLE else View.GONE
    }

    /**
     * Returns the total number of items in the data set held by the adapter
     * @return The total number of items in this adapter
     */
    override fun getItemCount() = orders.size
}
