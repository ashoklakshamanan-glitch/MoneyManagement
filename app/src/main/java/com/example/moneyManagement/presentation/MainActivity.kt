package com.example.moneyManagement.presentation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.moneyManagement.R
import com.example.moneyManagement.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding?=null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private val topLevelDestinationIds = setOf(
        R.id.dashboardFragment,
        R.id.transactionsFragment,
        R.id.analyticsFragment,
        R.id.budgetFragment,
        R.id.settingsFragment
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
    }
    private fun setupNavigation(){
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment

        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
        /*    binding.bottomNavigation.visibility =
                if (destination.id in topLevelDestinationIds) {
                    View.VISIBLE
                } else {
                    View.GONE
                }*/
        }
    }
}