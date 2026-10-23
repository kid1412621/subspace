package me.nanova.subspace.domain.model

data class QBListParams(
    val filter: String = "all",
    val category: String? = null,
    val tag: String? = null,
    val sort: String? = null,
    val reverse: Boolean = false,
    val hashes: String? = null,
    val limit: Int = DEFAULT_PAGE_SIZE,
    val offset: Int = 0,
) {
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }

    fun toMap(): Map<String, String?> {
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
