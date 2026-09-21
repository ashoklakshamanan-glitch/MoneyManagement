package com.example.moneyManagement.presentation.addTransaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.moneyManagement.databinding.FragmentAddEditTransactionBinding
import com.example.moneyManagement.domain.model.Category
import com.example.moneyManagement.domain.model.PaymentMethod
import com.example.moneyManagement.domain.model.TransactionType
import com.example.moneyManagement.utils.CategoryIconProvider
import com.example.moneyManagement.utils.DateTimeUtils
import com.example.moneyManagement.utils.collectOnStart
import dagger.hilt.android.AndroidEntryPoint
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

        collectOnStart { viewModel.categories.collect(::renderCategories) }

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
        binding.typeToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val type = if (checkedId == binding.typeIncomeButton.id) TransactionType.INCOME else TransactionType.EXPENSE
            if (type != viewModel.formState.value.type) viewModel.onTypeChanged(type)
        }
        binding.categoryDropdown.setOnItemClickListener { _, _, position, _ ->
            categoriesCache.getOrNull(position)?.let { viewModel.onCategorySelected(it.id) }
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
                androidx.core.content.ContextCompat.getDrawable(requireContext(), CategoryIconProvider.getIcon(it.icon))
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}