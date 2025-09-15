package com.example.stock.ui.screen

import android.graphics.Paint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import java.util.concurrent.atomic.AtomicBoolean


@Composable
fun ChartScreen(
    code: String,
    vm: CandlesViewModel
) {
    val candles by vm.candles.collectAsStateWithLifecycle()

    val dataAsc = remember(candles) { candles.sortedBy { it.time } }
    val labels = remember(dataAsc) { dataAsc.map { it.time } }

    LaunchedEffect(code) { vm.load(code, interval = "1day", outputsize = 200) }
    DisposableEffect(Unit) { onDispose { vm.clear() } }

    // チャート参照を保持
    var candleChartRef by remember { mutableStateOf<CandleStickChart?>(null) }
    var volumeChartRef by remember { mutableStateOf<BarChart?>(null) }

    Column(Modifier.fillMaxSize()){
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
            factory = {ctx ->
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

                    xAxis.isEnabled = false
                    legend.isEnabled = false

                    setViewPortOffsets(20f, 20f, 60f, 10f)
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
                chart.isAutoScaleMinMaxEnabled = true
                chart.notifyDataSetChanged()

                val count = entries.size
                if (count > 20) {
                    chart.setVisibleXRangeMaximum(60f)
                    chart.moveViewToX(chart.data.xMax - 60f)
                } else {
                    chart.moveViewToX(chart.data.xMax)
                }

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
                    setViewPortOffsets(20f, 10f, 60f, 30f)
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
                chart.notifyDataSetChanged()

                val visibleCount = 60f
                val count = volEntries.size
                if (count > visibleCount) {
                    chart.setVisibleXRangeMaximum(visibleCount)
                    chart.moveViewToX(chart.data.xMax - visibleCount) // 右端に最新
                } else {
                    chart.moveViewToX(chart.data.xMax) // データが少ない時
                }

                chart.invalidate()

            }
        )
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
        override fun onChartGestureEnd(me: android.view.MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
            volume.viewPortHandler.refresh(candle.viewPortHandler.matrixTouch, volume, true)
        }
        override fun onChartGestureStart(me: android.view.MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
        override fun onChartLongPressed(me: android.view.MotionEvent?) {}
        override fun onChartDoubleTapped(me: android.view.MotionEvent?) {}
        override fun onChartSingleTapped(me: android.view.MotionEvent?) {}
        override fun onChartFling(me1: android.view.MotionEvent?, me2: android.view.MotionEvent?, velocityX: Float, velocityY: Float) {}
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
        override fun onChartGestureEnd(me: android.view.MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
            candle.viewPortHandler.refresh(volume.viewPortHandler.matrixTouch, candle, true)
        }
        override fun onChartGestureStart(me: android.view.MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
        override fun onChartLongPressed(me: android.view.MotionEvent?) {}
        override fun onChartDoubleTapped(me: android.view.MotionEvent?) {}
        override fun onChartSingleTapped(me: android.view.MotionEvent?) {}
        override fun onChartFling(me1: android.view.MotionEvent?, me2: android.view.MotionEvent?, velocityX: Float, velocityY: Float) {}
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
