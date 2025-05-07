package me.nanova.subspace.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface QBAuthService {

    // endpoint: api/v2/auth/login
    @FormUrlEncoded
    @POST
    suspend fun login(
        @Url url: String,
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<ResponseBody>

    // endpoint: /api/v2/app/version
    @GET
    suspend fun appVersion(@Url url: String, @Header("Cookie") cookie: String): String

}
