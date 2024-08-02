package me.nanova.subspace.domain.model

import me.nanova.subspace.data.TorrentRepoImpl.Companion.PAGE_SIZE

data class QTListParams(

    var filter: String = "all",
    var category: String? = null,
    var tag: String? = null,
    var sort: String? = null,
    var reverse: Boolean? = null,
    var hashes: String? = null,
    var limit: Int = PAGE_SIZE,
    var offset: Int = 0,
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
}
