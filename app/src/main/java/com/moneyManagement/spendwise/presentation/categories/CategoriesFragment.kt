package com.moneymanagement.spendwise.presentation.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.moneymanagement.spendwise.R
import com.moneymanagement.spendwise.databinding.FragmentCategoriesBinding
import com.moneymanagement.spendwise.domain.model.Category
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.utils.collectOnStart
import com.moneymanagement.spendwise.utils.snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoriesViewModel by viewModels()
    private lateinit var adapter: CategoryAdapter

    private var selectedType: TransactionType = TransactionType.EXPENSE
    private var latestData: CategoriesScreenData = CategoriesScreenData(emptyList(), emptyList())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        adapter = CategoryAdapter(
            onEdit = { category ->
                AddEditCategoryDialogFragment.newInstanceEdit(category)
                    .show(childFragmentManager, "edit_category")
            },
            onDelete = { category -> viewModel.requestDelete(category) }
        )
        binding.categoriesRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CategoriesFragment.adapter
        }

        binding.typeTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedType = if (tab.position == 0) TransactionType.EXPENSE else TransactionType.INCOME
                refreshList()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

        binding.fabAddCategory.setOnClickListener {
            AddEditCategoryDialogFragment.newInstanceAdd(selectedType).show(childFragmentManager, "add_category")
        }

        collectOnStart {
            viewModel.screenData.collect { data ->
                latestData = data
                refreshList()
            }
        }
        collectOnStart { viewModel.events.collect(::handleEvent) }
    }

    private fun refreshList() {
        val list = if (selectedType == TransactionType.EXPENSE) latestData.expenseCategories else latestData.incomeCategories
        adapter.submitList(list)
    }

    private fun handleEvent(event: CategoriesEvent) {
        when (event) {
            CategoriesEvent.Saved -> binding.root.snackbar(getString(R.string.category_saved))
            CategoriesEvent.Deleted -> binding.root.snackbar(getString(R.string.category_deleted))
            CategoriesEvent.CannotDeleteDefault -> binding.root.snackbar(getString(R.string.cannot_delete_default_category))
            is CategoriesEvent.ShowError -> binding.root.snackbar(event.message)
            is CategoriesEvent.ConfirmSimpleDelete -> confirmSimpleDelete(event.category)
            is CategoriesEvent.ConfirmReassignDelete -> confirmReassignDelete(event.category, event.usageCount)
        }
    }

    private fun confirmSimpleDelete(category: Category) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_category_title, category.name))
            .setMessage(R.string.delete_category_empty_message)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.performSimpleDelete(category) }
            .show()
    }

    private fun confirmReassignDelete(category: Category, usageCount: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_category_title, category.name))
            .setMessage(getString(R.string.delete_category_in_use_message, usageCount))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_reassign_and_delete) { _, _ -> viewModel.confirmReassignAndDelete(category) }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
