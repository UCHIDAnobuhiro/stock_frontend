package com.example.stock.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SymbolItem(
    val code: String,
    val name: String
)
