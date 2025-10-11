package com.example.stock.ui.chart

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
 * 上下2つのチャート（ローソク足と出来高）を同期させるユーティリティ。
 *
 * 機能:
 * - ズーム/スクロール同期（相互）
 * - ハイライト同期（相互）
 * - 慣性スクロールを無効化して遅延やカクつきを防止
 *
 * @param candle ローソク足チャート
 * @param volume 出来高チャート
 */
fun attachSynchronizedPair(
    candle: CandleStickChart,
    volume: BarChart
) {
    // --- 慣性（deceleration）を完全に無効化 ---
    candle.isDragDecelerationEnabled = false
    volume.isDragDecelerationEnabled = false

    // --- viewport(ズーム/スクロール) 同期用のロック ---
    val vpLock = AtomicBoolean(false)

    /**
     * fromのviewport（ズーム・スクロール状態）をtoに反映する。
     * 同期中はロックし、無限ループを防止。
     */
    fun syncViewport(from: Chart<*>, to: Chart<*>) {
        if (!vpLock.compareAndSet(false, true)) return
        try {
            to.viewPortHandler.refresh(from.viewPortHandler.matrixTouch, to, true)
        } finally {
            vpLock.set(false)
        }
    }

    // Candle を動かしたら Volume に反映
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

    // Volume を動かしたら Candle に反映
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

    // --- ハイライト同期用のロック ---
    val hlLock = AtomicBoolean(false)

    /**
     * 指定したチャートで現在ハイライトされているX値が一致するか判定。
     */
    fun sameXHighlighted(chart: Chart<*>?, x: Float): Boolean {
        val cur = chart?.highlighted?.firstOrNull() ?: return false
        return cur.x == x
    }

    // ローソク足チャートで値選択時、出来高チャートも同じXをハイライト
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

    // 出来高チャートで値選択時、ローソク足チャートも同じXをハイライト
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