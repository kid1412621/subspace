package me.nanova.subspace.data.api

import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.Torrent
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface QTApiService {

    @GET("api/v2/app/version")
    suspend fun version(): String

    @GET("api/v2/torrents/info")
    suspend fun getTorrents(@QueryMap params: Map<String, String?>): List<Torrent>

    @GET("api/v2/torrents/info")
    suspend fun list(@QueryMap params: Map<String, String?>): List<Torrent>

    @GET("api/v2/torrents/info")
    fun flow(@QueryMap params: Map<String, String?>): Flow<List<Torrent>>
}

