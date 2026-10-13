package com.moneymanagement.spendwise.presentation

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.moneymanagement.spendwise.R
import com.moneymanagement.spendwise.databinding.ActivityMainBinding
import com.moneymanagement.spendwise.domain.model.ThemeMode
import com.moneymanagement.spendwise.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

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
        observeThemeChanges()
    }

    private fun observeThemeChanges() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsRepository.userPreferences
                    .map { it.themeMode }
                    .distinctUntilChanged()
                    .collect { themeMode ->
                        applyThemeMode(themeMode)
                    }
            }
        }
    }

    private fun applyThemeMode(mode: ThemeMode) {
        val nightMode = when (mode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
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
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
                    true
                binding.navHostFragmentContainer.setPadding(0, 0, 0, 0)
            } else {
                val isNightMode =
                    (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
                    !isNightMode
                applyWindowInsets()
            }
        }
        onBackPressedDispatcher.addCallback(this) {
            val currentFragment = navController.currentDestination?.id
            when (currentFragment) {
                R.id.dashboardFragment -> showExitDialog()
                in topLevelDestinationIds -> {
                    navController.popBackStack(R.id.dashboardFragment, false)
                }

                else -> {
                    navController.popBackStack()
                }
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

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit")
            .setMessage("Do you want to exit the app?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Exit") { _, _ ->
                finishAndRemoveTask()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
