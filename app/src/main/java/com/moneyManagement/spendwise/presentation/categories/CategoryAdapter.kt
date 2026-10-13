package com.moneymanagement.spendwise.presentation.categories

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moneymanagement.spendwise.databinding.ItemCategoryBinding
import com.moneymanagement.spendwise.domain.model.Category
import com.moneymanagement.spendwise.utils.CategoryIconProvider

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(oldItem: Category, newItem: Category) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Category, newItem: Category) = oldItem == newItem
}

class CategoryAdapter(
    private val onEdit: (Category) -> Unit,
    private val onDelete: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.categoryName.text = category.name
            binding.categoryIcon.setImageResource(CategoryIconProvider.getIcon(category.icon))
            runCatching { binding.categoryAvatarCard.setCardBackgroundColor(Color.parseColor(category.color)) }
            binding.defaultBadge.visibility = if (category.isDefault) View.VISIBLE else View.GONE

            binding.editButton.setOnClickListener { onEdit(category) }
            binding.deleteButton.setOnClickListener { onDelete(category) }
        }
    }
}
