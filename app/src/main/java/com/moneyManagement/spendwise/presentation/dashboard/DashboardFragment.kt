package com.moneymanagement.spendwise.presentation.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.moneymanagement.spendwise.databinding.FragmentDashboardBinding
import com.moneymanagement.spendwise.domain.model.DailyTotal
import com.moneymanagement.spendwise.presentation.common.TransactionAdapter
import com.moneymanagement.spendwise.utils.CurrencyFormatter
import com.moneymanagement.spendwise.utils.DateTimeUtils
import com.moneymanagement.spendwise.utils.UiState
import com.moneymanagement.spendwise.utils.collectOnStart
import com.moneymanagement.spendwise.utils.showIf
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.moneymanagement.spendwise.presentation.dashboard.DashBoardViewModel
import com.moneymanagement.spendwise.presentation.dashboard.DashboardData
import com.moneymanagement.spendwise.presentation.dashboard.DashboardFragmentDirections
import com.moneymanagement.spendwise.R
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.mapIndexed
import kotlin.getValue

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashBoardViewModel by viewModels()
    private lateinit var recentAdapter: TransactionAdapter
    private var currentCurrencyCode = "INR"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        setupClickListeners()
        collectOnStart {
            viewModel.uiState.collect { state -> render(state) }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddTransaction.setOnClickListener {
            findNavController().navigate(
                DashboardFragmentDirections.actionDashboardToAddEditTransaction()
            )
        }
    }

    private fun render(state: UiState<DashboardData>) {
        binding.loadingIndicator.showIf(state is UiState.Loading)
        when (state) {
            is UiState.Loading -> Unit
            is UiState.Empty -> Unit
            is UiState.Error -> Unit
            is UiState.Success -> renderData(state.data)
        }
    }

    private fun renderData(data: DashboardData) {
        currentCurrencyCode = data.currencyCode

        binding.balanceText.text = CurrencyFormatter.format(data.balance, data.currencyCode)
        binding.totalIncomeText.text = CurrencyFormatter.format(data.totalIncome, data.currencyCode)
        binding.totalExpenseText.text =
            CurrencyFormatter.format(data.totalExpense, data.currencyCode)

        val budget = data.monthlyBudget
        if (budget != null && budget.amount > 0) {
            binding.budgetProgressRing.progress = data.budgetPercentUsed.coerceIn(0, 100)
            binding.budgetPercentText.text =
                getString(R.string.dashboard_budget_used_percent, data.budgetPercentUsed)
            binding.budgetSummaryText.text = getString(
                R.string.budget_spent_of_total,
                CurrencyFormatter.format(data.currentMonthExpense, data.currencyCode),
                CurrencyFormatter.format(budget.amount, data.currencyCode)
            )
            binding.setBudgetButton.showIf(false)
            val overBudget = data.currentMonthExpense > budget.amount
            binding.budgetProgressRing.setIndicatorColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (overBudget) R.color.color_expense else R.color.md_primary
                )
            )
        } else {
            binding.budgetProgressRing.progress = 0
            binding.budgetPercentText.text = ""
            binding.budgetSummaryText.text = getString(R.string.dashboard_no_budget_set)
            binding.setBudgetButton.showIf(true)
        }

        recentAdapter.submitList(data.recentTransactions)
        binding.emptyTransactionsText.showIf(data.recentTransactions.isEmpty())
        binding.recentTransactionsRecycler.showIf(data.recentTransactions.isNotEmpty())

        updateChart(data.weeklySpending)
    }

    private fun setupRecyclerView() {
        recentAdapter = TransactionAdapter(
            currencyCode = { currentCurrencyCode },
            onClick = { transaction ->
                findNavController().navigate(
                    DashboardFragmentDirections.actionDashboardFragmentToTransactionDetailsFragment(
                        transaction.id
                    )
                )
            }
        )
        binding.recentTransactionsRecycler.apply {
            adapter = recentAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }
    private fun updateChart(daily: List<DailyTotal>) {
        if (daily.isEmpty()) {
            binding.dashboardMiniChart.clear()
            return
        }
        val entries = daily.mapIndexed { index, day -> Entry(index.toFloat(), day.total.toFloat()) }
        val dataSet = LineDataSet(entries, "Spending").apply {
            color = ContextCompat.getColor(requireContext(), R.color.md_primary)
            setDrawCircles(true)
            circleRadius = 3f
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.md_primary))
            lineWidth = 2f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireContext(), R.color.md_primary)
            fillAlpha = 40
            highLightColor = Color.TRANSPARENT
        }
        binding.dashboardMiniChart.xAxis.valueFormatter =
            IndexAxisValueFormatter(daily.map { DateTimeUtils.toLocalDate(it.dateMillis).dayOfMonth.toString() })
        binding.dashboardMiniChart.data = LineData(dataSet)
        binding.dashboardMiniChart.animateY(400)
        binding.dashboardMiniChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}