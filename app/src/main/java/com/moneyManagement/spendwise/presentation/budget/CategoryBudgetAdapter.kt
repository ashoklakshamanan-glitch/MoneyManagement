package com.spendwise.app.presentation.budget

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moneymanagement.spendwise.R
import com.moneymanagement.spendwise.databinding.ItemBudgetCategoryBinding
import com.moneymanagement.spendwise.domain.model.Budget
import com.moneymanagement.spendwise.utils.CategoryIconProvider
import com.moneymanagement.spendwise.utils.CurrencyFormatter

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Budget>() {
    override fun areItemsTheSame(oldItem: Budget, newItem: Budget) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Budget, newItem: Budget) = oldItem == newItem
}

class CategoryBudgetAdapter(
    private val currencyCode: () -> String,
    private val onClick: (Budget) -> Unit,
    private val onLongClick: (Budget) -> Unit
) : ListAdapter<Budget, CategoryBudgetAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBudgetCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemBudgetCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(budget: Budget) {
            val context = binding.root.context
            binding.categoryName.text = budget.categoryName ?: ""
            binding.categoryIcon.setImageResource(CategoryIconProvider.getIcon(budget.categoryIcon ?: "other"))
            runCatching { binding.categoryAvatarCard.setCardBackgroundColor(Color.parseColor(budget.categoryColor ?: "#78909C")) }

            val progress = budget.percentageUsed.coerceIn(0, 100)
            binding.budgetProgressBar.progress = progress
            binding.percentText.text = "${budget.percentageUsed}%"
            binding.spentOfTotalText.text = context.getString(
                R.string.budget_spent_of_total,
                CurrencyFormatter.format(budget.spent, currencyCode()),
                CurrencyFormatter.format(budget.amount, currencyCode())
            )

            val indicatorColorRes = when {
                budget.isOverBudget -> R.color.color_expense
                budget.isApproachingLimit -> R.color.color_warning
                else -> R.color.md_primary
            }
            binding.budgetProgressBar.setIndicatorColor(ContextCompat.getColor(context, indicatorColorRes))
            binding.warningIcon.visibility =
                if (budget.isOverBudget || budget.isApproachingLimit) View.VISIBLE else View.GONE

            binding.root.setOnClickListener { onClick(budget) }
            binding.root.setOnLongClickListener {
                onLongClick(budget)
                true
            }
        }
    }
}
