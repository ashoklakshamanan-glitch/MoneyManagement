package com.moneymanagement.spendwise.presentation.addTransaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.moneymanagement.spendwise.R
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.moneymanagement.spendwise.databinding.FragmentAddEditTransactionBinding
import com.moneymanagement.spendwise.domain.model.Category
import com.moneymanagement.spendwise.domain.model.PaymentMethod
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.utils.CategoryIconProvider
import com.moneymanagement.spendwise.utils.DateTimeUtils
import com.moneymanagement.spendwise.utils.collectOnStart
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class AddEditTransactionFragment : Fragment() {

    private var _binding: FragmentAddEditTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditTransactionViewModel by viewModels()
    private var categoriesCache: List<Category> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddEditTransactionBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        setupTextWatchers()
        setupTypeToggle()
        setupDateAndTimePickers()
        setupPaymentMethodPicker()
        binding.saveButton.setOnClickListener { viewModel.saveTransaction() }

        collectOnStart {
            launch { viewModel.categories.collect(::renderCategories) }
            launch { viewModel.formState.collect(::renderForm) }
            launch { viewModel.events.collect(::handleEvent) }
        }

    }

    private fun setupTextWatchers() {
        binding.amountInput.doAfterTextChanged { text ->
            val value = text?.toString().orEmpty()
            if (value != viewModel.formState.value.amount) viewModel.onAmountChanged(value)
        }
        binding.titleInput.doAfterTextChanged { text ->
            val value = text?.toString().orEmpty()
            if (value != viewModel.formState.value.title) viewModel.onTitleChanged(value)
        }
        binding.notesInput.doAfterTextChanged { text ->
            val value = text?.toString().orEmpty()
            if (value != viewModel.formState.value.notes) viewModel.onNotesChanged(value)
        }
    }

    private fun setupTypeToggle() {
        binding.typeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val type =
                if (checkedId == binding.typeIncomeButton.id) TransactionType.INCOME else TransactionType.EXPENSE
            if (type != viewModel.formState.value.type) viewModel.onTypeChanged(type)
        }
        binding.typeToggleGroup.check(binding.typeExpenseButton.id)
        binding.categoryDropdown.setOnItemClickListener { _, _, position, _ ->
            categoriesCache.getOrNull(position)?.let { viewModel.onCategorySelected(it.id) }
        }
    }

    private fun setupDateAndTimePickers() {
        binding.dateInput.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setSelection(toPickerDateMillis(viewModel.formState.value.dateMillis))
                .build()
            picker.addOnPositiveButtonClickListener { utcMillis ->
                val date = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
                viewModel.onDateChanged(DateTimeUtils.toEpochMillis(date))
            }
            picker.show(childFragmentManager, "transaction_date")
        }
        binding.dateInputLayout.setEndIconOnClickListener { binding.dateInput.performClick() }

        binding.timeInput.setOnClickListener {
            val parts = viewModel.formState.value.time.split(':')
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(parts.getOrNull(0)?.toIntOrNull() ?: 0)
                .setMinute(parts.getOrNull(1)?.toIntOrNull() ?: 0)
                .build()
            picker.addOnPositiveButtonClickListener {
                viewModel.onTimeChanged("%02d:%02d".format(picker.hour, picker.minute))
            }
            picker.show(childFragmentManager, "transaction_time")
        }
        binding.timeInputLayout.setEndIconOnClickListener { binding.timeInput.performClick() }
    }

    private fun setupPaymentMethodPicker() {
        val methods = PaymentMethod.entries.toList()
        binding.paymentMethodDropdown.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                methods.map(::paymentMethodLabel)
            )
        )
        binding.paymentMethodDropdown.setOnItemClickListener { _, _, position, _ ->
            methods.getOrNull(position)?.let(viewModel::onPaymentMethodChanged)
        }
    }

    private fun renderCategories(categories: List<Category>) {
        categoriesCache = categories
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            categories.map { it.name }
        )
        binding.categoryDropdown.setAdapter(adapter)

        val selectedId = viewModel.formState.value.selectedCategoryId
        val selectedCategory = categories.find { it.id == selectedId }
        binding.categoryDropdown.setText(selectedCategory?.name ?: "", false)
        selectedCategory?.let {
            binding.categoryInputLayout.startIconDrawable =
                ContextCompat.getDrawable(
                    requireContext(),
                    CategoryIconProvider.getIcon(it.icon)
                )
        }
    }

    private fun renderForm(state: AddEditFormState) {
        binding.toolbar.title =
            getString(if (state.isEditMode) R.string.edit_transaction_title else R.string.add_transaction_title)
        if (binding.amountInput.text?.toString() != state.amount) binding.amountInput.setText(state.amount)
        if (binding.titleInput.text?.toString() != state.title) binding.titleInput.setText(state.title)
        if (binding.notesInput.text?.toString() != state.notes) binding.notesInput.setText(state.notes)

        binding.typeToggleGroup.check(
            if (state.type == TransactionType.INCOME) binding.typeIncomeButton.id else binding.typeExpenseButton.id
        )
        binding.dateInput.setText(formatDate(state.dateMillis))
        binding.timeInput.setText(state.time)
        binding.paymentMethodDropdown.setText(paymentMethodLabel(state.paymentMethod), false)
        binding.amountInputLayout.error = state.amountError
        binding.titleInputLayout.error = state.titleError
        binding.categoryInputLayout.error = state.categoryError
        binding.saveButton.isEnabled = !state.isSaving
        binding.saveButton.setText(if (state.isSaving) R.string.action_saving else R.string.action_save)
    }

    private fun handleEvent(event: AddEditTransactionEvent) {
        when (event) {
            AddEditTransactionEvent.Saved -> {
                Snackbar.make(binding.root, R.string.transaction_saved, Snackbar.LENGTH_SHORT)
                    .show()
                findNavController().navigateUp()
            }
            is AddEditTransactionEvent.SaveFailed-> { Snackbar.make(binding.root, /*R.string.error_database*/event.message, Snackbar.LENGTH_LONG).show()}

        }
    }

    private fun formatDate(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

    private fun toPickerDateMillis(millis: Long): Long =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private fun paymentMethodLabel(method: PaymentMethod): String =
        method.name.lowercase().split('_')
            .joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
