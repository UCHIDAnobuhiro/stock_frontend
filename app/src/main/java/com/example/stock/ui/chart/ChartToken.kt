package com.example.stock.ui.chart

/** チャート専用トークン（Compose Color） */
object ChartTokens {
    object Dimens {
        // ロウソク足チャートの余白
        const val CANDLE_LEFT = 20f
        const val CANDLE_TOP = 10f
        const val CANDLE_RIGHT = 70f
        const val CANDLE_BOTTOM = 10f

        // 出来高チャートの余白
        const val VOLUME_LEFT = 20f
        const val VOLUME_TOP = 10f
        const val VOLUME_RIGHT = 70f
        const val VOLUME_BOTTOM = 80f

        // チャートの表示本数
        const val VISIBLE_COUNT = 60f

        // X軸の目盛の感覚
        const val X_STRIDE = 10

        // X軸のラベル
        const val X_LABEL_ROTATION = -25f

        // X軸の余白(食い込み防止)
        const val X_BOUND_MARGIN = 0.5f

        // ロウソク足の表示範囲の余白
        const val Y_PAD_RATIO = 0.05f

        // 出来高の軸の数
        const val Y_LABEL_COUNT = 5

    }
}