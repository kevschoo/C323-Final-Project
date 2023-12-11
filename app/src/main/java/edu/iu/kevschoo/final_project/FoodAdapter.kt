import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.iu.kevschoo.final_project.databinding.ItemFoodBinding
import edu.iu.kevschoo.final_project.model.Food

class FoodAdapter(
    private var foodItems: List<Food>,
    private val onQuantityChanged: (String, Int) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    class FoodViewHolder(val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder
    {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFoodBinding.inflate(layoutInflater, parent, false)
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int)
    {
        val foodItem = foodItems[position]
        holder.binding.apply {
            tvFoodName.text = foodItem.name
            tvFoodPrice.text = "$${foodItem.cost}"
            etQuantity.setText("0")

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

    override fun getItemCount(): Int = foodItems.size

    fun updateFoodItems(newFoodItems: List<Food>)
    {
        foodItems = newFoodItems
        Log.d("FoodAdapter", "Number of food items loaded: ${foodItems.size}")
        notifyDataSetChanged()
    }
}