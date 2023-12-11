package edu.iu.kevschoo.final_project.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import edu.iu.kevschoo.final_project.SharedViewModel
import edu.iu.kevschoo.final_project.databinding.FragmentMapBinding

import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import edu.iu.kevschoo.final_project.model.FoodOrder

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap)
    {
        val viewModel: SharedViewModel by viewModels({requireActivity()})
        viewModel.currentFoodOrder.observe(viewLifecycleOwner) { order ->
            order?.let {
                val origin = LatLng(order.addressOriginList[0].toDouble(), order.addressOriginList[1].toDouble())
                val destination = LatLng(order.addressDestinationList[0].toDouble(), order.addressDestinationList[1].toDouble())

                googleMap.addMarker(MarkerOptions().position(origin).title("Origin"))
                googleMap.addMarker(MarkerOptions().position(destination).title("Destination"))

                drawRoute(googleMap, origin, destination)

                val zoomLevel = 15f
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, zoomLevel))
            }
        }
    }

    private fun drawRoute(map: GoogleMap, origin: LatLng, destination: LatLng)
    {
        val polylineOptions = PolylineOptions()
            .add(origin)
            .add(destination)
            .width(5f)
            .color(Color.RED)

        map.addPolyline(polylineOptions)
    }

    private fun updateOrderInfoTextView(order: FoodOrder)
    {
        val orderInfo = StringBuilder()
        orderInfo.append("Order Date: ${order.orderDate}\n")
        orderInfo.append("Estimated Arrival Time: ${order.travelTime}\n")
        orderInfo.append("Delivery Status: ${if (order.isDelivered) "Delivered" else "In Transit"}")

        binding.tvOrderInfo.text = orderInfo.toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        val viewModel: SharedViewModel by viewModels({requireActivity()})
        viewModel.currentFoodOrder.observe(viewLifecycleOwner) { order ->
            order?.let {
                updateOrderInfoTextView(it)
            }
        }
    }

    override fun onDestroyView()
    {
        mapView.onDestroy()
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}