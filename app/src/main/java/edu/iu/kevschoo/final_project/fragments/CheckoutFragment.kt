package edu.iu.kevschoo.final_project.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.iu.kevschoo.final_project.CheckoutAdapter
import edu.iu.kevschoo.final_project.R
import edu.iu.kevschoo.final_project.SharedViewModel
import edu.iu.kevschoo.final_project.databinding.FragmentCheckoutBinding
import edu.iu.kevschoo.final_project.model.FoodOrder
import java.io.IOException
import java.util.Locale

class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by viewModels({requireActivity()})
    private lateinit var checkoutAdapter: CheckoutAdapter
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        viewModel.currentFoodOrder.observe(viewLifecycleOwner) { foodOrder ->
            if (foodOrder != null)
            {
                Log.d("CheckoutFragment", "Current order: ${foodOrder.foodID.size} items")
                updateOrderItems(foodOrder)
            }
            else
            { Log.d("CheckoutFragment", "Current order is null") }
        }

        binding.btnConfirmOrder.setOnClickListener {
            val address = binding.addressEditText.text.toString()
            if (address.isNotEmpty())
            {
                val coordinates = getCoordinatesFromAddress(address)
                if (coordinates != null)
                { processOrderWithCoordinates(coordinates) }
                else
                { showErrorDialog("Invalid address or unable to geocode.") }
            }
            else { checkAndRequestLocationPermissions() }

        }
    }

    private fun showErrorDialog(errorMsg: String)
    {
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(errorMsg)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun updateOrderItems(foodOrder: FoodOrder)
    {
        val foodItems = viewModel.allFoods.value.orEmpty()
        val orderItems = foodOrder.foodID.mapIndexedNotNull { index, foodId ->
            val food = foodItems.find { it.id == foodId }
            val quantity = foodOrder.foodAmount.getOrElse(index) { 0 }
            if (food != null && quantity > 0) Pair(food, quantity) else null
        }
        checkoutAdapter.setOrderItems(orderItems)
        updateTotalCost()
    }

    private fun updateTotalCost()
    {
        val totalCost = viewModel.calculateTotalCost()
        binding.tvTotal.text = "Total: $${totalCost}"
    }

    private fun setupRecyclerView()
    {
        checkoutAdapter = CheckoutAdapter(
            onQuantityChanged = { foodId, quantity ->
                viewModel.updateFoodOrder(foodId, quantity)
                if (quantity == 0) { viewModel.removeFoodFromOrder(foodId) }
                updateTotalCost()
            },
            onItemRemoved = { foodId ->
                viewModel.removeFoodFromOrder(foodId)
                updateTotalCost()
            }
        )
        binding.rvFoodItems.adapter = checkoutAdapter
        binding.rvFoodItems.layoutManager = LinearLayoutManager(context)
        attachItemTouchHelper()
    }

    private fun attachItemTouchHelper()
    {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean
            { return false }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
            {
                val foodId = checkoutAdapter.getFoodId(viewHolder.adapterPosition)
                viewModel.removeFoodFromOrder(foodId)
                updateTotalCost()
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvFoodItems)
    }

    private fun processOrderWithCoordinates(coordinates: Pair<Double, Double>)
    {
        viewModel.confirmOrder(coordinates,
            onSuccess = { findNavController().navigate(R.id.homeFragment) },
            onFailure = { errorMsg -> showErrorDialog(errorMsg) }
        )
    }

    private fun checkAndRequestLocationPermissions()
    {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
        { requestLocationPermissions() }
        else { getUserLocation() }
    }

    private fun getCoordinatesFromAddress(address: String): Pair<Double, Double>?
    {
        context?.let { safeContext ->
            val geocoder = Geocoder(safeContext, Locale.getDefault())
            return try
            {
                val addressResults = geocoder.getFromLocationName(address, 1)
                addressResults?.firstOrNull()?.let { location -> Pair(location.latitude, location.longitude) }
            }
            catch (e: IOException) {
                Log.e("CheckoutFragment", "Geocoder IO Exception", e)
                null
            }
        }
        return null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                { getUserLocation() }
                else
                { showErrorDialog("Location permission is required to place the order.") } }
        }
    }


    private fun getUserLocation()
    {
        context?.let { safeContext ->
            val locationManager = safeContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled)
            {
                showErrorDialog("Location services are disabled.")
                return
            }

            try
            {
                val location = if (isGpsEnabled) { locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) }
                else { locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }

                location?.let {
                    val userCoordinates = Pair(it.latitude, it.longitude)
                    processOrderWithCoordinates(userCoordinates) } ?: showErrorDialog("Unable to determine your location.")
            }
            catch (e: SecurityException) {
                Log.e("CheckoutFragment", "Security Exception", e)
                showErrorDialog("Unable to access your location.")
            }
        }
    }

    private fun requestLocationPermissions()
    {
        requestPermissions(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}