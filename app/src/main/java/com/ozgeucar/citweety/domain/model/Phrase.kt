package com.ozgeucar.citweety.domain.model

data class Phrase(
    val english: String,
    val local: String,
    val languageCode: String // "pl" for Polish, "hu" for Hungarian etc.
)