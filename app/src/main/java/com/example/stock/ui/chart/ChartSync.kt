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
 * 上下2つのチャート（ローソク足と出来高）を同期させる。
 *
 * - **ズーム／スクロール同期**
 *   上側（CandleStickChart）を操作すると下側（BarChart）が同じ範囲に追随し、
 *   下側を操作すると上側に反映される。
 *
 * - **ハイライト同期**
 *   任意のチャートでクロスヘアやタップ選択を行った際、同じ X 座標のデータを
 *   相手チャートにもハイライトする。解除も相互に同期される。
 *
 * - **ループ防止**
 *   `AtomicBoolean` によるロックで無限相互通知を防いでいる。
 *
 * @param candle 上段のローソク足チャート
 * @param volume 下段の出来高チャート
 */
fun attachSynchronizedPair(
    candle: CandleStickChart,
    volume: BarChart
) {
    // ズーム/スクロールの動機
    // Candleを動かした時にVolumeを反映
    val c2v = object : OnChartGestureListener {
        override fun onChartScale(e: MotionEvent?, sx: Float, sy: Float) {
            volume.viewPortHandler.refresh(candle.viewPortHandler.matrixTouch, volume, true)
        }

        override fun onChartTranslate(e: MotionEvent?, dx: Float, dy: Float) {
            volume.viewPortHandler.refresh(candle.viewPortHandler.matrixTouch, volume, true)
        }

        override fun onChartGestureEnd(
            e: MotionEvent?,
            g: ChartTouchListener.ChartGesture?
        ) {
            volume.viewPortHandler.refresh(candle.viewPortHandler.matrixTouch, volume, true)
        }

        override fun onChartGestureStart(
            e: MotionEvent?,
            g: ChartTouchListener.ChartGesture?
        ) {
        }

        //　使用しないイベントは空実装
        override fun onChartLongPressed(e: MotionEvent?) {}
        override fun onChartDoubleTapped(e: MotionEvent?) {}
        override fun onChartSingleTapped(e: MotionEvent?) {}
        override fun onChartFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            vx: Float,
            vy: Float
        ) {
        }
    }
    candle.onChartGestureListener = c2v

    // Volumeを動かした時にCandleを反映
    val v2c = object : OnChartGestureListener {
        override fun onChartScale(e: MotionEvent?, sx: Float, sy: Float) {
            candle.viewPortHandler.refresh(volume.viewPortHandler.matrixTouch, candle, true)
        }

        override fun onChartTranslate(e: MotionEvent?, dx: Float, dy: Float) {
            candle.viewPortHandler.refresh(volume.viewPortHandler.matrixTouch, candle, true)
        }

        override fun onChartGestureEnd(
            e: MotionEvent?,
            g: ChartTouchListener.ChartGesture?
        ) {
            candle.viewPortHandler.refresh(volume.viewPortHandler.matrixTouch, candle, true)
        }

        override fun onChartGestureStart(
            e: MotionEvent?,
            g: ChartTouchListener.ChartGesture?
        ) {
        }

        override fun onChartLongPressed(e: MotionEvent?) {}
        override fun onChartDoubleTapped(e: MotionEvent?) {}
        override fun onChartSingleTapped(e: MotionEvent?) {}
        override fun onChartFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            vx: Float,
            vy: Float
        ) {
        }
    }
    volume.onChartGestureListener = v2c

    // ハイライトの同期
    // ロックを使用し無限ループを防ぐ
    val lock = AtomicBoolean(false)

    fun sameXHighlighted(chart: Chart<*>?, x: Float): Boolean {
        val cur = chart?.highlighted?.firstOrNull() ?: return false
        return cur.x == x
    }

    // Candle 側で選択されたら Volume に反映
    candle.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry?, h: Highlight?) {
            if (h == null) return
            val d = volume.data ?: return
            if (d.entryCount == 0 || sameXHighlighted(volume, h.x)) return
            if (!lock.compareAndSet(false, true)) return
            try {
                volume.highlightValue(h.x, 0)
            } finally {
                lock.set(false)
            }
        }

        override fun onNothingSelected() {
            if (!lock.compareAndSet(false, true)) return
            try {
                volume.highlightValues(null)
            } finally {
                lock.set(false)
            }
        }
    })

    // Volume 側で選択されたら Candle に反映
    volume.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry?, h: Highlight?) {
            if (h == null) return
            val d = candle.data ?: return
            if (d.entryCount == 0 || sameXHighlighted(candle, h.x)) return
            if (!lock.compareAndSet(false, true)) return
            try {
                candle.highlightValue(h.x, 0)
            } finally {
                lock.set(false)
            }
        }

        override fun onNothingSelected() {
            if (!lock.compareAndSet(false, true)) return
            try {
                candle.highlightValues(null)
            } finally {
                lock.set(false)
            }
        }
    })
}