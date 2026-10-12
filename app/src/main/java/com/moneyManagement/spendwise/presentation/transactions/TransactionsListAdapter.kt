package com.moneymanagement.spendwise.presentation.transactions

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moneymanagement.spendwise.R
import com.moneymanagement.spendwise.databinding.ItemTransactionBinding
import com.moneymanagement.spendwise.databinding.ItemTransactionHeaderBinding
import com.moneymanagement.spendwise.domain.model.Transaction
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.utils.CategoryIconProvider
import com.moneymanagement.spendwise.utils.CurrencyFormatter
import com.moneymanagement.spendwise.utils.DateTimeUtils


private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_TRANSACTION = 1

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TransactionListItem>() {
    override fun areItemsTheSame(oldItem: TransactionListItem, newItem: TransactionListItem): Boolean =
        when {
            oldItem is TransactionListItem.Header && newItem is TransactionListItem.Header -> oldItem.label == newItem.label
            oldItem is TransactionListItem.Item && newItem is TransactionListItem.Item -> oldItem.transaction.id == newItem.transaction.id
            else -> false
        }

    override fun areContentsTheSame(oldItem: TransactionListItem, newItem: TransactionListItem): Boolean = oldItem == newItem
}

class TransactionsListAdapter(
    private val currencyCode: () -> String,
                              private val onClick: (Transaction) -> Unit)
    : ListAdapter<TransactionListItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is TransactionListItem.Header -> VIEW_TYPE_HEADER
            is TransactionListItem.Item -> VIEW_TYPE_TRANSACTION
        }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            HeaderViewHolder(ItemTransactionHeaderBinding.inflate(inflater, parent, false))
        } else {
            TransactionViewHolder(ItemTransactionBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = getItem(position)) {
            is TransactionListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is TransactionListItem.Item -> (holder as TransactionViewHolder).bind(item.transaction)
        }
    }
    inner class HeaderViewHolder(private val binding: ItemTransactionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(header: TransactionListItem.Header) {
            binding.headerLabel.text = header.label
        }
    }
    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            val context = binding.root.context
            binding.transactionTitle.text = transaction.title
            binding.transactionSubtitle.text =
                "${transaction.category.name} \u00B7 ${DateTimeUtils.formatTime12h(transaction.time)}"

            val isIncome = transaction.type == TransactionType.INCOME
            binding.transactionAmount.text = CurrencyFormatter.formatSigned(transaction.amount, currencyCode(), isIncome)
            binding.transactionAmount.setTextColor(
                context.getColor(if (isIncome) R.color.color_income else R.color.color_expense)
            )

            binding.categoryIcon.setImageResource(CategoryIconProvider.getIcon(transaction.category.icon))
            runCatching { binding.categoryIconContainer.setCardBackgroundColor(Color.parseColor(transaction.category.color)) }

            binding.root.setOnClickListener { onClick(transaction) }
        }
    }
}