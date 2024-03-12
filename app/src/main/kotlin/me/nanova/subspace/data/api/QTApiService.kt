package me.nanova.subspace.data.api

import me.nanova.subspace.domain.model.Torrent
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface QTApiService {

    @GET("api/v2/app/version")
    suspend fun version(): Response<String>

    @GET("api/v2/torrents/info")
    suspend fun getTorrents(@QueryMap params: Map<String, String?>): List<Torrent>
}

