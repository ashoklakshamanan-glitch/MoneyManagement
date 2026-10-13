package com.moneymanagement.spendwise.presentation.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.moneymanagement.spendwise.R
import com.moneymanagement.spendwise.databinding.FragmentTransactionsBinding
import com.moneymanagement.spendwise.presentation.transactions.TransactionsFragmentDirections
import com.moneymanagement.spendwise.utils.UiState
import com.moneymanagement.spendwise.utils.collectOnStart
import com.moneymanagement.spendwise.utils.showIf
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionsViewModel by viewModels()
    private var currentCurrencyCode = "INR"
    private lateinit var adapter: TransactionsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTransactionsBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupToolbarMenu()
        setupClickListeners()
        collectOnStart {
            launch { viewModel.uiState.collect(::updateUi) }
            /*launch { view.filter.collect(::renderFilterBanner) }*/
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionsListAdapter(
            currencyCode = { currentCurrencyCode },
            onClick = {
                findNavController().navigate(
                    TransactionsFragmentDirections.actionTransactionsFragmentToTransactionDetailsFragment(
                        it.id
                    )
                )
            }
        )
        binding.transactionsRecycler.apply{
            layoutManager= LinearLayoutManager(requireContext())
            adapter=this@TransactionsFragment.adapter
        }
    }
    private fun setupToolbarMenu() {
        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        (searchItem.actionView as? SearchView)?.apply {
            queryHint = getString(R.string.search_transactions_hint)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {return true}
                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.onSearchQueryChanged(newText.orEmpty())
                    return true
                }
            })
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_filter -> {
                    TransactionFilterBottomSheet().show(childFragmentManager, "filter_sheet")
                    true
                }
                R.id.action_sort -> {
                    showSortMenu()
                    true
                }
                else -> false
            }
        }
    }
    private fun showSortMenu() {
        val anchor = binding.toolbar.findViewById<View>(R.id.action_sort) ?: binding.toolbar
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_sort_options, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            val option = when (item.itemId) {
                R.id.sort_date_newest -> SortOption.DATE_NEWEST
                R.id.sort_date_oldest -> SortOption.DATE_OLDEST
                R.id.sort_amount_high -> SortOption.AMOUNT_HIGH
                R.id.sort_amount_low -> SortOption.AMOUNT_LOW
                else -> null
            }
            option?.let { viewModel.onSortOptionSelected(it) }
            true
        }
        popup.show()
    }
    private fun setupClickListeners() {
        binding.fabAddTransaction.setOnClickListener {
            findNavController().navigate(TransactionsFragmentDirections.actionTransactionsFragmentToAddEditTransactionFragment())
        }
        binding.clearFiltersButton.setOnClickListener { viewModel.clearFilters() }
    }
    private fun updateUi(state: UiState<TransactionsScreenData>) {
        binding.loadingIndicator.showIf(state is UiState.Loading)
        binding.transactionsRecycler.showIf(state is UiState.Success)
        binding.emptyStateContainer.showIf(state is UiState.Empty)

        when (state) {
            is UiState.Success -> {
                currentCurrencyCode = state.data.currencyCode
                adapter.submitList(state.data.items)
            }
            is UiState.Empty -> {
                val firstRun = viewModel.isTrulyEmptyDatabase()
                binding.emptyStateTitle.text = getString(R.string.transactions_empty_title)
                binding.emptyStateSubtitle.text = getString(
                    if (firstRun) R.string.transactions_empty_first_run else R.string.transactions_empty_subtitle
                )
            }
            else -> Unit
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}