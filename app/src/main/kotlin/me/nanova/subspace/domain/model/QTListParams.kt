package me.nanova.subspace.domain.model

import me.nanova.subspace.data.TorrentRepoImpl.Companion.PAGE_SIZE

data class QTListParams(
    val filter: String = "all",
    val category: String? = null,
    val tag: String? = null,
    val sort: String? = null,
    val reverse: Boolean = false,
    val hashes: String? = null,
    val limit: Int = PAGE_SIZE,
    val offset: Int = 0,
) {
    fun toMap(): Map<String, String?> {
//        val queryParamsMap = this::class.memberProperties
//            .filter { it.isAccessible = true; (it as KProperty1<QtListParams, *>).get(this) != null }
//            .associate { it.name to (it as KProperty1<QtListParams, *>).get(this).toString() }
//        return queryParamsMap;
        @Suppress("UNCHECKED_CAST")
        return mapOf(
            "filter" to filter,
            "category" to category,
            "tag" to tag,
            "sort" to sort,
            "reverse" to reverse.toString(),
            "limit" to limit.toString(),
            "offset" to offset.toString(),
            "hashes" to hashes
        ).filterValues { it != null } as Map<String, String>
    }

    fun hasFiltered(): Boolean {
        return this.filter != "all" || this.category != null || this.tag != null || this.hashes != null
    }

    fun hasSorted(): Boolean {
        return this.sort != null
    }
}
