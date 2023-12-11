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

class AllFoodOrdersFragment : Fragment() {

    private var _binding: FragmentAllFoodOrdersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by viewModels({requireActivity()})
    private var isSearchActive = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = FragmentAllFoodOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        observeAllOrders()
        binding.rvAllFoodOrders.layoutManager = LinearLayoutManager(context)
        setupSearchView()
    }

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

    private fun observeAllOrders() { viewModel.allUserOrders.observe(viewLifecycleOwner) { orders -> updateRecyclerView(orders) } }

    private fun observeFilteredOrders() { viewModel.filteredOrders.observe(viewLifecycleOwner) { filteredOrders -> updateRecyclerView(filteredOrders) } }

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

    private fun updateRecyclerView(orders: List<FoodOrder>)
    {
        Log.d("AllFoodOrdersFragment", "Updating RecyclerView with ${orders.size} orders")
        val sortedOrders = orders.sortedByDescending { it.orderDate }

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

    private fun navigateToCheckoutFragment() { findNavController().navigate(R.id.checkoutFragment) }

    private fun navigateToMapFragment() { findNavController().navigate(R.id.mapFragment) }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}