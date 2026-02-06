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

/** @ColorInt値を必要とするMPAndroidChartコンポーネント用のカラーパレット。 */
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
 * CandleStickChartにデフォルトスタイルを適用する。
 *
 * 設定内容：
 * - 説明と凡例を無効化
 * - ピンチズームを有効化、ダブルタップズームと慣性スクロールを無効化
 * - X軸を下部に配置しラベルなし
 * - 右Y軸のみ使用しグリッド線を表示
 * - [ChartTokens.Dimens]からビューポートオフセットを設定
 */
fun CandleStickChart.applyCandleDefaults() = apply {
    description.isEnabled = false
    setDrawGridBackground(false)
    setPinchZoom(true)
    isDoubleTapToZoomEnabled = false
    isDragDecelerationEnabled = false

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
 * 出来高表示用のBarChartにデフォルトスタイルを適用する。
 *
 * 設定内容：
 * - 説明と凡例を無効化
 * - ピンチズームを有効化、ダブルタップズームと慣性スクロールを無効化
 * - 右Y軸のみ使用し最小値を0に設定
 * - X軸を下部に配置し日付ラベルを表示
 * - [ChartTokens.Dimens]からビューポートオフセットを設定
 */
fun BarChart.applyVolumeDefaults() = apply {
    description.isEnabled = false
    setDrawGridBackground(false)
    setPinchZoom(true)
    isDoubleTapToZoomEnabled = false
    isDragDecelerationEnabled = false

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
 * チャートにデータがない時にローディングインジケーターを表示する。
 *
 * @param chart 対象のチャート
 * @param context 文字列リソースアクセス用のContext
 */
fun <T : Chart<*>> showLoading(chart: T, context: Context) {
    chart.clear()
    chart.setNoDataText(context.getString(R.string.loading))
    chart.invalidate()
}

/**
 * 既存のCandleDataSetを更新するか、新規作成する。
 *
 * GC負荷を軽減するため、利用可能な場合は既存のDataSetを再利用する。
 *
 * @param chart 対象のローソク足チャート
 * @param entries ローソク足エントリーのリスト
 * @return 更新または新規作成されたCandleDataSet
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
 * スタイル適用済みのCandleDataSetを作成する。
 *
 * 色：上昇は緑、下落は赤、変化なしは灰色。
 *
 * @param entries ローソク足エントリーのリスト
 * @return 設定済みの[CandleDataSet]
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
 * 既存のBarDataSetを更新するか、新規作成する。
 *
 * GC負荷を軽減するため、利用可能な場合は既存のDataSetを再利用する。
 *
 * @param chart 対象の出来高チャート
 * @param entries バーエントリーのリスト
 * @return 更新または新規作成されたBarDataSet
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
 * 出来高表示用のスタイル適用済みBarDataSetを作成する。
 *
 * @param entries 出来高エントリーのリスト
 * @return 設定済みの[BarDataSet]
 */
fun makeVolumeDataSet(entries: List<BarEntry>) =
    BarDataSet(entries, "Volume").apply {
        setDrawValues(false)
        axisDependency = YAxis.AxisDependency.RIGHT
        color = ChartPalette.VolumeBar
    }


/**
 * チャートの共通X軸設定を構成する。
 *
 * 軸の範囲を設定し、最新データを表示するようスクロールし、間隔ベースのリミットラインを追加する。
 *
 * @param chart 対象のチャート
 * @param totalEntries データエントリーの総数
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
 * 一定間隔でX軸にリミットラインを追加する。
 *
 * @param total エントリーの総数
 * @param stride リミットライン間の間隔（デフォルト：10）
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
 * X軸に日付ラベルを表示するフォーマッターを作成する。
 *
 * 間隔ごとにラベルを表示し、"-"を"/"に変換する（例："2025-09-21"→"2025/09/21"）。
 *
 * @param labels X軸位置に対応する日付文字列のリスト
 * @param stride 表示するラベル間の間隔（デフォルト：10）
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
 * ローソク足チャート用の右Y軸を構成する。
 *
 * 安値/高値からパディング付きで軸範囲を計算する。
 * 軸の境界を5の倍数に丸め、整数ラベルを表示する。
 *
 * @param chart 対象のローソク足チャート
 * @param lows 安値のリスト
 * @param highs 高値のリスト
 * @param padRatio 軸境界のパディング比率（デフォルト：[ChartTokens.Dimens.Y_PAD_RATIO]）
 * @param labelCount Y軸ラベルの数（デフォルト：[ChartTokens.Dimens.Y_LABEL_COUNT]）
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
 * 出来高チャート用の右Y軸を構成する。
 *
 * 読みやすい間隔のためnice stepを使用し、K/M表記でラベルをフォーマットする。
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
 * 現在の軸範囲とラベル設定に基づいてY軸のリミットラインを再生成する。
 *
 * axisMinimum/axisMaximum値を使用し、labelCountとgranularityを尊重する。
 * 重複や欠落を防ぐため浮動小数点精度を処理する。
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