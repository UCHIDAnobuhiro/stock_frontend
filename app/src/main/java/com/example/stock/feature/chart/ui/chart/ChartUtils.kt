package com.example.stock.feature.chart.ui.chart

import java.util.Locale
import kotlin.math.pow

/**
 * コンパクト表記（K/Mサフィックス）で数値をフォーマットする。
 *
 * 例：1,200 → "1.2K"、1,200,000 → "1.2M"、123 → "123"
 *
 * @param value フォーマットする数値
 * @return オプションのK/Mサフィックス付きのフォーマット済み文字列
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
 * 軸ラベル用の"nice"なステップ値を返す。
 *
 * 読みやすい間隔のため1, 2, 2.5, 5 × 10^nのいずれかに丸める。
 * 例：7 → 10、22 → 25、260 → 500
 *
 * @param step 元のステップ値
 * @return 丸められたniceステップ値
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
