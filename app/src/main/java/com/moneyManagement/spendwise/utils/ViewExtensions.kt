package com.moneymanagement.spendwise.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Runs [action] every time the fragment view reaches STARTED, cancelling it when the view
 * drops below STARTED - the standard, leak-safe way to collect a StateFlow from a Fragment.
 */
fun Fragment.collectOnStart(action: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            action()
        }
    }
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.showIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

fun Fragment.toast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun View.snackbar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
}

fun Fragment.hideKeyboard() {
    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(view?.windowToken, 0)
}

/** Clears an error the moment the user starts typing again, instead of leaving it stuck. */
fun TextInputLayout.clearErrorOnType(editText: TextInputEditText) {
    editText.doAfterTextChanged { error = null }
}

/** Parses a user-typed amount field, tolerant of an empty string or stray whitespace. */
fun String?.toAmountOrNull(): Double? = this?.trim()?.takeIf { it.isNotEmpty() }?.toDoubleOrNull()