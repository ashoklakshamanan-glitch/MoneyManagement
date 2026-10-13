package com.moneymanagement.spendwise.presentation.analytics

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.moneymanagement.spendwise.R
import com.moneymanagement.spendwise.databinding.FragmentAnalyticsBinding
import com.moneymanagement.spendwise.databinding.ItemStatCardBinding
import com.moneymanagement.spendwise.domain.model.CategoryTotal
import com.moneymanagement.spendwise.domain.model.DailyTotal
import com.moneymanagement.spendwise.domain.model.MonthlyTotal
import com.moneymanagement.spendwise.utils.CurrencyFormatter
import com.moneymanagement.spendwise.utils.DateTimeUtils
import com.moneymanagement.spendwise.utils.UiState
import com.moneymanagement.spendwise.utils.collectOnStart
import com.moneymanagement.spendwise.utils.showIf
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId
import kotlin.getValue


@AndroidEntryPoint
class AnalyticsFragment : Fragment() {
    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AnalyticsViewModel by viewModels()

    private lateinit var statIncome: ItemStatCardBinding
    private lateinit var statExpense: ItemStatCardBinding
    private lateinit var statSavings: ItemStatCardBinding
    private lateinit var statAvgDaily: ItemStatCardBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statIncome = binding.statIncome
        statExpense = binding.statExpense
        statSavings = binding.statSavings
        statAvgDaily = binding.statAvgDaily
        statIncome.statLabel.text = getString(R.string.analytics_total_income)
        statExpense.statLabel.text = getString(R.string.analytics_total_expense)
        statSavings.statLabel.text = getString(R.string.analytics_savings)
        statAvgDaily.statLabel.text = getString(R.string.analytics_avg_daily_spend)

        setupCharts()
        setupRangeChips()

        collectOnStart { viewModel.uiState.collect(::render) }
        collectOnStart { viewModel.selectedRange.collect(::syncRangeChip) }
    }

    private fun setupRangeChips() {
        binding.chipWeek.text = AnalyticsRange.WEEK.label
        binding.chipMonth.text = AnalyticsRange.MONTH.label
        binding.chipLastMonth.text = AnalyticsRange.LAST_MONTH.label
        binding.chipYear.text = AnalyticsRange.YEAR.label
        binding.chipCustom.text = AnalyticsRange.CUSTOM.label

        binding.rangeChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val id = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            when (id) {
                binding.chipWeek.id -> viewModel.onRangeSelected(AnalyticsRange.WEEK)
                binding.chipMonth.id -> viewModel.onRangeSelected(AnalyticsRange.MONTH)
                binding.chipLastMonth.id -> viewModel.onRangeSelected(AnalyticsRange.LAST_MONTH)
                binding.chipYear.id -> viewModel.onRangeSelected(AnalyticsRange.YEAR)
                binding.chipCustom.id -> openCustomRangePicker()
            }
        }
    }

    private fun openCustomRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(getString(R.string.cd_pick_custom_range))
            .build()
        picker.addOnPositiveButtonClickListener { selection ->
            val start = DateTimeUtils.toEpochMillis(
                Instant.ofEpochMilli(selection.first).atZone(ZoneId.of("UTC")).toLocalDate()
            )
            val end = DateTimeUtils.toEpochMillis(
                Instant.ofEpochMilli(selection.second).atZone(ZoneId.of("UTC")).toLocalDate()
            )
            viewModel.onCustomRangeSelected(start, end)
        }
        picker.addOnNegativeButtonClickListener { syncRangeChip(viewModel.selectedRange.value) }
        picker.addOnCancelListener { syncRangeChip(viewModel.selectedRange.value) }
        picker.show(childFragmentManager, "analytics_custom_range")
    }

    private fun syncRangeChip(range: AnalyticsRange) {
        val id = when (range) {
            AnalyticsRange.WEEK -> binding.chipWeek.id
            AnalyticsRange.MONTH -> binding.chipMonth.id
            AnalyticsRange.LAST_MONTH -> binding.chipLastMonth.id
            AnalyticsRange.YEAR -> binding.chipYear.id
            AnalyticsRange.CUSTOM -> binding.chipCustom.id
        }
        if (binding.rangeChipGroup.checkedChipId != id) binding.rangeChipGroup.check(id)
    }

    private fun setupCharts() {
        binding.categoryPieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 62f
            transparentCircleRadius = 66f
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                isWordWrapEnabled = true
                textColor = ContextCompat.getColor(requireContext(), R.color.md_on_surface_variant)
            }
            setNoDataText("")
        }

        binding.dailyLineChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            setNoDataText("")
            axisRight.isEnabled = false
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = ContextCompat.getColor(requireContext(), R.color.md_surface_variant)
                setDrawAxisLine(false)
                textColor = ContextCompat.getColor(requireContext(), R.color.md_on_surface_variant)
            }
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = ContextCompat.getColor(requireContext(), R.color.md_on_surface_variant)
                granularity = 1f
            }
        }

        binding.monthlyBarChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            setNoDataText("")
            axisRight.isEnabled = false
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = ContextCompat.getColor(requireContext(), R.color.md_surface_variant)
                setDrawAxisLine(false)
                axisMinimum = 0f
                textColor = ContextCompat.getColor(requireContext(), R.color.md_on_surface_variant)
            }
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = ContextCompat.getColor(requireContext(), R.color.md_on_surface_variant)
                granularity = 1f
            }
        }
    }

    private fun render(state: UiState<AnalyticsData>) {
        binding.loadingIndicator.showIf(state is UiState.Loading)
        if (state !is UiState.Success) return
        val data = state.data

        statIncome.statValue.text = CurrencyFormatter.format(data.totalIncome, data.currencyCode)
        statExpense.statValue.text = CurrencyFormatter.format(data.totalExpense, data.currencyCode)
        statSavings.statValue.text = CurrencyFormatter.format(data.savings, data.currencyCode)
        statSavings.statValue.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (data.savings >= 0) R.color.color_income else R.color.color_expense
            )
        )
        statAvgDaily.statValue.text =
            CurrencyFormatter.format(data.avgDailySpend, data.currencyCode)
        binding.topCategoryText.text = data.topCategory?.category?.name ?: "\u2014"

        renderPieChart(data.categoryBreakdown, data.currencyCode)
        renderLineChart(data.dailySpending)
        renderBarChart(data.monthlyExpenses)
    }

    private fun renderPieChart(breakdown: List<CategoryTotal>, currencyCode: String) {
        binding.pieChartEmptyText.showIf(breakdown.isEmpty())
        binding.categoryPieChart.showIf(breakdown.isNotEmpty())
        if (breakdown.isEmpty()) {
            binding.categoryPieChart.clear()
            return
        }

        val entries = breakdown.map { PieEntry(it.total.toFloat(), it.category.name) }
        val colors =
            breakdown.map { runCatching { Color.parseColor(it.category.color) }.getOrDefault(Color.GRAY) }
        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            sliceSpace = 2f
            valueTextSize = 11f
            valueTextColor = Color.WHITE
            valueFormatter =
                com.github.mikephil.charting.formatter.PercentFormatter(binding.categoryPieChart)
        }
        binding.categoryPieChart.data = PieData(dataSet)
        binding.categoryPieChart.animateY(500, Easing.EaseInOutQuad)
        binding.categoryPieChart.invalidate()
    }

    private fun renderLineChart(daily: List<DailyTotal>) {
        if (daily.isEmpty()) {
            binding.dailyLineChart.clear()
            return
        }
        val entries = daily.mapIndexed { index, d -> Entry(index.toFloat(), d.total.toFloat()) }
        val dataSet = LineDataSet(entries, "Daily spending").apply {
            color = ContextCompat.getColor(requireContext(), R.color.md_primary)
            lineWidth = 2f
            setDrawCircles(daily.size <= 31)
            circleRadius = 2.5f
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.md_primary))
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireContext(), R.color.md_primary)
            fillAlpha = 35
        }
        binding.dailyLineChart.xAxis.valueFormatter =
            IndexAxisValueFormatter(daily.map { DateTimeUtils.toLocalDate(it.dateMillis).dayOfMonth.toString() })
        binding.dailyLineChart.data = LineData(dataSet)
        binding.dailyLineChart.animateX(500)
        binding.dailyLineChart.invalidate()
    }

    private fun renderBarChart(monthly: List<MonthlyTotal>) {
        if (monthly.isEmpty()) {
            binding.monthlyBarChart.clear()
            return
        }
        val entries =
            monthly.mapIndexed { index, m -> BarEntry(index.toFloat(), m.total.toFloat()) }
        val dataSet = BarDataSet(entries, "Monthly expenses").apply {
            color = ContextCompat.getColor(requireContext(), R.color.md_primary)
            setDrawValues(false)
        }
        binding.monthlyBarChart.xAxis.valueFormatter =
            IndexAxisValueFormatter(monthly.map { DateTimeUtils.formatMonthShort(it.yearMonth) })
        binding.monthlyBarChart.data = BarData(dataSet).apply { barWidth = 0.6f }
        binding.monthlyBarChart.animateY(500)
        binding.monthlyBarChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}