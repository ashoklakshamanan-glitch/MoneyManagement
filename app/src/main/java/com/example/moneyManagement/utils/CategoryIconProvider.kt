package com.example.moneyManagement.utils

import androidx.annotation.DrawableRes
import com.example.moneyManagement.R

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
        "entertainment" to R.drawable.ic_category_entertainment,
    )

    /** Icon keys a user can pick from when creating a custom category. */
    val selectableKeys: List<String> = iconMap.keys.toList()

    @DrawableRes
    fun getIcon(key: String): Int = iconMap[key] ?: R.drawable.ic_add
}