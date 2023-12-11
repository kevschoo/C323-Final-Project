package edu.iu.kevschoo.final_project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.iu.kevschoo.final_project.databinding.ItemRestaurantBinding
import edu.iu.kevschoo.final_project.model.Restaurant

class RestaurantAdapter(
    private var restaurantList: List<Restaurant>,
    private val onRestaurantSelected: (Restaurant) -> Unit
) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    class RestaurantViewHolder(val binding: ItemRestaurantBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder
    {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemRestaurantBinding.inflate(layoutInflater, parent, false)
        return RestaurantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int)
    {
        val restaurant = restaurantList[position]
        holder.binding.tvRestaurantName.text = restaurant.name
        holder.itemView.setOnClickListener { onRestaurantSelected(restaurant) }
    }

    override fun getItemCount(): Int = restaurantList.size

    fun updateList(newList: List<Restaurant>)
    {
        restaurantList = newList
        notifyDataSetChanged()
    }

}