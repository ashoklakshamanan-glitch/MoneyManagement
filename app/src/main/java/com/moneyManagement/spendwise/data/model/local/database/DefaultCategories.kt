package com.moneymanagement.spendwise.data.model.local.database

import com.moneymanagement.spendwise.data.model.local.entity.CategoryEntity

/**
 * Seeded once, on first database creation (see [AppDatabase]'s callback). Icon keys map
 * to drawables via [com.spendwise.app.utils.CategoryIconProvider]; colors are hex strings
 * consumed directly by the UI layer.
 */
object DefaultCategories {

    val list: List<CategoryEntity> = listOf(
        CategoryEntity(
            name = "Food",
            icon = "food",
            color = "#FF7043",
            type = "EXPENSE",
            isDefault = true
        ),
        CategoryEntity(
            name = "Transport",
            icon = "transport",
            color = "#42A5F5",
            type = "EXPENSE",
            isDefault = true
        ),
        CategoryEntity(
            name = "Shopping",
            icon = "shopping",
            color = "#AB47BC",
            type = "EXPENSE",
            isDefault = true
        ),
        CategoryEntity(
            name = "Bills",
            icon = "bills",
            color = "#EF5350",
            type = "EXPENSE",
            isDefault = true
        ),
        CategoryEntity(
            name = "Entertainment",
            icon = "entertainment",
            color = "#EC407A",
            type = "EXPENSE",
            isDefault = true
        ),
        CategoryEntity(
            name = "Health",
            icon = "health",
            color = "#26A69A",
            type = "EXPENSE",
            isDefault = true
        ),
        CategoryEntity(
            name = "Education",
            icon = "education",
            color = "#5C6BC0",
            type = "EXPENSE",
            isDefault = true
        ),
        CategoryEntity(
            name = "Rent",
            icon = "rent",
            color = "#8D6E63",
            type = "EXPENSE",
            isDefault = true
        ),
        CategoryEntity(
            name = "Travel",
            icon = "travel",
            color = "#29B6F6",
            type = "EXPENSE",
            isDefault = true
        ),
        CategoryEntity(
            name = "Groceries",
            icon = "groceries",
            color = "#66BB6A",
            type = "EXPENSE",
            isDefault = true
        ),
        CategoryEntity(
            name = "Other",
            icon = "other",
            color = "#78909C",
            type = "EXPENSE",
            isDefault = true
        ),
        CategoryEntity(
            name = "Salary",
            icon = "salary",
            color = "#43A047",
            type = "INCOME",
            isDefault = true
        ),
        CategoryEntity(
            name = "Investment",
            icon = "investment",
            color = "#00897B",
            type = "INCOME",
            isDefault = true
        ),
        CategoryEntity(
            name = "Other",
            icon = "other",
            color = "#78909C",
            type = "INCOME",
            isDefault = true
        )
    )
}
