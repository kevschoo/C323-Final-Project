package edu.iu.kevschoo.final_project.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import edu.iu.kevschoo.final_project.SharedViewModel
import edu.iu.kevschoo.final_project.databinding.FragmentCreateRestaurantBinding
import edu.iu.kevschoo.final_project.model.Food
import edu.iu.kevschoo.final_project.model.Restaurant

class CreateRestaurantFragment : Fragment() {

    private var _binding: FragmentCreateRestaurantBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by viewModels({requireActivity()})
    private val menuItems = mutableListOf<Food>()


    /**
     * Inflates the layout for this fragment
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return Return the View for the fragment's UI, or null
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = FragmentCreateRestaurantBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called immediately after onCreateView
     * @param view The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle)
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        fun updateFoodListDisplay()
        {
            val foodListString = menuItems.joinToString(separator = "\n") { "${it.name} - $${it.cost}" }
            binding.tvFoodList.text = foodListString
            binding.tvFoodList.visibility = if (menuItems.isEmpty()) View.GONE else View.VISIBLE
        }

        binding.btnAddFood.setOnClickListener {
            val foodName = binding.etFoodName.text.toString()
            val foodPrice = binding.etFoodPrice.text.toString().toFloatOrNull()
            if (foodName.isNotEmpty() && foodPrice != null)
            {
                val food = Food(name = foodName, cost = foodPrice)
                viewModel.createFood(food) { foodId ->
                    if (foodId != null)
                    {
                        menuItems.add(food.copy(id = foodId))
                        updateFoodListDisplay()
                        binding.etFoodName.text.clear()
                        binding.etFoodPrice.text.clear()
                    }
                }
            }
        }

        binding.btnCreateRestaurant.setOnClickListener {
            val restaurantName = binding.etRestaurantName.text.toString()
            val restaurantAddress = binding.etRestaurantAddress.text.toString()

            if (restaurantName.isNotEmpty() && restaurantAddress.isNotEmpty() && menuItems.isNotEmpty())
            {
                val restaurant = Restaurant(
                    name = restaurantName,
                    menu = menuItems.map { it.id },
                    address = restaurantAddress
                )
                viewModel.createRestaurant(restaurant)

                binding.etRestaurantName.text.clear()
                binding.etRestaurantAddress.text.clear()
                menuItems.clear()
                updateFoodListDisplay()
            }
        }

        updateFoodListDisplay()
    }

    /**
     * Called when the view hierarchy associated with the fragment is being destroyed
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}