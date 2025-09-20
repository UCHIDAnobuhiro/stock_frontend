package com.example.stock.ui.screen

import android.graphics.Paint
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.stock.R
import com.example.stock.ui.component.CommonHeader
import com.example.stock.viewmodel.CandlesViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.pow

@Composable
fun ChartScreen(
    navController: NavController,
    name: String,
    code: String,
    vm: CandlesViewModel,
    onLogout: () -> Unit
) {
    val candles by vm.candles.collectAsStateWithLifecycle()

    val dataAsc = remember(candles) { candles.sortedBy { it.time } }
    val labels = remember(dataAsc) { dataAsc.map { it.time } }

    LaunchedEffect(code) { vm.load(code, interval = "1day", outputsize = 200) }
    DisposableEffect(Unit) { onDispose { vm.clear() } }

    // チャート参照を保持
    var candleChartRef by remember { mutableStateOf<CandleStickChart?>(null) }
    var volumeChartRef by remember { mutableStateOf<BarChart?>(null) }

    Scaffold(
        topBar = {
            CommonHeader(
                titleText = stringResource(R.string.app_header_candle_chart),
                onBack = { navController.popBackStack() },
                onLogout = onLogout
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = code,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Column(Modifier.fillMaxSize()) {
                var synced by remember { mutableStateOf(false) }
                LaunchedEffect(code) { synced = false }
                if (!synced && candleChartRef != null && volumeChartRef != null) {
                    SideEffect {
                        syncCharts(candleChartRef!!, volumeChartRef!!)
                        synced = true
                    }
                }
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(3f),
                    factory = { ctx ->
                        CandleStickChart(ctx).apply {
                            description.isEnabled = false
                            setDrawGridBackground(false)
                            setPinchZoom(true)
                            isDoubleTapToZoomEnabled = false

                            axisLeft.isEnabled = false

                            axisRight.apply {
                                isEnabled = true
                                setDrawGridLines(true)
                                setLabelCount(5, false)
                                spaceTop = 10f
                            }

                            xAxis.apply {
                                isEnabled = true
                                setDrawLabels(false)
                                setDrawAxisLine(false)
                                setDrawGridLines(false)
                                granularity = 1f
                                position = XAxis.XAxisPosition.BOTTOM
                            }

                            legend.isEnabled = false

                            setViewPortOffsets(20f, 20f, 70f, 10f)
                            candleChartRef = this
                        }
                    },
                    update = { chart ->
                        val entries = dataAsc.mapIndexed { i, c ->
                            CandleEntry(
                                i.toFloat(),
                                c.high.toFloat(),
                                c.low.toFloat(),
                                c.open.toFloat(),
                                c.close.toFloat()
                            )
                        }

                        if (entries.isEmpty()) {
                            chart.clear()
                            chart.setNoDataText("Loading…")
                            chart.invalidate()
                            return@AndroidView
                        }

                        val set = CandleDataSet(entries, "Price").apply {
                            setDrawValues(false)
                            shadowColorSameAsCandle = true
                            decreasingColor = "#e74c3c".toColorInt()   // 赤
                            decreasingPaintStyle = Paint.Style.FILL
                            increasingColor = "#2ecc71".toColorInt()   // 緑
                            increasingPaintStyle = Paint.Style.FILL
                            neutralColor = "#95a5a6".toColorInt()      // グレー
                            barSpace = 0.2f
                        }
                        chart.data = CandleData(set)

                        val minLow = dataAsc.minOf { it.low }.toFloat()
                        val maxHigh = dataAsc.maxOf { it.high }.toFloat()
                        val pad = (maxHigh - minLow) * 0.05f  // 5%マージン
                        chart.axisRight.axisMinimum = (minLow - pad)
                        chart.axisRight.axisMaximum = (maxHigh + pad)

                        chart.isAutoScaleMinMaxEnabled = false
                        chart.isScaleYEnabled = false
                        chart.notifyDataSetChanged()

                        chart.axisRight.apply {
                            setDrawGridLines(false)
                            removeAllLimitLines()
                            // この時点で mEntries が“実際に振られた目盛り”になっている
                            mEntries?.forEach { v ->
                                val ll =
                                    com.github.mikephil.charting.components.LimitLine(v).apply {
                                        lineWidth = 1f
                                        // 好みの色/スタイル
                                        lineColor = "#E5E7EB".toColorInt()   // 薄いグレー
                                        // enableDashedLine(8f, 6f, 0f)      // 破線にしたければ
                                    }
                                addLimitLine(ll)
                            }
                        }

                        val count = entries.size
                        if (count > 20) {
                            chart.setVisibleXRangeMaximum(60f)
                            chart.moveViewToX(chart.data.xMax - 60f)
                        } else {
                            chart.moveViewToX(chart.data.xMax)
                        }

                        val stride = 10
                        chart.xAxis.apply {
                            removeAllLimitLines()
                            for (i in 0 until entries.size step stride) {
                                val ll =
                                    com.github.mikephil.charting.components.LimitLine(i.toFloat())
                                        .apply {
                                            lineWidth = 1f
                                            lineColor = "#E5E7EB".toColorInt()
                                        }
                                addLimitLine(ll)
                            }
                        }
                        chart.xAxis.axisMinimum = -0.5f
                        chart.xAxis.axisMaximum = chart.data.xMax + 0.5f
                        chart.invalidate()
                    }
                )

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.2f),
                    factory = { ctx ->
                        BarChart(ctx).apply {
                            description.isEnabled = false
                            setDrawGridBackground(false)
                            setPinchZoom(true)
                            isDoubleTapToZoomEnabled = false

                            axisLeft.isEnabled = false
                            axisRight.apply {
                                isEnabled = true
                                axisMinimum = 0f
                                setDrawGridLines(false)
                                setLabelCount(5, true)
                            }

                            xAxis.apply {
                                position = XAxis.XAxisPosition.BOTTOM
                                setDrawGridLines(false)
                                granularity = 1f
                                valueFormatter = object : ValueFormatter() {
                                    override fun getFormattedValue(value: Float): String {
                                        val i = value.toInt()
                                        return if (i in labels.indices) labels[i] else ""
                                    }
                                }
                            }
                            legend.isEnabled = false
                            // 上下の描画領域を揃える（左余白を確保）
                            setViewPortOffsets(20f, 10f, 70f, 100f)
                            volumeChartRef = this
                        }
                    },
                    update = { chart ->
                        val volEntries = dataAsc.mapIndexed { i, c ->
                            BarEntry(i.toFloat(), c.volume.toFloat())
                        }

                        if (volEntries.isEmpty()) {
                            chart.clear()
                            chart.setNoDataText("Loading…")
                            chart.invalidate()
                            return@AndroidView
                        }

                        val set = BarDataSet(volEntries, "Volume").apply {
                            setDrawValues(false)
                            axisDependency = YAxis.AxisDependency.RIGHT
                            color = "#3498db".toColorInt()
                        }
                        chart.data = BarData(set).apply { barWidth = 0.8f }

                        val vols = volEntries.map { it.y }
                        val dataMax = vols.maxOrNull() ?: 0f

                        val labelCount = 5
                        val min = 0f

                        val rawStep = if (dataMax <= 0f) 1f else (dataMax - min) / (labelCount - 1)
                        val step = niceStep(rawStep)
                        val axisMax = (min + step * (labelCount - 1)).coerceAtLeast(dataMax)

                        chart.axisRight.apply {
                            axisMinimum = min
                            axisMaximum = axisMax
                            setLabelCount(labelCount, true)
                            valueFormatter = object : ValueFormatter() {
                                override fun getFormattedValue(value: Float): String =
                                    formatCompact(value)
                            }
                        }

                        chart.notifyDataSetChanged()
                        chart.axisRight.apply {
                            setDrawGridLines(false)
                            removeAllLimitLines()
                            mEntries?.forEach { v ->
                                val ll =
                                    com.github.mikephil.charting.components.LimitLine(v).apply {
                                        lineWidth = 1f
                                        lineColor = "#E5E7EB".toColorInt()
                                    }
                                addLimitLine(ll)
                            }
                        }

                        val visibleCount = 60f
                        val count = volEntries.size
                        if (count > visibleCount) {
                            chart.setVisibleXRangeMaximum(visibleCount)
                            chart.moveViewToX(chart.data.xMax - visibleCount) // 右端に最新
                        } else {
                            chart.moveViewToX(chart.data.xMax) // データが少ない時
                        }

                        val stride = 10
                        chart.xAxis.apply {
                            removeAllLimitLines()
                            for (i in 0 until volEntries.size step stride) {
                                val ll =
                                    com.github.mikephil.charting.components.LimitLine(i.toFloat())
                                        .apply {
                                            lineWidth = 1f
                                            lineColor = "#E5E7EB".toColorInt()
                                        }
                                addLimitLine(ll)
                            }

                            valueFormatter = object : ValueFormatter() {
                                override fun getFormattedValue(value: Float): String {
                                    val i = kotlin.math.round(value).toInt()
                                    return if (i in labels.indices && i % 10 == 0) labels[i].replace(
                                        '-',
                                        '/'
                                    ) else ""
                                }
                            }
                            setDrawLabels(true)
                            granularity = 1f
                            isGranularityEnabled = true
                            setAvoidFirstLastClipping(true)
                            textSize = 9f
                            labelRotationAngle = -28f
                        }
                        chart.xAxis.axisMinimum = -0.5f
                        chart.xAxis.axisMaximum = chart.data.xMax + 0.5f
                        chart.invalidate()
                    }
                )
            }
        }
    }

}

/** 上下チャートのズーム/スクロール/ハイライトを同期 */
private fun syncCharts(
    candle: CandleStickChart,
    volume: BarChart
) {
    // 同期用リスナ
    val syncListener = object : OnChartGestureListener {
        override fun onChartScale(me: android.view.MotionEvent?, scaleX: Float, scaleY: Float) {
            volume.viewPortHandler.refresh(candle.viewPortHandler.matrixTouch, volume, true)
        }

        override fun onChartTranslate(me: android.view.MotionEvent?, dX: Float, dY: Float) {
            volume.viewPortHandler.refresh(candle.viewPortHandler.matrixTouch, volume, true)
        }

        override fun onChartGestureEnd(
            me: android.view.MotionEvent?,
            lastPerformedGesture: ChartTouchListener.ChartGesture?
        ) {
            volume.viewPortHandler.refresh(candle.viewPortHandler.matrixTouch, volume, true)
        }

        override fun onChartGestureStart(
            me: android.view.MotionEvent?,
            lastPerformedGesture: ChartTouchListener.ChartGesture?
        ) {
        }

        override fun onChartLongPressed(me: android.view.MotionEvent?) {}
        override fun onChartDoubleTapped(me: android.view.MotionEvent?) {}
        override fun onChartSingleTapped(me: android.view.MotionEvent?) {}
        override fun onChartFling(
            me1: android.view.MotionEvent?,
            me2: android.view.MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ) {
        }
    }
    candle.onChartGestureListener = syncListener

    // 逆方向も同期（下で操作したら上に反映）
    val syncListener2 = object : OnChartGestureListener {
        override fun onChartScale(me: android.view.MotionEvent?, scaleX: Float, scaleY: Float) {
            candle.viewPortHandler.refresh(volume.viewPortHandler.matrixTouch, candle, true)
        }

        override fun onChartTranslate(me: android.view.MotionEvent?, dX: Float, dY: Float) {
            candle.viewPortHandler.refresh(volume.viewPortHandler.matrixTouch, candle, true)
        }

        override fun onChartGestureEnd(
            me: android.view.MotionEvent?,
            lastPerformedGesture: ChartTouchListener.ChartGesture?
        ) {
            candle.viewPortHandler.refresh(volume.viewPortHandler.matrixTouch, candle, true)
        }

        override fun onChartGestureStart(
            me: android.view.MotionEvent?,
            lastPerformedGesture: ChartTouchListener.ChartGesture?
        ) {
        }

        override fun onChartLongPressed(me: android.view.MotionEvent?) {}
        override fun onChartDoubleTapped(me: android.view.MotionEvent?) {}
        override fun onChartSingleTapped(me: android.view.MotionEvent?) {}
        override fun onChartFling(
            me1: android.view.MotionEvent?,
            me2: android.view.MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ) {
        }
    }
    volume.onChartGestureListener = syncListener2


    val isSyncing = AtomicBoolean(false)

    fun sameXHighlighted(chart: Chart<*>?, x: Float): Boolean {
        val cur = chart?.highlighted?.firstOrNull() ?: return false
        return cur.x == x
    }

    // ハイライトも同期（任意）
    candle.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry?, h: Highlight?) {
            if (h == null) return
            val data = volume.data ?: return
            if (data.entryCount == 0) return
            if (sameXHighlighted(volume, h.x)) return
            if (!isSyncing.compareAndSet(false, true)) return
            try {
                volume.highlightValue(h.x, 0)
            } finally {
                isSyncing.set(false)
            }
        }

        override fun onNothingSelected() {
            if (!isSyncing.compareAndSet(false, true)) return
            try {
                volume.highlightValues(null)
            } finally {
                isSyncing.set(false)
            }
        }
    })

    volume.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry?, h: Highlight?) {
            if (h == null) return
            val data = candle.data ?: return
            if (data.entryCount == 0) return
            if (sameXHighlighted(candle, h.x)) return
            if (!isSyncing.compareAndSet(false, true)) return
            try {
                candle.highlightValue(h.x, 0)
            } finally {
                isSyncing.set(false)
            }
        }

        override fun onNothingSelected() {
            if (!isSyncing.compareAndSet(false, true)) return
            try {
                candle.highlightValues(null)
            } finally {
                isSyncing.set(false)
            }
        }
    })

}

// K / M 表記（小数1桁まで）
private fun formatCompact(value: Float): String {
    return when {
        value >= 1_000_000 -> {
            val millions = value / 1_000_000
            if (millions % 1f == 0f) {
                String.format(Locale.US, "%.0fM", millions) // 整数なら小数点なし
            } else {
                String.format(Locale.US, "%.1fM", millions) // それ以外は小数1桁
            }
        }

        value >= 1_000 -> {
            val thousands = value / 1_000
            if (thousands % 1f == 0f) {
                String.format(Locale.US, "%.0fK", thousands)
            } else {
                String.format(Locale.US, "%.1fK", thousands)
            }
        }

        else -> value.toLong().toString()
    }
}

private fun niceStep(step: Float): Float {
    if (step <= 0f) return 1f
    val exp = kotlin.math.floor(kotlin.math.log10(step.toDouble())).toInt()
    val base = 10.0.pow(exp).toFloat()
    val f = step / base
    val nf = when {
        f <= 1f -> 1f
        f <= 2f -> 2f
        f <= 2.5f -> 2.5f
        f <= 5f -> 5f
        else -> 10f
    }
    return nf * base
}