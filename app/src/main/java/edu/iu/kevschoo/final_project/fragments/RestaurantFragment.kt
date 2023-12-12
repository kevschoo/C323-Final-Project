package edu.iu.kevschoo.final_project.fragments

import FoodAdapter
import ImageAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import edu.iu.kevschoo.final_project.R
import edu.iu.kevschoo.final_project.SharedViewModel
import edu.iu.kevschoo.final_project.databinding.FragmentRestaurantBinding
import edu.iu.kevschoo.final_project.model.Restaurant

class RestaurantFragment : Fragment() {

    private var _binding: FragmentRestaurantBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by viewModels({requireActivity()})

    /**
     * Inflates the layout for this fragment
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return Return the View for the fragment's UI, or null
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = FragmentRestaurantBinding.inflate(inflater, container, false)
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
        viewModel.selectedRestaurant.observe(viewLifecycleOwner) { restaurant ->
            if (restaurant != null)
            {
                Log.d("RestaurantFragment", "Loaded Restaurant: ${restaurant.name}")
                setupRecyclerViews(restaurant)
                updateUI(restaurant)
                viewModel.fetchCurrentRestaurantFood(restaurant.id)
            }
            else { Log.d("RestaurantFragment", "Restaurant is null") }
        }

        viewModel.currentRestaurantFood.observe(viewLifecycleOwner) { foods ->
            Log.d("RestaurantFragment", "Number of food items loaded for restaurant: ${foods.size}")
            (binding.rvFoodItems.adapter as FoodAdapter).updateFoodItems(foods)
        }
    }

    /**
     * Sets up RecyclerViews for displaying restaurant images and food items
     * @param restaurant The Restaurant object whose details are to be displayed
     */
    private fun setupRecyclerViews(restaurant: Restaurant)
    {
        val imageAdapter = ImageAdapter(restaurant.pictureList)
        binding.rvRestaurantImages.adapter = imageAdapter
        binding.rvRestaurantImages.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val currentOrderQuantities = viewModel.currentFoodOrder.value?.foodID?.zip(
            viewModel.currentFoodOrder.value?.foodAmount ?: listOf()
        )?.toMap() ?: mapOf()

        val foodAdapter = FoodAdapter(
            emptyList(),
            currentOrderQuantities,
            { foodId, quantity -> viewModel.updateFoodOrder(foodId, quantity) }
        )
        binding.rvFoodItems.adapter = foodAdapter

        binding.btnCheckout.setOnClickListener { findNavController().navigate(R.id.checkoutFragment) }
        binding.rvFoodItems.adapter = foodAdapter
        binding.rvFoodItems.layoutManager = LinearLayoutManager(context)
    }

    /**
     * Updates the UI components with the details of the selected restaurant
     * @param restaurant The Restaurant object whose details are to be displayed
     */
    private fun updateUI(restaurant: Restaurant) { binding.tvRestaurantName.text = restaurant.name }

    /**
     * Called when the view hierarchy associated with the fragment is being destroyed
     */
    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}