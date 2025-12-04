package com.example.stock.feature.chart.ui.chart

import java.util.Locale
import kotlin.math.pow

/**
 * 数値をコンパクトに表記するユーティリティ。
 *
 * - 1,200 → "1.2K"
 * - 12,000 → "12K"
 * - 1,200,000 → "1.2M"
 * - 123 → "123"
 *
 * @param value 表示したい数値（float）
 * @return "K" や "M" を付けた文字列（小数は最大1桁）
 */
fun formatCompact(value: Float): String = when {
    value >= 1_000_000 -> {
        val m = value / 1_000_000
        if (m % 1f == 0f) String.format(Locale.US, "%.0fM", m)
        else String.format(Locale.US, "%.1fM", m)
    }

    value >= 1_000 -> {
        val k = value / 1_000
        if (k % 1f == 0f) String.format(Locale.US, "%.0fK", k)
        else String.format(Locale.US, "%.1fK", k)
    }

    else -> value.toLong().toString()
}

/**
 * グラフ軸の「きれいな刻み幅」を返す。
 *
 * 例:
 * - 7 → 10
 * - 22 → 25
 * - 260 → 500
 *
 * 1, 2, 2.5, 5 × 10^n のいずれかになる。
 *
 * @param step 軸ラベル間隔の元になる値
 * @return 見やすい刻み幅
 */
fun niceStep(step: Float): Float {
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
