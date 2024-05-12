package me.nanova.subspace.data.api

import me.nanova.subspace.domain.model.Categories
import me.nanova.subspace.domain.model.Torrent
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface QTApiService {

    @GET("api/v2/app/version")
    suspend fun version(): String

    @GET("api/v2/torrents/info")
    suspend fun torrents(@QueryMap params: Map<String, String?>): List<Torrent>

    @GET("api/v2/torrents/categories")
    suspend fun categories(): Map<String, Categories>

    @GET("api/v2/torrents/tags")
    suspend fun tags(): List<String>
}

