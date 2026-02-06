package com.example.stock.core.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * チャートの表示間隔（日足、週足、月足）を選択するドロップダウン。
 *
 * @param selected 現在選択されている間隔（例："1day"）
 * @param onSelected 間隔が選択された時のコールバック
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntervalDropDown(
    selected: String,
    onSelected: (String) -> Unit
) {
    // 選択肢のリスト
    val options = listOf("1day", "1week", "1month")
    // ドロップダウンの展開状態
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        // 選択された間隔を表示するテキストフィールド（読み取り専用）
        TextField(
            value = intervalLabels[selected] ?: selected,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        // ドロップダウンメニュー本体
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(intervalLabels[option] ?: option) },
                    onClick = {
                        onSelected(option) // 選択時にコールバックを呼び出す
                        expanded = false  // メニューを閉じる
                    }
                )
            }
        }
    }
}

// 間隔ラベル表示用のマップ
private val intervalLabels = mapOf(
    "1day" to "日足",
    "1week" to "週足",
    "1month" to "月足"
)
