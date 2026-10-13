package com.moneymanagement.spendwise.presentation.categories

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.moneymanagement.spendwise.R
import com.moneymanagement.spendwise.databinding.DialogAddEditCategoryBinding
import com.moneymanagement.spendwise.databinding.ItemColorPickerBinding
import com.moneymanagement.spendwise.databinding.ItemIconPickerBinding
import com.moneymanagement.spendwise.domain.model.Category
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.utils.CategoryColorPalette
import com.moneymanagement.spendwise.utils.CategoryIconProvider

class AddEditCategoryDialogFragment : DialogFragment() {

    private val viewModel: CategoriesViewModel by viewModels({ requireParentFragment() })

    private val existingId: Long? by lazy { requireArguments().getLong(ARG_ID, -1L).takeIf { it != -1L } }
    private val existingName: String by lazy { requireArguments().getString(ARG_NAME).orEmpty() }
    private val existingIcon: String? by lazy { requireArguments().getString(ARG_ICON) }
    private val existingColor: String? by lazy { requireArguments().getString(ARG_COLOR) }
    private val type: TransactionType by lazy {
        TransactionType.valueOf(requireArguments().getString(ARG_TYPE) ?: TransactionType.EXPENSE.name)
    }

    private var selectedIcon: String = ""
    private var selectedColor: String = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        selectedIcon = existingIcon ?: CategoryIconProvider.selectableKeys.first()
        selectedColor = existingColor ?: CategoryColorPalette.swatches.first()

        val binding = DialogAddEditCategoryBinding.inflate(layoutInflater).also {
            it.categoryNameInput.setText(existingName)
        }

        val iconAdapter = IconPickerAdapter(CategoryIconProvider.selectableKeys, selectedIcon) { key ->
            selectedIcon = key
        }
        binding.iconGrid.apply {
            layoutManager = GridLayoutManager(requireContext(), 6)
            adapter = iconAdapter
        }

        val colorAdapter = ColorPickerAdapter(CategoryColorPalette.swatches, selectedColor) { hex ->
            selectedColor = hex
        }
        binding.colorGrid.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = colorAdapter
        }

        val title = if (existingId != null) R.string.edit_category_title else R.string.add_category_title
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(binding.root)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_save, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = binding.categoryNameInput.text?.toString().orEmpty()
                if (name.isBlank()) {
                    binding.categoryNameInputLayout.error = getString(R.string.error_category_name_required)
                    return@setOnClickListener
                }
                val id = existingId
                if (id != null) {
                    val existing = Category(
                        id = id,
                        name = existingName,
                        icon = existingIcon.orEmpty(),
                        color = existingColor.orEmpty(),
                        type = type,
                        isDefault = false
                    )
                    viewModel.updateCategory(existing, name, selectedIcon, selectedColor)
                } else {
                    viewModel.addCategory(name, selectedIcon, selectedColor, type)
                }
                dismiss()
            }
        }
        return dialog
    }

    private class IconPickerAdapter(
        private val icons: List<String>,
        initialSelection: String,
        private val onSelected: (String) -> Unit
    ) : RecyclerView.Adapter<IconPickerAdapter.ViewHolder>() {

        private var selected = initialSelection

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemIconPickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount() = icons.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(icons[position])

        inner class ViewHolder(private val binding: ItemIconPickerBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(key: String) {
                binding.iconImage.setImageResource(CategoryIconProvider.getIcon(key))
                val isSelected = key == selected
                binding.iconCard.strokeWidth = if (isSelected) 4 else 0
                binding.iconCard.setCardBackgroundColor(
                    binding.root.context.getColor(if (isSelected) R.color.md_primary_container else R.color.md_surface_variant)
                )
                binding.root.setOnClickListener {
                    val previous = selected
                    selected = key
                    onSelected(key)
                    notifyItemChanged(icons.indexOf(previous))
                    notifyItemChanged(bindingAdapterPosition)
                }
            }
        }
    }

    private class ColorPickerAdapter(
        private val colors: List<String>,
        initialSelection: String,
        private val onSelected: (String) -> Unit
    ) : RecyclerView.Adapter<ColorPickerAdapter.ViewHolder>() {

        private var selected = initialSelection

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemColorPickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount() = colors.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(colors[position])

        inner class ViewHolder(private val binding: ItemColorPickerBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(hex: String) {
                runCatching { binding.colorCard.setCardBackgroundColor(Color.parseColor(hex)) }
                val isSelected = hex == selected
                binding.checkIcon.visibility = if (isSelected) View.VISIBLE else View.GONE
                binding.root.setOnClickListener {
                    val previous = selected
                    selected = hex
                    onSelected(hex)
                    notifyItemChanged(colors.indexOf(previous))
                    notifyItemChanged(bindingAdapterPosition)
                }
            }
        }
    }

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_NAME = "name"
        private const val ARG_ICON = "icon"
        private const val ARG_COLOR = "color"
        private const val ARG_TYPE = "type"

        fun newInstanceAdd(type: TransactionType): AddEditCategoryDialogFragment = AddEditCategoryDialogFragment().apply {
            arguments = bundleOf(ARG_TYPE to type.name)
        }

        fun newInstanceEdit(category: Category): AddEditCategoryDialogFragment = AddEditCategoryDialogFragment().apply {
            arguments = bundleOf(
                ARG_ID to category.id,
                ARG_NAME to category.name,
                ARG_ICON to category.icon,
                ARG_COLOR to category.color,
                ARG_TYPE to category.type.name
            )
        }
    }
}
