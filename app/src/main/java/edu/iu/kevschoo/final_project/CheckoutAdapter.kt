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

    class CheckoutViewHolder(val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder
    {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFoodBinding.inflate(layoutInflater, parent, false)
        return CheckoutViewHolder(binding)
    }

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

    override fun getItemCount(): Int = orderItems.size

    fun setOrderItems(newOrderItems: List<Pair<Food, Int>>)
    {
        orderItems = newOrderItems
        notifyDataSetChanged()
    }

    fun getFoodId(position: Int): String { return orderItems[position].first.id }
}
