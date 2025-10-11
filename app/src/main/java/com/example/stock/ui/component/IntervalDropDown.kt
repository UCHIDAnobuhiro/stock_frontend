package com.example.stock.ui.component

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
 * チャートのインターバル（日足・週足・月足）を選択するドロップダウン。
 *
 * @param selected 現在選択中のインターバル（"1day"など）
 * @param onSelected インターバル選択時のコールバック
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntervalDropDown(
    selected: String,
    onSelected: (String) -> Unit
) {
    // 選択肢一覧
    val options = listOf("1day", "1week", "1month")
    // ドロップダウン展開状態
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        // 選択中インターバルを表示するテキストフィールド（編集不可）
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
                        onSelected(option) // 選択時にコールバック
                        expanded = false  // メニューを閉じる
                    }
                )
            }
        }
    }
}

// インターバルのラベル表示用マップ
private val intervalLabels = mapOf(
    "1day" to "日足",
    "1week" to "週足",
    "1month" to "月足"
)
