import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.iu.kevschoo.final_project.databinding.ItemFoodBinding
import edu.iu.kevschoo.final_project.model.Food

class FoodAdapter(
    private var foodItems: List<Food>,
    private val currentOrderQuantities: Map<String, Int>,
    private val onQuantityChanged: (String, Int) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {
    /**
     * ViewHolder for food items, holding the view binding.
     */
    class FoodViewHolder(val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Creates new ViewHolder instances for RecyclerView items
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds the View for each item
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder
    {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFoodBinding.inflate(layoutInflater, parent, false)
        return FoodViewHolder(binding)
    }

    /**
     * Binds the data at the specified position into the ViewHolder
     * @param holder The ViewHolder that should be updated to represent the contents of the item at the given position
     * @param position The position of the item within the adapter's data set
     */
    override fun onBindViewHolder(holder: FoodViewHolder, position: Int)
    {
        val foodItem = foodItems[position]
        val quantity = currentOrderQuantities[foodItem.id] ?: 0

        holder.binding.apply {
            tvFoodName.text = foodItem.name
            tvFoodPrice.text = "$${foodItem.cost}"
            etQuantity.setText(quantity.toString())

            btnAdd.setOnClickListener {
                val currentQuantity = etQuantity.text.toString().toIntOrNull() ?: 0
                etQuantity.setText((currentQuantity + 1).toString())
                onQuantityChanged(foodItem.id, currentQuantity + 1)
            }

            btnRemove.setOnClickListener {
                val currentQuantity = etQuantity.text.toString().toIntOrNull() ?: 0
                if (currentQuantity > 0)
                {
                    etQuantity.setText((currentQuantity - 1).toString())
                    onQuantityChanged(foodItem.id, currentQuantity - 1)
                }
            }

            etQuantity.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus)
                {
                    val quantity = etQuantity.text.toString().toIntOrNull() ?: 0
                    onQuantityChanged(foodItem.id, quantity)
                }
            }
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter
     * @return The total number of items in this adapter
     */
    override fun getItemCount(): Int = foodItems.size

    /**
     * Updates the list of food items and notifies the adapter to refresh the view
     * @param newFoodItems The new list of food items to display
     */
    fun updateFoodItems(newFoodItems: List<Food>)
    {
        foodItems = newFoodItems
        Log.d("FoodAdapter", "Number of food items loaded: ${foodItems.size}")
        notifyDataSetChanged()
    }
}