package com.moneymanagement.spendwise.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.moneymanagement.spendwise.BuildConfig
import com.moneymanagement.spendwise.R
import com.moneymanagement.spendwise.databinding.FragmentSettingsBinding
import com.moneymanagement.spendwise.databinding.ItemSettingsRowBinding
import com.moneymanagement.spendwise.domain.model.ThemeMode
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.domain.model.UserPreferences
import com.moneymanagement.spendwise.utils.collectOnStart
import com.moneymanagement.spendwise.data.export.ExportFormat
import com.moneymanagement.spendwise.domain.model.PaymentMethod
import com.moneymanagement.spendwise.utils.snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    private lateinit var rowCurrency: ItemSettingsRowBinding
    private lateinit var rowTheme: ItemSettingsRowBinding
    private lateinit var rowDefaultType: ItemSettingsRowBinding
    private lateinit var rowCategories: ItemSettingsRowBinding
    private lateinit var rowPaymentMethods: ItemSettingsRowBinding
    private lateinit var rowBudget: ItemSettingsRowBinding
    private lateinit var rowExport: ItemSettingsRowBinding
    private lateinit var rowImport: ItemSettingsRowBinding
    private lateinit var rowClearData: ItemSettingsRowBinding
    private lateinit var rowAbout: ItemSettingsRowBinding

    private var pendingFormat: ExportFormat = ExportFormat.CSV

    private val createDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
            uri?.let { viewModel.exportData(it, pendingFormat) }
        }

    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { viewModel.importData(it, pendingFormat) }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindRows()
        setRowContent()
        setRowClickListeners()

        collectOnStart { viewModel.userPreferences.collect(::renderPreferences) }
        collectOnStart { viewModel.events.collect(::handleEvent) }
    }

    private fun bindRows() {
        rowCurrency = binding.rowCurrency
        rowTheme = binding.rowTheme
        rowDefaultType = binding.rowDefaultType
        rowCategories = binding.rowCategories
        rowPaymentMethods = binding.rowPaymentMethods
        rowBudget = binding.rowBudget
        rowExport = binding.rowExport
        rowImport = binding.rowImport
        rowClearData = binding.rowClearData
        rowAbout = binding.rowAbout
    }

    private fun setRowContent() {
        rowCurrency.rowIcon.setImageResource(R.drawable.ic_currency)
        rowCurrency.rowTitle.text = getString(R.string.settings_currency)
        rowCurrency.rowValue.visibility = View.VISIBLE

        rowTheme.rowIcon.setImageResource(R.drawable.ic_theme_system)
        rowTheme.rowTitle.text = getString(R.string.settings_theme)
        rowTheme.rowValue.visibility = View.VISIBLE

        rowDefaultType.rowIcon.setImageResource(R.drawable.ic_wallet)
        rowDefaultType.rowTitle.text = getString(R.string.settings_default_transaction_type)
        rowDefaultType.rowValue.visibility = View.VISIBLE

        rowCategories.rowIcon.setImageResource(R.drawable.ic_category_other)
        rowCategories.rowTitle.text = getString(R.string.settings_manage_categories)
        rowCategories.rowSubtitle.text = getString(R.string.settings_manage_categories_desc)
        rowCategories.rowSubtitle.visibility = View.VISIBLE

        rowPaymentMethods.rowIcon.setImageResource(R.drawable.ic_payment)
        rowPaymentMethods.rowTitle.text = getString(R.string.settings_manage_payment_methods)

        rowBudget.rowIcon.setImageResource(R.drawable.ic_budget)
        rowBudget.rowTitle.text = getString(R.string.settings_monthly_budget)

        rowExport.rowIcon.setImageResource(R.drawable.ic_export)
        rowExport.rowTitle.text = getString(R.string.settings_export_data)
        rowExport.rowSubtitle.text = getString(R.string.settings_export_data_desc)
        rowExport.rowSubtitle.visibility = View.VISIBLE

        rowImport.rowIcon.setImageResource(R.drawable.ic_import)
        rowImport.rowTitle.text = getString(R.string.settings_import_data)
        rowImport.rowSubtitle.text = getString(R.string.settings_import_data_desc)
        rowImport.rowSubtitle.visibility = View.VISIBLE

        rowClearData.rowIcon.setImageResource(R.drawable.ic_delete)
        rowClearData.rowIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.color_expense)
        rowClearData.rowTitle.text = getString(R.string.settings_clear_all_data)
        rowClearData.rowTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_expense))
        rowClearData.rowSubtitle.text = getString(R.string.settings_clear_all_data_desc)
        rowClearData.rowSubtitle.visibility = View.VISIBLE

        rowAbout.rowIcon.setImageResource(R.drawable.ic_splash_logo)
        rowAbout.rowTitle.text = getString(R.string.settings_about_app)
        rowAbout.rowSubtitle.text = getString(R.string.settings_version, BuildConfig.VERSION_NAME)
        rowAbout.rowSubtitle.visibility = View.VISIBLE
    }

    private fun setRowClickListeners() {
        rowCurrency.root.setOnClickListener { showCurrencyPicker() }
        rowTheme.root.setOnClickListener { showThemePicker() }
        rowDefaultType.root.setOnClickListener { showDefaultTypePicker() }
        rowCategories.root.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToCategoriesFragment())
        }
        rowPaymentMethods.root.setOnClickListener { showPaymentMethodsInfo() }
        rowBudget.root.setOnClickListener { findNavController().navigate(R.id.budgetFragment) }
        rowExport.root.setOnClickListener { showExportFormatPicker() }
        rowImport.root.setOnClickListener { showImportFormatPicker() }
        rowClearData.root.setOnClickListener { confirmClearData() }
        rowAbout.root.setOnClickListener { showAboutDialog() }
    }

    private fun renderPreferences(prefs: UserPreferences) {
        rowCurrency.rowValue.text = prefs.currencyCode
        rowTheme.rowValue.text = when (prefs.themeMode) {
            ThemeMode.LIGHT -> "Light"
            ThemeMode.DARK -> "Dark"
            ThemeMode.SYSTEM -> "System"
        }
        rowDefaultType.rowValue.text = if (prefs.defaultTransactionType == TransactionType.INCOME) {
            getString(R.string.type_income)
        } else {
            getString(R.string.type_expense)
        }
    }

    private fun showCurrencyPicker() {
        val labels = resources.getStringArray(R.array.currency_options)
        val codes = resources.getStringArray(R.array.currency_codes)
        val current = codes.indexOf(viewModel.userPreferences.value.currencyCode).coerceAtLeast(0)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_currency)
            .setSingleChoiceItems(labels, current) { dialog, which ->
                viewModel.setCurrency(codes[which])
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun showThemePicker() {
        val options = resources.getStringArray(R.array.theme_options)
        val modes = arrayOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM)
        val current = modes.indexOf(viewModel.userPreferences.value.themeMode).coerceAtLeast(0)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_theme)
            .setSingleChoiceItems(options, current) { dialog, which ->
                viewModel.setThemeMode(modes[which])
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun showDefaultTypePicker() {
        val options = arrayOf(getString(R.string.type_expense), getString(R.string.type_income))
        val types = arrayOf(TransactionType.EXPENSE, TransactionType.INCOME)
        val current = types.indexOf(viewModel.userPreferences.value.defaultTransactionType).coerceAtLeast(0)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_default_transaction_type)
            .setSingleChoiceItems(options, current) { dialog, which ->
                viewModel.setDefaultTransactionType(types[which])
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun showPaymentMethodsInfo() {
        val methods = PaymentMethod.entries.joinToString("\n") { "\u2022 ${it.displayName}" }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_manage_payment_methods)
            .setMessage(methods)
            .setPositiveButton(R.string.action_ok, null)
            .show()
    }

    private fun showExportFormatPicker() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.export_format_title)
            .setItems(arrayOf(getString(R.string.export_format_csv), getString(R.string.export_format_json))) { _, which ->
                pendingFormat = if (which == 0) ExportFormat.CSV else ExportFormat.JSON
                val extension = if (pendingFormat == ExportFormat.CSV) "csv" else "json"
                createDocumentLauncher.launch("spendwise_export.$extension")
            }
            .show()
    }

    private fun showImportFormatPicker() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.export_format_title)
            .setItems(arrayOf(getString(R.string.export_format_csv), getString(R.string.export_format_json))) { _, which ->
                pendingFormat = if (which == 0) ExportFormat.CSV else ExportFormat.JSON
                openDocumentLauncher.launch(arrayOf("*/*"))
            }
            .show()
    }

    private fun confirmClearData() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.clear_data_confirm_title)
            .setMessage(R.string.clear_data_confirm_message)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.settings_clear_all_data) { _, _ -> viewModel.clearAllData() }
            .show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_about_app)
            .setMessage(getString(R.string.settings_about_body) + "\n\n" + getString(R.string.settings_version, BuildConfig.VERSION_NAME))
            .setPositiveButton(R.string.action_ok, null)
            .show()
    }

    private fun handleEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ExportResult -> {
                when {
                    event.success -> binding.root.snackbar(getString(R.string.export_success, event.count))
                    event.errorMessage == "empty" -> binding.root.snackbar(getString(R.string.export_nothing_to_export))
                    else -> binding.root.snackbar(getString(R.string.export_failed))
                }
            }
            is SettingsEvent.ImportResult -> binding.root.snackbar(
                if (event.success) getString(R.string.import_success, event.count) else getString(R.string.import_failed)
            )
            SettingsEvent.DataCleared -> binding.root.snackbar(getString(R.string.clear_data_done))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
