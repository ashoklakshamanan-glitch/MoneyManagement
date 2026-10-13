package com.moneymanagement.spendwise.presentation.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.fragment.app.viewModels
import com.moneymanagement.spendwise.R
import com.moneymanagement.spendwise.databinding.BottomsheetFilterTransactionsBinding
import com.moneymanagement.spendwise.domain.model.Category
import com.moneymanagement.spendwise.domain.model.PaymentMethod
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.utils.DateTimeUtils
import com.moneymanagement.spendwise.utils.collectOnStart
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId

@AndroidEntryPoint
class TransactionFilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetFilterTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionsViewModel by viewModels({ requireParentFragment() })

    private var pendingStartDate: Long? = null
    private var pendingEndDate: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetFilterTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val current = viewModel.filter.value
        pendingStartDate = current.startDate
        pendingEndDate = current.endDate
        updateDateRangeButtonText()

        setupTypeChips(current.type)
        setupPaymentMethodChips(current.paymentMethods)
        collectOnStart { viewModel.allCategories.collect { setupCategoryChips(it, current.categoryIds) } }

        binding.dateRangeButton.setOnClickListener { openDateRangePicker() }
        binding.resetButton.setOnClickListener {
            viewModel.clearFilters()
            dismiss()
        }
        binding.applyButton.setOnClickListener { applyAndDismiss() }
    }

    private fun setupTypeChips(currentType: TransactionType?) {
        val checkedId = when (currentType) {
            TransactionType.EXPENSE -> binding.chipTypeExpense.id
            TransactionType.INCOME -> binding.chipTypeIncome.id
            null -> binding.chipTypeAll.id
        }
        binding.typeChipGroup.check(checkedId)
    }

    private fun setupCategoryChips(categories: List<Category>, selectedIds: Set<Long>) {
        if (binding.categoryChipGroup.childCount == categories.size) return
        binding.categoryChipGroup.removeAllViews()
        for (category in categories) {
            val chip = Chip(requireContext()).apply {
                text = category.name
                isCheckable = true
                isChecked = category.id in selectedIds
                tag = category.id
                setEnsureMinTouchTargetSize(false)
            }
            binding.categoryChipGroup.addView(chip)
        }
    }

    private fun setupPaymentMethodChips(selected: Set<PaymentMethod>) {
        binding.paymentMethodChipGroup.removeAllViews()
        for (method in PaymentMethod.entries) {
            val chip = Chip(requireContext()).apply {
                text = method.displayName
                isCheckable = true
                isChecked = method in selected
                tag = method
                setEnsureMinTouchTargetSize(false)
            }
            binding.paymentMethodChipGroup.addView(chip)
        }
    }

    private fun openDateRangePicker() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        if (pendingStartDate != null && pendingEndDate != null) {
            builder.setSelection(
                Pair(toUtcMillis(pendingStartDate!!), toUtcMillis(pendingEndDate!!))
            )
        }
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { selection ->
            pendingStartDate = fromUtcMillis(selection.first)
            pendingEndDate = fromUtcMillis(selection.second)
            updateDateRangeButtonText()
        }
        picker.show(childFragmentManager, "date_range_picker")
    }

    private fun updateDateRangeButtonText() {
        val start = pendingStartDate
        val end = pendingEndDate
        binding.dateRangeButton.text = if (start != null && end != null) {
            "${DateTimeUtils.formatDate(start)} \u2013 ${DateTimeUtils.formatDate(end)}"
        } else {
            getString(R.string.filter_date_range)
        }
    }

    private fun applyAndDismiss() {
        val selectedType = when (binding.typeChipGroup.checkedChipId) {
            binding.chipTypeExpense.id -> TransactionType.EXPENSE
            binding.chipTypeIncome.id -> TransactionType.INCOME
            else -> null
        }
        val selectedCategoryIds = (0 until binding.categoryChipGroup.childCount)
            .map { binding.categoryChipGroup.getChildAt(it) as Chip }
            .filter { it.isChecked }
            .map { it.tag as Long }
            .toSet()
        val selectedPaymentMethods = (0 until binding.paymentMethodChipGroup.childCount)
            .map { binding.paymentMethodChipGroup.getChildAt(it) as Chip }
            .filter { it.isChecked }
            .map { it.tag as PaymentMethod }
            .toSet()

        viewModel.applyFilters(selectedType, selectedCategoryIds, selectedPaymentMethods, pendingStartDate, pendingEndDate)
        dismiss()
    }

    private fun toUtcMillis(localMillis: Long): Long =
        DateTimeUtils.toLocalDate(localMillis).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()

    private fun fromUtcMillis(utcMillis: Long): Long =
        DateTimeUtils.toEpochMillis(Instant.ofEpochMilli(utcMillis).atZone(ZoneId.of("UTC")).toLocalDate())

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
