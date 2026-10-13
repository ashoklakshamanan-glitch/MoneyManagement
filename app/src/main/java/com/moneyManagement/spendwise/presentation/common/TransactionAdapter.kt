package com.moneymanagement.spendwise.presentation.common

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moneymanagement.spendwise.R
import com.moneymanagement.spendwise.databinding.ItemTransactionBinding
import com.moneymanagement.spendwise.domain.model.Transaction
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.presentation.transactions.TransactionsListAdapter
import com.moneymanagement.spendwise.utils.CategoryIconProvider
import com.moneymanagement.spendwise.utils.CurrencyFormatter
import com.moneymanagement.spendwise.utils.DateTimeUtils

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Transaction>() {
    override fun areItemsTheSame(
        oldItem: Transaction,
        newItem: Transaction
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: Transaction,
        newItem: Transaction
    ) = oldItem == newItem
}

class TransactionAdapter(
    private val currencyCode: () -> String,
    private val onClick: (transaction: Transaction) -> Unit
) :
    ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionViewHolder {
        val binding =
            ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TransactionViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            val context = binding.root.context
            binding.transactionTitle.text = transaction.title
            binding.transactionSubtitle.text =
                "${transaction.category.name} \u00B7 ${DateTimeUtils.formatTime12h(transaction.time)}"

            val isIncome = transaction.type == TransactionType.INCOME
            binding.transactionAmount.text =
                CurrencyFormatter.formatSigned(transaction.amount, currencyCode(), isIncome)
            binding.transactionAmount.setTextColor(
                context.getColor(if (isIncome) R.color.color_income else R.color.color_expense)
            )

            binding.categoryIcon.setImageResource(CategoryIconProvider.getIcon(transaction.category.icon))
            runCatching {
                binding.categoryIconContainer.setCardBackgroundColor(
                    Color.parseColor(
                        transaction.category.color
                    )
                )
            }

            binding.root.setOnClickListener { onClick(transaction) }
        }
    }

}