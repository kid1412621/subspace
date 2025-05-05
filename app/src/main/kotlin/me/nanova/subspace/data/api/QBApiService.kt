package me.nanova.subspace.data.api

import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.QBCategories
import me.nanova.subspace.domain.model.Torrent
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface QBApiService {

    @GET("api/v2/app/version")
    suspend fun appVersion(): String

    @GET("api/v2/app/webapiVersion")
    suspend fun apiVersion(): String

    @GET("api/v2/torrents/info")
    suspend fun list(@QueryMap params: Map<String, String?>): List<Torrent>

    @GET("api/v2/torrents/categories")
    suspend fun categories(): QBCategories?

    @GET("api/v2/torrents/tags")
    suspend fun tags(): List<String>

    @GET("api/v2/torrents/info")
    fun flow(@QueryMap params: Map<String, String?>): Flow<List<Torrent>>
}

