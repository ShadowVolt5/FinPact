package ru.finpact.model

data class TransferSearchPage(
    val items: List<Transfer>,
    val hasMore: Boolean,
)
