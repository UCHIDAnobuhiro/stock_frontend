package com.example.stock.feature.chart.ui.chart

import android.content.Context
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt
import com.example.stock.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.formatter.ValueFormatter

/** Color palette for MPAndroidChart components requiring @ColorInt values. */
object ChartPalette {
    val Grid = "#E5E7EB".toColorInt()
    val CandleUp = "#2ECC71".toColorInt()
    val CandleDown = "#E74C3C".toColorInt()
    val CandleNeutral = "#95A5A6".toColorInt()
    val VolumeBar = "#3498DB".toColorInt()

    fun labelColor(context: Context): Int {
        val night = context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return if (night == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            "#FFFFFF".toColorInt()
        } else {
            "#000000".toColorInt()
        }
    }
}

/**
 * Applies default styling to a CandleStickChart.
 *
 * Configuration includes:
 * - Disables description and legend
 * - Enables pinch zoom, disables double-tap zoom
 * - Positions X-axis at bottom without labels
 * - Uses only right Y-axis with grid lines
 * - Sets viewport offsets from [ChartTokens.Dimens]
 */
fun CandleStickChart.applyCandleDefaults() = apply {
    description.isEnabled = false
    setDrawGridBackground(false)
    setPinchZoom(true)
    isDoubleTapToZoomEnabled = false

    xAxis.apply {
        isEnabled = true
        position = XAxis.XAxisPosition.BOTTOM
        setDrawLabels(false)
        setDrawAxisLine(false)
        setDrawGridLines(false)
        granularity = 1f
        textColor = ChartPalette.labelColor(context)
    }

    axisLeft.isEnabled = false
    axisRight.apply {
        isEnabled = true
        setDrawGridLines(true)
        setLabelCount(5, false)
        textColor = ChartPalette.labelColor(context)
    }

    legend.isEnabled = false
    setViewPortOffsets(
        ChartTokens.Dimens.CANDLE_LEFT,
        ChartTokens.Dimens.CANDLE_TOP,
        ChartTokens.Dimens.CANDLE_RIGHT,
        ChartTokens.Dimens.CANDLE_BOTTOM
    )
}

/**
 * Applies default styling to a BarChart for volume display.
 *
 * Configuration includes:
 * - Disables description and legend
 * - Enables pinch zoom, disables double-tap zoom
 * - Uses only right Y-axis with minimum at 0
 * - Positions X-axis at bottom with date labels
 * - Sets viewport offsets from [ChartTokens.Dimens]
 */
fun BarChart.applyVolumeDefaults() = apply {
    description.isEnabled = false
    setDrawGridBackground(false)
    setPinchZoom(true)
    isDoubleTapToZoomEnabled = false

    xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        setDrawGridLines(false)
        granularity = 1f
        textColor = ChartPalette.labelColor(context)
    }

    axisLeft.isEnabled = false
    axisRight.apply {
        isEnabled = true
        axisMinimum = 0f
        setDrawGridLines(false)
        setLabelCount(5, true)
        textColor = ChartPalette.labelColor(context)
    }

    legend.isEnabled = false
    setViewPortOffsets(
        ChartTokens.Dimens.VOLUME_LEFT,
        ChartTokens.Dimens.VOLUME_TOP,
        ChartTokens.Dimens.VOLUME_RIGHT,
        ChartTokens.Dimens.VOLUME_BOTTOM
    )
}

/**
 * Displays a loading indicator when the chart has no data.
 *
 * @param chart Target chart
 * @param context Context for accessing string resources
 */
fun <T : Chart<*>> showLoading(chart: T, context: Context) {
    chart.clear()
    chart.setNoDataText(context.getString(R.string.loading))
    chart.invalidate()
}

/**
 * Updates an existing CandleDataSet or creates a new one.
 *
 * Reuses existing DataSet when available to reduce GC pressure.
 *
 * @param chart Target candlestick chart
 * @param entries List of candle entries
 * @return The updated or newly created CandleDataSet
 */
fun updateOrCreateCandleData(
    chart: CandleStickChart,
    entries: List<CandleEntry>
): CandleDataSet {
    val data = chart.data
    val ds = if (data != null && data.dataSetCount > 0) {
        (data.getDataSetByIndex(0) as CandleDataSet).also { it.values = entries }
    } else {
        makeCandleDataSet(entries).also { chart.data = CandleData(it) }
    }
    chart.data.notifyDataChanged()
    chart.notifyDataSetChanged()
    return ds
}

/**
 * Creates a styled CandleDataSet.
 *
 * Colors: green for up, red for down, gray for neutral.
 *
 * @param entries List of candle entries
 * @return Configured [CandleDataSet]
 */
fun makeCandleDataSet(entries: List<CandleEntry>) =
    CandleDataSet(entries, "Price").apply {
        setDrawValues(false)
        shadowColorSameAsCandle = true
        decreasingColor = ChartPalette.CandleDown
        decreasingPaintStyle = Paint.Style.FILL
        increasingColor = ChartPalette.CandleUp
        increasingPaintStyle = Paint.Style.FILL
        neutralColor = ChartPalette.CandleNeutral
    }

/**
 * Updates an existing BarDataSet or creates a new one.
 *
 * Reuses existing DataSet when available to reduce GC pressure.
 *
 * @param chart Target volume chart
 * @param entries List of bar entries
 * @return The updated or newly created BarDataSet
 */
fun updateOrCreateBarData(
    chart: BarChart,
    entries: List<BarEntry>,
): BarDataSet {
    val data = chart.data
    val ds = if (data != null && data.dataSetCount > 0) {
        (data.getDataSetByIndex(0) as BarDataSet).also { it.values = entries }
    } else {
        makeVolumeDataSet(entries).also { chart.data = BarData(it) }
    }
    chart.data.notifyDataChanged()
    chart.notifyDataSetChanged()
    return ds
}

/**
 * Creates a styled BarDataSet for volume display.
 *
 * @param entries List of volume entries
 * @return Configured [BarDataSet]
 */
fun makeVolumeDataSet(entries: List<BarEntry>) =
    BarDataSet(entries, "Volume").apply {
        setDrawValues(false)
        axisDependency = YAxis.AxisDependency.RIGHT
        color = ChartPalette.VolumeBar
    }


/**
 * Configures common X-axis settings for charts.
 *
 * Sets axis bounds, scrolls to show recent data, and adds stride-based limit lines.
 *
 * @param chart Target chart
 * @param totalEntries Total number of data entries
 */
fun setupXAxisCommon(
    chart: BarLineChartBase<*>,
    totalEntries: Int
) {
    chart.xAxis.axisMinimum = -ChartTokens.Dimens.X_BOUND_MARGIN
    chart.xAxis.axisMaximum = chart.data.xMax + ChartTokens.Dimens.X_BOUND_MARGIN

    if (totalEntries > ChartTokens.Dimens.VISIBLE_COUNT) {
        chart.setVisibleXRangeMaximum(ChartTokens.Dimens.VISIBLE_COUNT)
        chart.moveViewToX(chart.data.xMax - ChartTokens.Dimens.VISIBLE_COUNT + ChartTokens.Dimens.X_BOUND_MARGIN)
    } else {
        chart.moveViewToX(chart.data.xMax + ChartTokens.Dimens.X_BOUND_MARGIN)
    }

    chart.xAxis.refreshStrideLimitLines(totalEntries)
}

/**
 * Adds limit lines to X-axis at regular intervals.
 *
 * @param total Total number of entries
 * @param stride Interval between limit lines (default: 10)
 */
fun XAxis.refreshStrideLimitLines(
    total: Int,
    stride: Int = ChartTokens.Dimens.X_STRIDE,
    lineWidth: Float = 1f,
    lineColor: Int = ChartPalette.Grid
) {
    setDrawGridLines(false)
    setDrawLimitLinesBehindData(true)
    removeAllLimitLines()
    for (i in 0 until total step stride) {
        addLimitLine(
            LimitLine(i.toFloat()).apply {
                this.lineWidth = lineWidth
                this.lineColor = lineColor
            }
        )
    }
}

/**
 * Creates a formatter that displays date labels on the X-axis.
 *
 * Shows labels at stride intervals, converting "-" to "/" (e.g., "2025-09-21" to "2025/09/21").
 *
 * @param labels List of date strings corresponding to X-axis positions
 * @param stride Interval between displayed labels (default: 10)
 */
fun makeDateAxisFormatter(
    labels: List<String>,
    stride: Int = ChartTokens.Dimens.X_STRIDE
): ValueFormatter {
    return object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val i = value.toInt()
            return if (i in labels.indices && i % stride == 0) {
                labels[i].replace('-', '/')
            } else ""
        }
    }
}

/**
 * Configures the right Y-axis for candlestick charts.
 *
 * Calculates axis range from low/high values with padding.
 * Rounds axis bounds to multiples of 5 and displays integer labels.
 *
 * @param chart Target candlestick chart
 * @param lows List of low prices
 * @param highs List of high prices
 * @param padRatio Padding ratio for axis bounds (default: [ChartTokens.Dimens.Y_PAD_RATIO])
 * @param labelCount Number of Y-axis labels (default: [ChartTokens.Dimens.Y_LABEL_COUNT])
 */
fun setupRightAxisForCandle(
    chart: CandleStickChart,
    lows: List<Double>,
    highs: List<Double>,
    padRatio: Float = ChartTokens.Dimens.Y_PAD_RATIO,
    labelCount: Int = ChartTokens.Dimens.Y_LABEL_COUNT
) {
    if (lows.isEmpty() || highs.isEmpty()) return

    val minLow = lows.minOrNull()!!.toFloat()
    val maxHigh = highs.maxOrNull()!!.toFloat()

    val rawRange = (maxHigh - minLow).takeIf { it > 0f } ?: (maxHigh * 0.01f).coerceAtLeast(0.01f)
    val pad = rawRange * padRatio

    val rawMin = (minLow - pad).coerceAtLeast(0f)
    val rawMax = maxHigh + pad

    val targetStep = (rawMax - rawMin) / (labelCount - 1)
    var step = (kotlin.math.ceil(targetStep / 5f) * 5f).coerceAtLeast(5f)

    val axisMin = kotlin.math.floor(rawMin / 5f) * 5f
    var axisMax = axisMin + step * (labelCount - 1)

    while (axisMax < rawMax) {
        step += 5f
        axisMax = axisMin + step * (labelCount - 1)
    }

    chart.axisRight.apply {
        axisMinimum = axisMin
        axisMaximum = axisMax

        setLabelCount(labelCount, true)

        granularity = step
        isGranularityEnabled = true

        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = value.toInt().toString()
        }
    }
}

/**
 * Configures the right Y-axis for volume charts.
 *
 * Uses nice step values for readable intervals and formats labels with K/M notation.
 */
fun setupRightAxisForVolume(
    chart: BarChart,
    volEntries: List<BarEntry>,
    labelCount: Int = ChartTokens.Dimens.Y_LABEL_COUNT,
    min: Float = 0f
) {
    val dataMax = volEntries.maxOfOrNull { it.y } ?: 0f
    val rawStep = if (dataMax <= min) 1f else (dataMax - min) / (labelCount - 1)
    val step = niceStep(rawStep)
    val axisMax = (min + step * (labelCount - 1)).coerceAtLeast(dataMax)

    chart.axisRight.apply {
        axisMinimum = min
        axisMaximum = axisMax
        setLabelCount(labelCount, true)
        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = formatCompact(value)
        }
    }
}

/**
 * Regenerates limit lines for Y-axis based on current axis range and label settings.
 *
 * Uses axisMinimum/axisMaximum values and respects labelCount and granularity.
 * Handles floating-point precision to prevent duplicate or missing lines.
 */
fun YAxis.refreshGridLimitLinesFromAxis(
    labelCountOverride: Int? = null,
    stepOverride: Float? = null,
    lineWidth: Float = 1f,
    @ColorInt lineColor: Int = ChartPalette.Grid
) {
    setDrawGridLines(false)
    setDrawLimitLinesBehindData(true)
    removeAllLimitLines()

    val min = axisMinimum
    val max = axisMaximum
    if (!min.isFinite() || !max.isFinite() || max <= min) return

    val labels = (labelCountOverride ?: this.labelCount).coerceAtLeast(2)
    val step = when {
        stepOverride != null && stepOverride > 0f -> stepOverride
        isGranularityEnabled && granularity > 0f -> granularity
        else -> (max - min) / (labels - 1)
    }.coerceAtLeast(1e-6f)

    val eps = (max - min) * 1e-6f
    if (labelCountOverride != null) {
        var v = min
        repeat(labels) {
            addLimitLine(
                LimitLine(v).apply {
                    this.lineWidth = lineWidth
                    this.lineColor = lineColor
                }
            )
            v += step
        }
    } else {
        var v = min
        var guard = 0
        while (v <= max + eps && guard < 1000) {
            addLimitLine(
                LimitLine(v).apply {
                    this.lineWidth = lineWidth
                    this.lineColor = lineColor
                }
            )
            v += step
            guard++
        }
    }
}