package com.example.stock.ui.chart

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

/** MPAndroidChartが @ColorInt を要求する部分のためのパレット（Hex文字列） */
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
            "#FFFFFF".toColorInt()   // ダーク時は白
        } else {
            "#000000".toColorInt()   // ライト時は黒
        }
    }
}

/**
 * CandleStickChart（ローソク足チャート）のデフォルトスタイルを適用する。
 *
 * - 説明ラベルや凡例を非表示
 * - グリッド背景を無効化
 * - ピンチズーム有効／ダブルタップズーム無効
 * - X軸を下部に配置（ラベルは非表示・グリッド線非表示）
 * - 左Y軸を無効化し、右Y軸のみ利用（ラベル数5、グリッド線ON）
 * - 描画領域の余白は [ChartTokens.Dimens] に従って設定
 */
fun CandleStickChart.applyCandleDefaults() = apply {
    // チャート全体の設定
    description.isEnabled = false
    setDrawGridBackground(false)
    setPinchZoom(true)
    isDoubleTapToZoomEnabled = false

    // X軸の設定
    xAxis.apply {
        isEnabled = true
        position = XAxis.XAxisPosition.BOTTOM
        setDrawLabels(false)
        setDrawAxisLine(false)
        setDrawGridLines(false)
        granularity = 1f
        textColor = ChartPalette.labelColor(context)
    }

    // Y軸の設定
    axisLeft.isEnabled = false
    axisRight.apply {
        isEnabled = true
        setDrawGridLines(true)
        setLabelCount(5, false)
        textColor = ChartPalette.labelColor(context)
    }

    // 凡例と余白
    legend.isEnabled = false
    setViewPortOffsets(
        ChartTokens.Dimens.CANDLE_LEFT,
        ChartTokens.Dimens.CANDLE_TOP,
        ChartTokens.Dimens.CANDLE_RIGHT,
        ChartTokens.Dimens.CANDLE_BOTTOM
    )
}

/**
 * BarChart（出来高チャート）のデフォルトスタイルを適用する。
 *
 * - 説明ラベルや凡例を非表示
 * - グリッド背景を無効化
 * - ピンチズーム有効／ダブルタップズーム無効
 * - 左Y軸を無効化し、右Y軸のみ利用（最小値0、ラベル数5、グリッド線OFF）
 * - X軸を下部に配置（グリッド線非表示）
 * - 描画領域の余白は [ChartTokens.Dimens] に従って設定
 */
fun BarChart.applyVolumeDefaults() = apply {
    // チャート全体の設定
    description.isEnabled = false
    setDrawGridBackground(false)
    setPinchZoom(true)
    isDoubleTapToZoomEnabled = false

    // X軸の設定
    xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        setDrawGridLines(false)
        granularity = 1f
        textColor = ChartPalette.labelColor(context)
    }

    // Y軸の設定
    axisLeft.isEnabled = false
    axisRight.apply {
        isEnabled = true
        axisMinimum = 0f
        setDrawGridLines(false)
        setLabelCount(5, true)
        textColor = ChartPalette.labelColor(context)
    }

    // 凡例と余白
    legend.isEnabled = false
    setViewPortOffsets(
        ChartTokens.Dimens.VOLUME_LEFT,
        ChartTokens.Dimens.VOLUME_TOP,
        ChartTokens.Dimens.VOLUME_RIGHT,
        ChartTokens.Dimens.VOLUME_BOTTOM
    )
}

/**
 * チャートがまだデータを持っていないときに「Loading...」表示を行う。
 *
 * - 既存のデータをクリア
 * - リソース文字列 `R.string.loading` を NoDataText として表示
 * - invalidate() を呼んで即時再描画
 *
 * @param chart  対象のチャート
 * @param context コンテキスト（文字列リソース取得に使用）
 */
fun <T : Chart<*>> showLoading(chart: T, context: Context) {
    chart.clear()
    chart.setNoDataText(context.getString(R.string.loading))
    chart.invalidate()
}

/**
 * CandleStickChart の DataSet を「再利用して更新」または「新規作成」するヘルパー。
 *
 * - 既存の DataSet があれば values を差し替えて再利用（GC を抑制）
 * - なければ `makeCandleDataSet(entries)` で新規作成し、`chart.data` にセット
 * - 変更後は `notifyDataSetChanged()` を呼び出して再描画をトリガ
 *
 * ※ DataSet のインデックスは 0 を前提（単一 DataSet 構成）。
 *
 * @param chart 対象のローソク足チャート
 * @param entries ローソク足エントリー一覧
 * @return 更新または作成された CandleDataSet
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
 * ローソク足用の DataSet を生成するヘルパー。
 *
 * - 値ラベルは非表示
 * - 陰線（下落）は赤塗り、陽線（上昇）は緑塗り、中立はグレー
 * - ヒゲの色はロウソクと同じ
 * @param entries ローソク足のデータリスト
 * @return 設定済みの [CandleDataSet]
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
 * BarChart の DataSet を「再利用して更新」または「新規作成」するヘルパー。
 *
 * - 既存の DataSet があれば values を差し替えて再利用（GC を抑制）
 * - なければ `makeVolumeDataSet(entries)` で新規作成し、`chart.data` にセット
 * - 変更後は `notifyDataSetChanged()` を呼び出して再描画をトリガ
 *
 * ※ DataSet のインデックスは 0 を前提（単一 DataSet 構成）。
 *
 * @param chart 対象の出来高チャート
 * @param entries 出来高エントリー一覧
 * @return 更新または作成された BarDataSet
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
 * 出来高（BarChart）用の DataSet を生成するヘルパー。
 *
 * - 値ラベルは非表示
 * - 右Y軸に依存（出来高は右軸で描画される前提）
 * - 棒の色は [ChartPalette.VolumeBar] を使用
 *
 * @param entries 出来高エントリー一覧
 * @return 設定済みの [BarDataSet]
 */
fun makeVolumeDataSet(entries: List<BarEntry>) =
    BarDataSet(entries, "Volume").apply {
        setDrawValues(false)
        axisDependency = YAxis.AxisDependency.RIGHT
        color = ChartPalette.VolumeBar
    }


/**
 * X軸の共通設定を行う。
 *
 * - 表示範囲の最小値/最大値をエントリー数に基づいて設定
 * - データが多い場合は最新の N 本にスクロールして表示
 * - stride ごとに補助線を追加（内部で [refreshStrideLimitLines] を呼ぶ）
 *
 * @param chart 対象のチャート（ローソク足/出来高など）
 * @param totalEntries データ件数（エントリー数）
 */
fun setupXAxisCommon(
    chart: BarLineChartBase<*>,
    totalEntries: Int
) {
    //　最小値と最大値を設定
    chart.xAxis.axisMinimum = -ChartTokens.Dimens.X_BOUND_MARGIN
    chart.xAxis.axisMaximum = chart.data.xMax + ChartTokens.Dimens.X_BOUND_MARGIN

    // データ数に応じてスクロール制御
    if (totalEntries > ChartTokens.Dimens.VISIBLE_COUNT) {
        chart.setVisibleXRangeMaximum(ChartTokens.Dimens.VISIBLE_COUNT)
        chart.moveViewToX(chart.data.xMax - ChartTokens.Dimens.VISIBLE_COUNT + ChartTokens.Dimens.X_BOUND_MARGIN)
    } else {
        chart.moveViewToX(chart.data.xMax + ChartTokens.Dimens.X_BOUND_MARGIN)
    }

    // グリッド線を設定
    chart.xAxis.refreshStrideLimitLines(totalEntries)
}

/**
 * X軸に stride ごとに補助線を追加する。
 *
 * @param total 全エントリー数
 * @param stride 間隔（デフォルト10）
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
 * X軸に日付ラベルを表示するフォーマッタを生成する。
 *
 * - 指定した stride ごとにラベルを表示（例: 10本ごと）
 * - ラベル文字列内の `-` を `/` に置換して表示（例: "2025-09-21" → "2025/09/21"）
 * - stride に満たない位置やインデックス外は空文字を返す
 *
 * @param labels X軸に対応する日付リスト（エントリー数と同じ長さを想定）
 * @param stride 何本ごとにラベルを表示するか（デフォルト = 10）
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
 * ローソク足チャートの右Y軸をセットアップする。
 *
 * - 安値・高値リストから軸範囲を計算し、上下に余白を追加する
 * - 軸最小値／最大値は必ず 5 の倍数に丸められる（マイナス値は禁止）
 * - ラベル数は固定（デフォルト 5 本）
 * - 目盛り間隔（granularity）は 5 の倍数になるように調整される
 * - 値フォーマットは整数表示（小数点なし）
 *
 * @param chart 対象のローソク足チャート
 * @param lows 各足の安値リスト
 * @param highs 各足の高値リスト
 * @param padRatio 上下に追加する余白の比率（デフォルトは [ChartTokens.Dimens.Y_PAD_RATIO]）
 * @param labelCount Y 軸ラベルの数（デフォルトは [ChartTokens.Dimens.Y_LABEL_COUNT]）
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

    // レンジと余白を計算
    val rawRange = (maxHigh - minLow).takeIf { it > 0f } ?: (maxHigh * 0.01f).coerceAtLeast(0.01f)
    val pad = rawRange * padRatio

    // 軸の候補値(マイナスは0で止める)
    val rawMin = (minLow - pad).coerceAtLeast(0f) // マイナス禁止
    val rawMax = maxHigh + pad

    // ステップ幅を5の倍数に切りあげ
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
 * 出来高チャートの右Y軸をセットアップする。
 *
 * - 最小値は常に 0
 * - 最大値は「niceStep」による見やすい刻み幅で調整
 * - ラベル数は固定（デフォルト5）
 * - 値は K/M 表記でフォーマットされる
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
 * 右/左Y軸の現在の範囲とラベル設定から補助線(LimitLine)を再生成する。
 *
 * - mEntries を使わず axisMinimum/axisMaximum を参照
 * - labelCount と granularity（有効なら）を尊重
 * - stepOverride を渡すと任意の刻みで固定可能（例：出来高側に niceStep(rawStep) を適用したい時）
 * - 端の浮動小数誤差を吸収して重複/欠落を防ぐ
 */
fun YAxis.refreshGridLimitLinesFromAxis(
    labelCountOverride: Int? = null,
    stepOverride: Float? = null,
    lineWidth: Float = 1f,
    @ColorInt lineColor: Int = ChartPalette.Grid
) {
    // 標準グリッドは切り、LimitLine を背面で描画
    setDrawGridLines(false)
    setDrawLimitLinesBehindData(true)
    removeAllLimitLines()

    val min = axisMinimum
    val max = axisMaximum
    if (!min.isFinite() || !max.isFinite() || max <= min) return

    val labels = (labelCountOverride ?: this.labelCount).coerceAtLeast(2)
    // 刻み幅の決定: 優先度 = stepOverride > granularity(有効時) > 均等割
    val step = when {
        stepOverride != null && stepOverride > 0f -> stepOverride
        isGranularityEnabled && granularity > 0f -> granularity
        else -> (max - min) / (labels - 1)
    }.coerceAtLeast(1e-6f)

    // min から step ごとに生成。labelCountOverride を使う場合は件数優先、
    // 使わない場合は min..max を越えないように生成。
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