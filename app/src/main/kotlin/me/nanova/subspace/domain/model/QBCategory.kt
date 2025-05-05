package me.nanova.subspace.domain.model

data class QBCategory(
    val name: String,
    val savePath: String = ""
)

typealias QBCategories = Map<String, QBCategory>