package ru.finpact.model

enum class Currency(val code: String) {
    RUB("RUB"),
    USD("USD"),
    EUR("EUR");

    companion object {
        private val byCode: Map<String, Currency> =
            entries.associateBy { it.code.uppercase() }

        fun fromCode(code: String): Currency? =
            byCode[code.uppercase()]

        fun isSupported(code: String): Boolean =
            fromCode(code) != null

        fun supportedCodes(): Set<String> =
            byCode.keys
    }
}
