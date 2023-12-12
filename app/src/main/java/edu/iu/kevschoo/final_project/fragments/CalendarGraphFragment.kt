package edu.iu.kevschoo.final_project.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import edu.iu.kevschoo.final_project.SharedViewModel
import edu.iu.kevschoo.final_project.databinding.FragmentCalendarGraphBinding
import edu.iu.kevschoo.final_project.model.FoodOrder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarGraphFragment : Fragment() {

    private var _binding: FragmentCalendarGraphBinding? = null
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
        _binding = FragmentCalendarGraphBinding.inflate(inflater, container, false)
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
        binding.toggleButton.isChecked = true
        binding.compactCalendarView.visibility = if (binding.toggleButton.isChecked) View.VISIBLE else View.GONE
        binding.barChart.visibility = if (binding.toggleButton.isChecked) View.GONE else View.VISIBLE

        viewModel.reFetchOrders()
        viewModel.getUserOrderDates().observe(viewLifecycleOwner) { dates -> highlightOrderDatesInCalendar(dates) }
        setupBarChartListener()
        viewModel.updateWeeklySpendingData()
        setupBarChart(viewModel.weeklySpendingData.value ?: emptyList())

        binding.toggleButton.setOnClickListener {
            if (binding.toggleButton.isChecked)
            {
                binding.compactCalendarView.visibility = View.VISIBLE
                binding.barChart.visibility = View.GONE
            }
            else
            {
                binding.compactCalendarView.visibility = View.GONE
                binding.barChart.visibility = View.VISIBLE
                viewModel.updateWeeklySpendingData()
                setupBarChart(viewModel.weeklySpendingData.value ?: emptyList())
            }
        }

        binding.compactCalendarView.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) { displayOrdersForDate(dateClicked) }
            override fun onMonthScroll(firstDayOfNewMonth: Date) {} })
    }

    /**
     * Displays the orders for a specific date
     * @param selectedDate The date for which to display orders
     */
    private fun displayOrdersForDate(selectedDate: Date)
    {
        val ordersOnDate = viewModel.allUserOrders.value?.filter { order -> isSameDay(order.orderDate, selectedDate) }.orEmpty()

        val totalSpent = ordersOnDate.sumCosts()
        val restaurantNames = ordersOnDate.mapNotNull { order -> viewModel.allRestaurants.value?.find { it.id == order.restaurantID }?.name }.distinct().joinToString(", ")

        val infoText = if (ordersOnDate.isNotEmpty()) { "Total Spent: $${String.format("%.2f", totalSpent)}\n" + "Restaurants: $restaurantNames" }
        else { "No orders on this day." }

        binding.infoTextView.text = infoText
    }

    /**
     * Sums the costs of a list of food orders
     * @return The total cost
     */
    fun List<FoodOrder>.sumCosts(): Float
    {
        var sum = 0f
        for (order in this) { sum += order.cost ?: 0f }
        return sum
    }

    /**
     * Highlights the dates with orders in the calendar view
     * @param dates The dates to highlight in the calendar
     */
    private fun highlightOrderDatesInCalendar(dates: List<Date>)
    {
        dates.forEach { date ->
            val event = Event(Color.RED, date.time, "Order")
            binding.compactCalendarView.addEvent(event)
        }
    }

    /**
     * Gets a label for a day
     * @param dayIndex The index of the day
     * @return The label for the day
     */
    private fun getDayLabel(dayIndex: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6 + dayIndex)
        return SimpleDateFormat("E", Locale.getDefault()).format(cal.time)
    }

    /**
     * Displays the orders for a specific day index
     * @param dayIndex The index of the day for which to display orders
     */
    private fun displayOrdersForDay(dayIndex: Int)
    {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6 + dayIndex)
        val selectedDate = cal.time

        val ordersOnDate = viewModel.allUserOrders.value?.filter { order -> isSameDay(order.orderDate, selectedDate) }.orEmpty()

        val totalSpent = ordersOnDate.sumCosts()

        val spendingByRestaurant = ordersOnDate.groupBy { order ->
            viewModel.allRestaurants.value?.find { it.id == order.restaurantID }?.name ?: "Unknown" }.mapValues { (_, orders) -> orders.sumCosts() }

        val restaurantDetails = spendingByRestaurant.entries.joinToString("\n") { (name, cost) -> "$name: $${String.format("%.2f", cost)}" }

        val message = if (ordersOnDate.isNotEmpty())
        {
            "Date: ${SimpleDateFormat("MMMM dd", Locale.getDefault()).format(selectedDate)}\n" +
                    "Total Spent: $${String.format("%.2f", totalSpent)}\n" +
                    "Details:\n$restaurantDetails"
        }
        else { "No orders on this day." }

        binding.infoTextView.text = message
    }

    /**
     * Checks if two dates are on the same day
     * @param date1 The first date
     * @param date2 The second date
     * @return True if the dates are on the same day, false otherwise
     */
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1[Calendar.YEAR] == cal2[Calendar.YEAR] &&
                cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]
    }

    /**
     * Sets up the bar chart with spending data
     * @param spendingData The spending data to display in the bar chart
     */
    private fun setupBarChart(spendingData: List<Float>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        spendingData.forEachIndexed { index, amount ->
            entries.add(BarEntry(index.toFloat(), amount))
            labels.add(getDayLabel(index))
        }

        val dataSet = BarDataSet(entries, "Daily Spending")
        val data = BarData(dataSet)
        binding.barChart.data = data

        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.barChart.invalidate()
    }

    /**
     * Sets up the listener for the bar chart
     */
    private fun setupBarChartListener()
    {
        binding.barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?)
            {
                e?.let {
                    val index = it.x.toInt()
                    displayOrdersForDay(index)
                }
            }
            override fun onNothingSelected() {}
        })
    }

    /**
     * Called when the view hierarchy associated with the fragment is being destroyed
     */
    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}