package com.spendwise.app.presentation.budget

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.moneymanagement.spendwise.R
import com.moneymanagement.spendwise.databinding.DialogSetBudgetBinding
import com.moneymanagement.spendwise.presentation.budget.BudgetViewModel
import com.moneymanagement.spendwise.utils.toAmountOrNull
import kotlin.getValue

class SetBudgetDialogFragment : DialogFragment() {

    private val viewModel: BudgetViewModel by viewModels({ requireParentFragment() })

    private val categoryId: Long? by lazy {
        requireArguments().getLong(ARG_CATEGORY_ID, -1L).takeIf { it != -1L }
    }
    private val categoryName: String? by lazy { requireArguments().getString(ARG_CATEGORY_NAME) }
    private val currentAmount: Double by lazy { requireArguments().getDouble(ARG_CURRENT_AMOUNT, 0.0) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogSetBudgetBinding.inflate(layoutInflater)
        if (currentAmount > 0) {
            val text = if (currentAmount == currentAmount.toLong().toDouble()) {
                currentAmount.toLong().toString()
            } else {
                currentAmount.toString()
            }
            binding.budgetAmountInput.setText(text)
        }

        val title = categoryName?.let { getString(R.string.dialog_set_budget_title) + " \u2013 $it" }
            ?: getString(R.string.dialog_set_overall_budget_title)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(binding.root)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_save, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val amount = binding.budgetAmountInput.text?.toString().toAmountOrNull()
                    if (amount == null || amount <= 0.0) {
                        binding.budgetAmountInputLayout.error = getString(R.string.error_budget_amount_required)
                        return@setOnClickListener
                    }
                    val catId = categoryId
                    if (catId != null) viewModel.setCategoryBudget(catId, amount) else viewModel.setOverallBudget(amount)
                    dismiss()
                }
        }
        return dialog
    }

    companion object {
        private const val ARG_CATEGORY_ID = "categoryId"
        private const val ARG_CATEGORY_NAME = "categoryName"
        private const val ARG_CURRENT_AMOUNT = "currentAmount"

        fun newInstanceOverall(currentAmount: Double): SetBudgetDialogFragment = SetBudgetDialogFragment().apply {
            arguments = bundleOf(ARG_CURRENT_AMOUNT to currentAmount)
        }

        fun newInstanceForCategory(categoryId: Long, categoryName: String, currentAmount: Double): SetBudgetDialogFragment =
            SetBudgetDialogFragment().apply {
                arguments = bundleOf(
                    ARG_CATEGORY_ID to categoryId,
                    ARG_CATEGORY_NAME to categoryName,
                    ARG_CURRENT_AMOUNT to currentAmount
                )
            }
    }
}
