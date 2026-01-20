package com.example.stock.feature.chart.ui.chart

import android.view.MotionEvent
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Synchronizes two charts (candlestick and volume) for coordinated interaction.
 *
 * Features:
 * - Bidirectional zoom/scroll synchronization
 * - Bidirectional highlight synchronization
 * - Disabled inertia scrolling to prevent lag
 *
 * @param candle Candlestick chart instance
 * @param volume Volume chart instance
 */
fun attachSynchronizedPair(
    candle: CandleStickChart,
    volume: BarChart
) {
    candle.isDragDecelerationEnabled = false
    volume.isDragDecelerationEnabled = false

    val vpLock = AtomicBoolean(false)

    /**
     * Synchronizes viewport matrix from one chart to another.
     * Uses [vpLock] to prevent infinite recursion between charts.
     *
     * @param from Source chart to copy viewport from
     * @param to Target chart to apply viewport to
     */
    fun syncViewport(from: Chart<*>, to: Chart<*>) {
        if (!vpLock.compareAndSet(false, true)) return
        try {
            to.viewPortHandler.refresh(from.viewPortHandler.matrixTouch, to, true)
        } finally {
            vpLock.set(false)
        }
    }

    val candleToVolumeGesture = object : OnChartGestureListener {
        override fun onChartGestureStart(e: MotionEvent?, g: ChartTouchListener.ChartGesture?) {
            syncViewport(candle, volume)
        }

        override fun onChartScale(e: MotionEvent?, sx: Float, sy: Float) {
            syncViewport(candle, volume)
        }

        override fun onChartTranslate(e: MotionEvent?, dx: Float, dy: Float) {
            syncViewport(candle, volume)
        }

        override fun onChartGestureEnd(e: MotionEvent?, g: ChartTouchListener.ChartGesture?) {
            syncViewport(candle, volume)
        }

        override fun onChartLongPressed(e: MotionEvent?) {}
        override fun onChartDoubleTapped(e: MotionEvent?) {}
        override fun onChartSingleTapped(e: MotionEvent?) {}
        override fun onChartFling(e1: MotionEvent?, e2: MotionEvent?, vx: Float, vy: Float) {}
    }
    candle.onChartGestureListener = candleToVolumeGesture

    val volumeToCandleGesture = object : OnChartGestureListener {
        override fun onChartGestureStart(e: MotionEvent?, g: ChartTouchListener.ChartGesture?) {
            syncViewport(volume, candle)
        }

        override fun onChartScale(e: MotionEvent?, sx: Float, sy: Float) {
            syncViewport(volume, candle)
        }

        override fun onChartTranslate(e: MotionEvent?, dx: Float, dy: Float) {
            syncViewport(volume, candle)
        }

        override fun onChartGestureEnd(e: MotionEvent?, g: ChartTouchListener.ChartGesture?) {
            syncViewport(volume, candle)
        }

        override fun onChartLongPressed(e: MotionEvent?) {}
        override fun onChartDoubleTapped(e: MotionEvent?) {}
        override fun onChartSingleTapped(e: MotionEvent?) {}
        override fun onChartFling(e1: MotionEvent?, e2: MotionEvent?, vx: Float, vy: Float) {}
    }
    volume.onChartGestureListener = volumeToCandleGesture

    val hlLock = AtomicBoolean(false)

    /**
     * Checks if the chart already has a highlight at the same X position.
     * Used to prevent redundant highlight updates.
     *
     * @param chart Chart to check for existing highlight
     * @param x X coordinate to compare
     * @return true if the chart already has a highlight at the same X position
     */
    fun sameXHighlighted(chart: Chart<*>?, x: Float): Boolean {
        val cur = chart?.highlighted?.firstOrNull() ?: return false
        return cur.x == x
    }

    candle.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry?, h: Highlight?) {
            if (h == null) return
            val data = volume.data ?: return
            if (data.entryCount == 0 || sameXHighlighted(volume, h.x)) return
            if (!hlLock.compareAndSet(false, true)) return
            try {
                volume.highlightValue(h.x, 0)
            } finally {
                hlLock.set(false)
            }
        }

        override fun onNothingSelected() {
            if (!hlLock.compareAndSet(false, true)) return
            try {
                volume.highlightValues(null)
            } finally {
                hlLock.set(false)
            }
        }
    })

    volume.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry?, h: Highlight?) {
            if (h == null) return
            val data = candle.data ?: return
            if (data.entryCount == 0 || sameXHighlighted(candle, h.x)) return
            if (!hlLock.compareAndSet(false, true)) return
            try {
                candle.highlightValue(h.x, 0)
            } finally {
                hlLock.set(false)
            }
        }

        override fun onNothingSelected() {
            if (!hlLock.compareAndSet(false, true)) return
            try {
                candle.highlightValues(null)
            } finally {
                hlLock.set(false)
            }
        }
    })
}