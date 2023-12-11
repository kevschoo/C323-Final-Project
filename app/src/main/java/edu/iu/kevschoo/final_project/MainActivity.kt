package edu.iu.kevschoo.final_project

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView
import edu.iu.kevschoo.final_project.databinding.ActivityMainBinding
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import edu.iu.kevschoo.final_project.SharedViewModel

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: NavigationView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        setSupportActionBar(binding.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        toggle = ActionBarDrawerToggle(this, drawerLayout, binding.toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        sharedViewModel.authenticationState.observe(this) { authState ->
            if (authState == AuthenticationState.UNAUTHENTICATED)
            {
                navController.navigate(R.id.loginFragment)
                toggle.isDrawerIndicatorEnabled = false
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
            else
            {
                toggle.isDrawerIndicatorEnabled = true
                supportActionBar?.setDisplayHomeAsUpEnabled(true)

            }
        }
        sharedViewModel.fetchUserData()

        val headerView = binding.navView.getHeaderView(0)
        val tvName: TextView = headerView.findViewById(R.id.tvName)
        val tvEmail: TextView = headerView.findViewById(R.id.tvEmail)
        val imageView: ImageView = headerView.findViewById(R.id.imageView)

        sharedViewModel.userName.observe(this) { name -> tvName.text = name }

        sharedViewModel.userEmail.observe(this) { email -> tvEmail.text = email }

        sharedViewModel.userProfilePictureUrl.observe(this) { url ->
            if (url.isNullOrEmpty()) { Glide.with(this).load(R.drawable.ic_profile_picture).into(imageView) }
            else { Glide.with(this).load(url).into(imageView) }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        val navController = findNavController(R.id.nav_host_fragment)

        if (sharedViewModel.authenticationState.value == AuthenticationState.UNAUTHENTICATED) { navController.navigate(R.id.loginFragment) }
        else {
            when (item.itemId) {
                R.id.nav_home -> navController.navigate(R.id.homeFragment)
                R.id.nav_orders -> navController.navigate(R.id.foodOrdersFragment)
                R.id.nav_all_orders -> navController.navigate(R.id.allFoodOrdersFragment)
                R.id.nav_map -> navController.navigate(R.id.mapFragment)
                R.id.nav_calender -> navController.navigate(R.id.calendarGraphFragment)
                R.id.nav_profile -> navController.navigate(R.id.profileFragment)
                R.id.nav_create -> navController.navigate(R.id.createRestaurantFragment)
                R.id.nav_logout -> {
                    sharedViewModel.signOut()
                    navController.navigate(R.id.loginFragment)
                }
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed()
    {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) { binding.drawerLayout.closeDrawer(GravityCompat.START) }
        else { super.onBackPressed() }
    }
}