package me.nanova.subspace.data.api

import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.data.api.dto.TorrentDto
import me.nanova.subspace.data.api.dto.TorrentFileDto
import me.nanova.subspace.data.api.dto.TorrentPeersResponseDto
import me.nanova.subspace.data.api.dto.TorrentPropertiesDto
import me.nanova.subspace.data.api.dto.TorrentTrackerDto
import me.nanova.subspace.data.api.dto.TransferInfoDto
import me.nanova.subspace.domain.model.QBCategories
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface QBApiService {

    @GET("api/v2/app/version")
    suspend fun appVersion(): String

    @GET("api/v2/app/webapiVersion")
    suspend fun apiVersion(): String

    @GET("api/v2/torrents/info")
    suspend fun list(@QueryMap params: Map<String, String?>): List<TorrentDto>

    @GET("api/v2/torrents/categories")
    suspend fun categories(): QBCategories?

    @GET("api/v2/torrents/tags")
    suspend fun tags(): List<String>

    @GET("api/v2/torrents/info")
    fun flow(@QueryMap params: Map<String, String?>): Flow<List<TorrentDto>>

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

    @POST("api/v2/torrents/add")
    suspend fun addTorrent(@Body body: MultipartBody): ResponseBody

    @FormUrlEncoded
    @POST("api/v2/torrents/delete")
    suspend fun delete(
        @Field("hashes") hashes: String,
        @Field("deleteFiles") deleteFiles: Boolean
    ): ResponseBody

    @FormUrlEncoded
    @POST("api/v2/torrents/recheck")
    suspend fun recheck(
        @Field("hashes") hashes: String
    ): ResponseBody

    @GET("api/v2/torrents/properties")
    suspend fun properties(
        @Query("hash") hash: String
    ): TorrentPropertiesDto

    @GET("api/v2/torrents/files")
    suspend fun files(
        @Query("hash") hash: String
    ): List<TorrentFileDto>

    @FormUrlEncoded
    @POST("api/v2/torrents/filePrio")
    suspend fun setFilePriority(
        @Field("hash") hash: String,
        @Field("id") fileIds: String,
        @Field("priority") priority: Int
    ): ResponseBody

    @GET("api/v2/torrents/trackers")
    suspend fun trackers(
        @Query("hash") hash: String
    ): List<TorrentTrackerDto>

    @GET("api/v2/sync/torrentPeers")
    suspend fun peers(
        @Query("hash") hash: String
    ): TorrentPeersResponseDto

    @FormUrlEncoded
    @POST("api/v2/torrents/reannounce")
    suspend fun reannounce(
        @Field("hashes") hashes: String
    ): ResponseBody

    @GET("api/v2/transfer/info")
    suspend fun transferInfo(): TransferInfoDto

    @GET("api/v2/transfer/speedLimitsMode")
    suspend fun speedLimitsMode(): Int

    @POST("api/v2/transfer/toggleSpeedLimitsMode")
    suspend fun toggleSpeedLimitsMode(): ResponseBody
}

