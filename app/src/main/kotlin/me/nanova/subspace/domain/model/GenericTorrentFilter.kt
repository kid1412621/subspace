package me.nanova.subspace.domain.model

data class GenericTorrentFilter(
    val status: Set<DomainTorrentState>? = null,
    val query: String? = null,
    val tags: List<String>? = null,
    val category: String? = null
    // Pagination parameters (page, pageSize) are assumed to be handled by Paging 3
    // and are not explicitly part of this filter class for now.
)
