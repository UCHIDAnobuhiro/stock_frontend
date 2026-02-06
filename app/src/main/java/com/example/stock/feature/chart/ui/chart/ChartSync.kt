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
 * 2つのチャート（ローソク足と出来高）を同期して連動操作を実現する。
 *
 * 機能：
 * - 双方向のズーム/スクロール同期
 * - 双方向のハイライト同期
 * - ラグ防止のため慣性スクロールを無効化
 *
 * @param candle ローソク足チャートインスタンス
 * @param volume 出来高チャートインスタンス
 */
fun attachSynchronizedPair(
    candle: CandleStickChart,
    volume: BarChart
) {
    candle.isDragDecelerationEnabled = false
    volume.isDragDecelerationEnabled = false

    val vpLock = AtomicBoolean(false)

    /**
     * 一方のチャートから他方へビューポートマトリックスを同期する。
     * チャート間の無限再帰を防ぐために[vpLock]を使用する。
     *
     * @param from ビューポートをコピーする元のチャート
     * @param to ビューポートを適用する先のチャート
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
     * チャートが既に同じX位置にハイライトを持っているかを確認する。
     * 冗長なハイライト更新を防ぐために使用する。
     *
     * @param chart 既存のハイライトを確認するチャート
     * @param x 比較するX座標
     * @return チャートが既に同じX位置にハイライトを持っている場合はtrue
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