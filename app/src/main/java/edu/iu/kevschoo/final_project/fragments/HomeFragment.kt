package edu.iu.kevschoo.final_project.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import edu.iu.kevschoo.final_project.R
import edu.iu.kevschoo.final_project.RestaurantAdapter
import edu.iu.kevschoo.final_project.SharedViewModel
import edu.iu.kevschoo.final_project.databinding.FragmentHomeBinding
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import edu.iu.kevschoo.final_project.model.Restaurant

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by viewModels({requireActivity()})

    private lateinit var popularRestaurantsAdapter: RestaurantAdapter
    private lateinit var allRestaurantsAdapter: RestaurantAdapter
    private var isSearchActive = false


    /**
     * Inflates the layout for this fragment
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return Return the View for the fragment's UI, or null
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
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
        setupRecyclerViews()
        setupSearchView()
        observeViewModel()
        viewModel.fetchUserData()
    }

    private fun observeViewModel()
    {
        viewModel.recentRestaurants.observe(viewLifecycleOwner) { recentRestaurants ->
            if (recentRestaurants.isNullOrEmpty())
            { viewModel.allRestaurants.value?.let { updatePopularRestaurantsList(it.take(5)) } }
            else { updatePopularRestaurantsList(recentRestaurants) }
        }

        viewModel.allRestaurants.observe(viewLifecycleOwner) { allRestaurants ->
            if (!isSearchActive)
            { allRestaurantsAdapter.updateList(allRestaurants) }
        }

        viewModel.filteredRestaurants.observe(viewLifecycleOwner) { filteredRestaurants ->
            if (isSearchActive)
            { allRestaurantsAdapter.updateList(filteredRestaurants) }
        }
    }

    private fun setupRecyclerViews()
    {
        popularRestaurantsAdapter = RestaurantAdapter(emptyList()) { restaurant ->
            Log.d("HomeFragment", "Selected Restaurant ID: ${restaurant.id}")
            viewModel.selectRestaurant(restaurant)
            navigateToRestaurantFragment()
        }
        allRestaurantsAdapter = RestaurantAdapter(emptyList()) { restaurant ->
            Log.d("HomeFragment", "Selected Restaurant ID: ${restaurant.id}")
            viewModel.selectRestaurant(restaurant)
            navigateToRestaurantFragment()
        }

        binding.rvRecentRestaurants.apply {
            adapter = popularRestaurantsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.rvAllRestaurants.apply {
            adapter = allRestaurantsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun navigateToRestaurantFragment() { findNavController().navigate(R.id.restaurantFragment) }

    private fun updatePopularRestaurantsList(restaurants: List<Restaurant>) {
        Log.d("HomeFragment", "Updating popular restaurants list: ${restaurants.size}")
        popularRestaurantsAdapter.updateList(if (restaurants.isNotEmpty()) restaurants.take(5) else emptyList())
    }

    private fun setupSearchView()
    {
        val searchItem = binding.toolbarAllFoodOrders.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean
            {
                if (!query.isNullOrEmpty())
                {
                    isSearchActive = true
                    viewModel.filterRestaurants(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean
            {
                isSearchActive = !newText.isNullOrEmpty()
                if (isSearchActive)
                { viewModel.filterRestaurants(newText) }
                else
                { allRestaurantsAdapter.updateList(viewModel.allRestaurants.value ?: emptyList()) }
                return false
            }
        })
    }

    /**
     * Called when the view hierarchy associated with the fragment is being destroyed
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
