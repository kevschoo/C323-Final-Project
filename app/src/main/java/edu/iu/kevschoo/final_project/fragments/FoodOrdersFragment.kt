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


    /**
     * Inflates the layout for this fragment
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return Return the View for the fragment's UI, or null
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = FragmentFoodOrdersBinding.inflate(inflater, container, false)
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
        viewModel.reFetchOrders()
        observeOrders()
        binding.rvFoodItems.layoutManager = LinearLayoutManager(context)
    }
    /**
     * Observes the orders
     */
    private fun observeOrders()
    {
        viewModel.allUserOrders.observe(viewLifecycleOwner) { orders ->
            Log.d("FoodOrdersFragment", "Observing ${orders.size} total orders")
            orders.forEach { order ->
                if (!order.isDelivered && order.travelTime <= Date()) {
                    viewModel.updateOrderStatusAsDelivered(order.id)
                }
            }
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

    /**
     * Navigates to the checkout fragment
     */
    private fun navigateToCheckoutFragment() { findNavController().navigate(R.id.checkoutFragment) }

    /**
     * Navigates to the map fragment
     */
    private fun navigateToMapFragment() { findNavController().navigate(R.id.mapFragment) }

    /**
     * Called when the view hierarchy associated with the fragment is being destroyed
     */
    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}