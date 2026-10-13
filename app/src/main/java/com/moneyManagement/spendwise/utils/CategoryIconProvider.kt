package com.moneymanagement.spendwise.utils

import androidx.annotation.DrawableRes
import com.moneymanagement.spendwise.R


/**
 * Resolves the string icon key stored on [com.spendwise.app.domain.model.Category] to an
 * actual drawable. Keeping this indirection (instead of storing a @DrawableRes id in the
 * database) means icon resource ids can be safely renamed/renumbered across app versions
 * without corrupting existing rows.
 */
object CategoryIconProvider {

    private val iconMap: Map<String, Int> = mapOf(
        "food" to R.drawable.ic_category_food,
        "transport" to R.drawable.ic_category_transport,
        "shopping" to R.drawable.ic_category_shopping,
       /* "bills" to R.drawable.ic_category_bills,*/
        "entertainment" to R.drawable.ic_category_entertainment,
     /*   "health" to R.drawable.ic_category_health,
        "education" to R.drawable.ic_category_education,
        "rent" to R.drawable.ic_category_rent,
        "travel" to R.drawable.ic_category_travel,
        "groceries" to R.drawable.ic_category_groceries,
        "salary" to R.drawable.ic_category_salary,
        "investment" to R.drawable.ic_category_investment,*/
        "other" to R.drawable.ic_category_other
    )

    /** Icon keys a user can pick from when creating a custom category. */
    val selectableKeys: List<String> = iconMap.keys.toList()

    @DrawableRes
    fun getIcon(key: String): Int = iconMap[key] ?: R.drawable.ic_category_other
}

/** Swatches offered when a user picks a color for a custom category - mirrors the
 *  category_swatch_* entries in colors.xml so the picker and any future themed reuse
 *  of those color resources stay in sync. */
object CategoryColorPalette {
    val swatches: List<String> = listOf(
        "#EF5350", "#FF7043", "#FFA726", "#D4A017", "#66BB6A", "#26A69A",
        "#29B6F6", "#42A5F5", "#5C6BC0", "#AB47BC", "#EC407A", "#78909C"
    )
}
