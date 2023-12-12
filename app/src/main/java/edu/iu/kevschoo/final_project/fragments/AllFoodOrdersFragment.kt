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
import edu.iu.kevschoo.final_project.FoodOrderAdapter
import edu.iu.kevschoo.final_project.R
import edu.iu.kevschoo.final_project.SharedViewModel
import edu.iu.kevschoo.final_project.databinding.FragmentAllFoodOrdersBinding
import edu.iu.kevschoo.final_project.model.FoodOrder
import java.util.Date

class AllFoodOrdersFragment : Fragment() {

    private var _binding: FragmentAllFoodOrdersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by viewModels({requireActivity()})
    private var isSearchActive = false


    /**
     * Inflates the layout for this fragment
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return Return the View for the fragment's UI, or null
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = FragmentAllFoodOrdersBinding.inflate(inflater, container, false)
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
        observeAllOrders()
        binding.rvAllFoodOrders.layoutManager = LinearLayoutManager(context)
        setupSearchView()
    }

    /**
     * Sets up the SearchView for filtering food orders
     */
    private fun setupSearchView()
    {
        val searchItem = binding.toolbarAllFoodOrders.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? androidx.appcompat.widget.SearchView
        searchView?.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean
            {
                handleSearchQuery(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean
            {
                handleSearchQuery(newText)
                return true
            }
        })
    }

    /**
     * Observes all user orders and updates the RecyclerView
     */
    private fun observeAllOrders() { viewModel.allUserOrders.observe(viewLifecycleOwner) { orders -> updateRecyclerView(orders) } }

    /**
     * Observes filtered user orders and updates the RecyclerView
     */
    private fun observeFilteredOrders() { viewModel.filteredOrders.observe(viewLifecycleOwner) { filteredOrders -> updateRecyclerView(filteredOrders) } }

    /**
     * Handles the search query for filtering orders
     * @param query The search query string
     */
    private fun handleSearchQuery(query: String?)
    {
        if (query.isNullOrEmpty() && isSearchActive)
        {
            observeAllOrders()
            isSearchActive = false
        }
        else if (!query.isNullOrEmpty())
        {
            viewModel.filterOrdersByRestaurantName(query)
            if (!isSearchActive)
            {
                observeFilteredOrders()
                isSearchActive = true
            }
        }
    }

    /**
     * Updates the RecyclerView with the given list of food orders
     * @param orders The list of FoodOrder objects to be displayed
     */
    private fun updateRecyclerView(orders: List<FoodOrder>)
    {
        Log.d("AllFoodOrdersFragment", "Updating RecyclerView with ${orders.size} orders")
        val sortedOrders = orders.sortedByDescending { it.orderDate }
        orders.forEach { order ->
            if (!order.isDelivered && order.travelTime <= Date()) {
                viewModel.updateOrderStatusAsDelivered(order.id)
            }
        }
        val adapter = FoodOrderAdapter(
            orders = sortedOrders,
            allFoods = viewModel.allFoods.value.orEmpty(),
            getRestaurantName = { restaurantId -> viewModel.allRestaurants.value?.find { it.id == restaurantId }?.name },
            onReorderClicked = { order -> viewModel.reorderOrder(order) { navigateToCheckoutFragment() } },
            onTrackClicked = { order -> viewModel.trackOrder(order) { navigateToMapFragment() } }
        )

        adapter.showReorderButton = true
        adapter.showTrackButton = false
        binding.rvAllFoodOrders.adapter = adapter
    }

    /**
     * Navigates to the CheckoutFragment
     */
    private fun navigateToCheckoutFragment() { findNavController().navigate(R.id.checkoutFragment) }

    /**
     * Navigates to the MapFragment
     */
    private fun navigateToMapFragment() { findNavController().navigate(R.id.mapFragment) }

    /**
     * Called when the view hierarchy associated with the fragment is being destroyed
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}