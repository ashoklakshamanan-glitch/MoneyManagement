package com.example.moneyManagement.presentation

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.moneyManagement.R
import com.example.moneyManagement.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
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
        installSplashScreen()
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment

        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.visibility =
                if (destination.id in topLevelDestinationIds) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            if (destination.id == R.id.splashFragment) {
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
                binding.navHostFragmentContainer.setPadding(0, 0, 0, 0)
            } else {
                val isNightMode = (resources.configuration.uiMode and  Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = !isNightMode
                applyWindowInsets()
            }
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragmentContainer) { view, insets ->
            val top = insets.getInsets(
                WindowInsetsCompat.Type.statusBars()
            ).top

            view.setPadding(
                view.paddingLeft,
                top,
                view.paddingRight,
                view.paddingBottom
            )

            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
