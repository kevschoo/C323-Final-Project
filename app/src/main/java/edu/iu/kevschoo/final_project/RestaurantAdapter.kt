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
    /**
     * ViewHolder for restaurant items, holding the view binding
     */
    class RestaurantViewHolder(val binding: ItemRestaurantBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Creates new ViewHolder instances for RecyclerView items
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds the View for each item
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder
    {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemRestaurantBinding.inflate(layoutInflater, parent, false)
        return RestaurantViewHolder(binding)
    }

    /**
     * Binds the data at the specified position into the ViewHolder
     * @param holder The ViewHolder that should be updated to represent the contents of the item at the given position
     * @param position The position of the item within the adapter's data set
     */
    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int)
    {
        val restaurant = restaurantList[position]
        holder.binding.tvRestaurantName.text = restaurant.name
        holder.itemView.setOnClickListener { onRestaurantSelected(restaurant) }
    }

    /**
     * Returns the total number of items in the data set held by the adapter
     * @return The total number of items in this adapter
     */
    override fun getItemCount(): Int = restaurantList.size

    /**
     * Updates the list of restaurants and notifies the adapter to refresh the view
     * @param newList The new list of restaurants to display
     */
    fun updateList(newList: List<Restaurant>)
    {
        restaurantList = newList
        notifyDataSetChanged()
    }

}