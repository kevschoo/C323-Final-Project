package edu.iu.kevschoo.final_project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.iu.kevschoo.final_project.databinding.ItemFoodBinding
import edu.iu.kevschoo.final_project.model.Food

class CheckoutAdapter(
    private val onQuantityChanged: (String, Int) -> Unit,
    private val onItemRemoved: (String) -> Unit
) : RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder>() {
    private var orderItems: List<Pair<Food, Int>> = listOf()

    /**
     * ViewHolder for checkout items, holding the view binding
     */
    class CheckoutViewHolder(val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Creates new ViewHolder instances for RecyclerView items
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds the View for each item
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder
    {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFoodBinding.inflate(layoutInflater, parent, false)
        return CheckoutViewHolder(binding)
    }

    /**
     * Binds the data at the specified position into the ViewHolder
     * @param holder The ViewHolder that should be updated to represent the contents of the item at the given position
     * @param position The position of the item within the adapter's data set
     */
    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int)
    {
        val (food, quantity) = orderItems[position]
        holder.binding.apply {
            tvFoodName.text = food.name
            tvFoodPrice.text = "$${food.cost}"
            etQuantity.setText(quantity.toString())

            btnAdd.setOnClickListener {
                val currentQuantity = etQuantity.text.toString().toIntOrNull() ?: 0
                val newQuantity = currentQuantity + 1
                etQuantity.setText(newQuantity.toString())
                onQuantityChanged(food.id, newQuantity)
            }

            btnRemove.setOnClickListener {
                val currentQuantity = etQuantity.text.toString().toIntOrNull() ?: 0
                val newQuantity = if (currentQuantity > 0) currentQuantity - 1 else 0
                etQuantity.setText(newQuantity.toString())
                if (newQuantity == 0) { onItemRemoved(food.id) }
                else { onQuantityChanged(food.id, newQuantity) }
            }
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter
     * @return The total number of items in this adapter
     */
    override fun getItemCount(): Int = orderItems.size

    /**
     * Updates the list of order items and notifies the adapter to refresh the view
     * @param newOrderItems The new list of order items to display
     */
    fun setOrderItems(newOrderItems: List<Pair<Food, Int>>)
    {
        orderItems = newOrderItems
        notifyDataSetChanged()
    }

    /**
     * Retrieves the ID of the food item at the specified position
     * @param position The position of the item in the list
     * @return The ID of the food item
     */
    fun getFoodId(position: Int): String { return orderItems[position].first.id }
}
