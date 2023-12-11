package edu.iu.kevschoo.final_project.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import edu.iu.kevschoo.final_project.SharedViewModel
import edu.iu.kevschoo.final_project.databinding.FragmentFoodOrdersBinding
import java.util.Calendar
import java.util.Date
import edu.iu.kevschoo.final_project.FoodOrderAdapter
import edu.iu.kevschoo.final_project.R

class FoodOrdersFragment : Fragment() {

    private var _binding: FragmentFoodOrdersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by viewModels({requireActivity()})

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = FragmentFoodOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        observeOrders()
        binding.rvFoodItems.layoutManager = LinearLayoutManager(context)
    }

    private fun observeOrders()
    {
        viewModel.allUserOrders.observe(viewLifecycleOwner) { orders ->
            Log.d("FoodOrdersFragment", "Observing ${orders.size} total orders")
            val currentDayOrders = orders.filter { it.orderDate.isToday() }
            val sortedOrders = orders.sortedByDescending { it.orderDate }

            val adapter = FoodOrderAdapter(
                orders = sortedOrders,
                allFoods = viewModel.allFoods.value.orEmpty(),
                getRestaurantName = { restaurantId -> viewModel.allRestaurants.value?.find { it.id == restaurantId }?.name },
                onReorderClicked = { order -> viewModel.reorderOrder(order) { navigateToCheckoutFragment() } },
                onTrackClicked = { order -> viewModel.trackOrder(order) { navigateToMapFragment() } }
            )

            adapter.showReorderButton = false
            adapter.showTrackButton = true
            binding.rvFoodItems.adapter = adapter
        }
    }

    private fun Date.isToday(): Boolean
    {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal2.time = this
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun navigateToCheckoutFragment() { findNavController().navigate(R.id.checkoutFragment) }

    private fun navigateToMapFragment() { findNavController().navigate(R.id.mapFragment) }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}