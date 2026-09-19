package com.example.moneyManagement.presentation.addTransaction

import androidx.lifecycle.ViewModel
import com.example.moneyManagement.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(private val repository: TransactionRepository): ViewModel (){


}