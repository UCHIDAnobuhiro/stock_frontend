package com.example.stock.feature.chart.ui.chart

/** チャート設定用のデザイントークン。 */
object ChartTokens {
    /** チャートレイアウトと外観の寸法定数。 */
    object Dimens {
        // ローソク足チャートのビューポートオフセット
        const val CANDLE_LEFT = 20f
        const val CANDLE_TOP = 20f
        const val CANDLE_RIGHT = 70f
        const val CANDLE_BOTTOM = 20f

        // 出来高チャートのビューポートオフセット
        const val VOLUME_LEFT = 20f
        const val VOLUME_TOP = 10f
        const val VOLUME_RIGHT = 70f
        const val VOLUME_BOTTOM = 80f

        // 表示するローソク足の数
        const val VISIBLE_COUNT = 60f

        // X軸グリッド線の間隔
        const val X_STRIDE = 10

        // X軸ラベルの回転角度
        const val X_LABEL_ROTATION = -25f

        // クリッピング防止用のX軸マージン
        const val X_BOUND_MARGIN = 0.5f

        // ローソク足範囲用のY軸パディング比率
        const val Y_PAD_RATIO = 0.05f

        // Y軸ラベルの数
        const val Y_LABEL_COUNT = 5
    }
}