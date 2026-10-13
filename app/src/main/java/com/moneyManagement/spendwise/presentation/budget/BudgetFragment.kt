package com.moneymanagement.spendwise.presentation.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.moneymanagement.spendwise.R
import com.moneymanagement.spendwise.databinding.FragmentBudgetBinding
import com.moneymanagement.spendwise.domain.model.Budget
import com.moneymanagement.spendwise.utils.CurrencyFormatter
import com.moneymanagement.spendwise.utils.UiState
import com.moneymanagement.spendwise.utils.collectOnStart
import com.moneymanagement.spendwise.utils.showIf
import com.moneymanagement.spendwise.utils.snackbar
import com.spendwise.app.presentation.budget.CategoryBudgetAdapter
import com.spendwise.app.presentation.budget.SetBudgetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var adapter: CategoryBudgetAdapter

    private var currentCurrencyCode = "INR"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CategoryBudgetAdapter(
            currencyCode = { currentCurrencyCode },
            onClick = { budget -> openCategoryBudgetDialog(budget) },
            onLongClick = { budget -> confirmDeleteBudget(budget) }
        )
        binding.categoryBudgetsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@BudgetFragment.adapter
        }

        binding.editOverallBudgetButton.setOnClickListener { openOverallBudgetDialog() }
        binding.addCategoryBudgetButton.setOnClickListener { showAddCategoryBudgetPicker() }

        collectOnStart  { viewModel.uiState.collect(::render) }
        collectOnStart { viewModel.events.collect(::handleEvent) }
    }

    private fun render(state: UiState<BudgetScreenData>) {
        binding.loadingIndicator.showIf(state is UiState.Loading)
        if (state !is UiState.Success) return

        currentCurrencyCode = state.data.currencyCode
        val overall = state.data.overallBudget

        if (overall != null && overall.amount > 0) {
            binding.overallBudgetAmountText.text = CurrencyFormatter.format(overall.amount, state.data.currencyCode)
            binding.overallProgressBar.progress = overall.percentageUsed.coerceIn(0, 100)
            binding.overallProgressBar.setIndicatorColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (overall.isOverBudget) R.color.color_expense else R.color.md_primary
                )
            )
            binding.overallBudgetSummaryText.text = if (overall.isOverBudget) {
                getString(R.string.budget_over_by, CurrencyFormatter.format(-overall.remaining, state.data.currencyCode))
            } else {
                getString(
                    R.string.budget_spent_of_total,
                    CurrencyFormatter.format(overall.spent, state.data.currencyCode),
                    CurrencyFormatter.format(overall.amount, state.data.currencyCode)
                ) + " \u00B7 " + getString(R.string.budget_remaining, CurrencyFormatter.format(overall.remaining, state.data.currencyCode))
            }
            binding.editOverallBudgetButton.text = getString(R.string.budget_edit_overall_cta)
        } else {
            binding.overallBudgetAmountText.text = CurrencyFormatter.format(0.0, state.data.currencyCode)
            binding.overallProgressBar.progress = 0
            binding.overallBudgetSummaryText.text = getString(R.string.dashboard_no_budget_set)
            binding.editOverallBudgetButton.text = getString(R.string.budget_set_overall_cta)
        }

        adapter.submitList(state.data.categoryBudgets)
        binding.categoryBudgetsEmptyText.showIf(state.data.categoryBudgets.isEmpty())
        binding.categoryBudgetsRecycler.showIf(state.data.categoryBudgets.isNotEmpty())
    }

    private fun openOverallBudgetDialog() {
        val current = (viewModel.uiState.value as? UiState.Success)?.data?.overallBudget?.amount ?: 0.0
        SetBudgetDialogFragment.newInstanceOverall(current).show(childFragmentManager, "overall_budget_dialog")
    }

    private fun openCategoryBudgetDialog(budget: Budget) {
        val categoryId = budget.categoryId ?: return
        SetBudgetDialogFragment
            .newInstanceForCategory(categoryId, budget.categoryName.orEmpty(), budget.amount)
            .show(childFragmentManager, "category_budget_dialog")
    }

    private fun showAddCategoryBudgetPicker() {
        val budgetedIds = (viewModel.uiState.value as? UiState.Success)?.data?.categoryBudgets
            ?.mapNotNull { it.categoryId }?.toSet() ?: emptySet()
        val available = viewModel.expenseCategories.value.filter { it.id !in budgetedIds }

        if (available.isEmpty()) {
            binding.root.snackbar(getString(R.string.budget_empty))
            return
        }

        val names = available.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.budget_add_category_cta)
            .setItems(names) { _, index ->
                val category = available[index]
                SetBudgetDialogFragment
                    .newInstanceForCategory(category.id, category.name, 0.0)
                    .show(childFragmentManager, "category_budget_dialog")
            }
            .show()
    }

    private fun confirmDeleteBudget(budget: Budget) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_budget_title)
            .setMessage(R.string.delete_budget_message)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.deleteBudget(budget.id) }
            .show()
    }

    private fun handleEvent(event: BudgetEvent) {
        when (event) {
            BudgetEvent.Saved -> binding.root.snackbar(getString(R.string.budget_saved))
            is BudgetEvent.ShowError -> binding.root.snackbar(event.message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
