package com.example.countercalculator

data class HistoryRecord(
    val id: Long,
    val apartmentName: String,
    val date: String,
    val total: String,
    val details: List<HistoryDetail>
)

data class HistoryDetail(
    val name: String,
    val value: String,
    val subtitle: String = ""
)
