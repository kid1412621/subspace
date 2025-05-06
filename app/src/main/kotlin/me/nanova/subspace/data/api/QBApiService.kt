package me.nanova.subspace.data.api

import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.QBCategories
import me.nanova.subspace.domain.model.Torrent
import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
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

    @FormUrlEncoded
    @POST("api/v2/torrents/start")
    suspend fun start(
        @Field("hashes") torrentHashes: String
    ): ResponseBody

    @FormUrlEncoded
    @POST("api/v2/torrents/stop")
    suspend fun stop(
        @Field("hashes") torrentHashes: String
    ): ResponseBody
}

