package com.moneymanagement.spendwise.presentation.transactiondetails

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.moneymanagement.spendwise.R
import com.moneymanagement.spendwise.databinding.FragmentTransactionDetailsBinding
import com.moneymanagement.spendwise.databinding.ItemDetailRowBinding
import com.moneymanagement.spendwise.domain.model.Transaction
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.utils.CategoryIconProvider
import com.moneymanagement.spendwise.utils.CurrencyFormatter
import com.moneymanagement.spendwise.utils.DateTimeUtils
import com.moneymanagement.spendwise.utils.UiState
import com.moneymanagement.spendwise.utils.collectOnStart
import com.moneymanagement.spendwise.utils.showIf
import com.moneymanagement.spendwise.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class TransactionDetailsFragment : Fragment() {

    private var _binding: FragmentTransactionDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionDetailsViewModel by viewModels()

    private lateinit var rowType: ItemDetailRowBinding
    private lateinit var rowCategory: ItemDetailRowBinding
    private lateinit var rowDate: ItemDetailRowBinding
    private lateinit var rowTime: ItemDetailRowBinding
    private lateinit var rowPaymentMethod: ItemDetailRowBinding
    private lateinit var rowNotes: ItemDetailRowBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rowType = binding.rowType
        rowCategory = binding.rowCategory
        rowDate = binding.rowDate
        rowTime = binding.rowTime
        rowPaymentMethod = binding.rowPaymentMethod
        rowNotes = binding.rowNotes
        setRowLabels()

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.setOnMenuItemClickListener { item ->
            val transaction = (viewModel.uiState.value as? UiState.Success)?.data?.transaction
            when (item.itemId) {
                R.id.action_edit -> {
                    transaction?.let { navigateToEdit(it.id) }
                    true
                }
                R.id.action_delete -> {
                    transaction?.let { confirmDelete(it) }
                    true
                }
                else -> false
            }
        }
        binding.editButton.setOnClickListener {
            (viewModel.uiState.value as? UiState.Success)?.data?.transaction?.let { navigateToEdit(it.id) }
        }
        binding.deleteButton.setOnClickListener {
            (viewModel.uiState.value as? UiState.Success)?.data?.transaction?.let { confirmDelete(it) }
        }

        collectOnStart { viewModel.uiState.collect(::render) }
        collectOnStart { viewModel.events.collect(::handleEvent) }
    }

    private fun setRowLabels() {
        rowType.rowLabel.text = getString(R.string.label_type)
        rowCategory.rowLabel.text = getString(R.string.label_category)
        rowDate.rowLabel.text = getString(R.string.label_date)
        rowTime.rowLabel.text = getString(R.string.label_time)
        rowPaymentMethod.rowLabel.text = getString(R.string.label_payment_method)
        rowNotes.rowLabel.text = getString(R.string.label_notes)
    }

    private fun navigateToEdit(transactionId: Long) {
        findNavController().navigate(
            TransactionDetailsFragmentDirections.actionTransactionDetailsToAddEditTransaction(transactionId)
        )
    }

    private fun confirmDelete(transaction: Transaction) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_transaction_title)
            .setMessage(getString(R.string.delete_transaction_message, transaction.title))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.deleteTransaction() }
            .show()
    }

    private fun render(state: UiState<TransactionDetailsData>) {
        binding.loadingIndicator.showIf(state is UiState.Loading)
        if (state is UiState.Success) renderData(state.data)
    }

    private fun renderData(data: TransactionDetailsData) {
        val transaction = data.transaction
        val isIncome = transaction.type == TransactionType.INCOME

        binding.amountText.text = CurrencyFormatter.formatSigned(transaction.amount, data.currencyCode, isIncome)
        binding.amountText.setTextColor(
            requireContext().getColor(if (isIncome) R.color.color_income else R.color.color_expense)
        )
        binding.titleText.text = transaction.title

        binding.categoryIcon.setImageResource(CategoryIconProvider.getIcon(transaction.category.icon))
        runCatching { binding.categoryAvatarCard.setCardBackgroundColor(Color.parseColor(transaction.category.color)) }

        rowType.rowValue.text = if (isIncome) getString(R.string.type_income) else getString(R.string.type_expense)
        rowCategory.rowValue.text = transaction.category.name
        rowDate.rowValue.text = DateTimeUtils.formatDate(transaction.date)
        rowTime.rowValue.text = DateTimeUtils.formatTime12h(transaction.time)
        rowPaymentMethod.rowValue.text = transaction.paymentMethod.displayName
        rowNotes.rowValue.text = transaction.notes.ifBlank { "\u2014" }
    }

    private fun handleEvent(event: TransactionDetailsEvent) {
        when (event) {
            TransactionDetailsEvent.Deleted -> {
                toast(getString(R.string.transaction_deleted))
                findNavController().navigateUp()
            }
            is TransactionDetailsEvent.ShowError -> toast(event.message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

