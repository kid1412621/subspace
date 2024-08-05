package me.nanova.subspace.domain.model

data class QTCategory(
    val name: String,
    val savePath: String
)

typealias QTCategories = Map<String, QTCategory>