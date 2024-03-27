package me.nanova.subspace.domain.model

data class QTListParams(

    var filter: String = "all",
    var category: String? = null,
    var tag: String? = null,
    var sort: String? = null,
    var reverse: Boolean = false,
    var limit: Int = 20,
    var offset: Int = 0,
    var hashes: String? = null,
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
