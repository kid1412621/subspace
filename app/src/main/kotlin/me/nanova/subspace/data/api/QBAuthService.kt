package me.nanova.subspace.data.api

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface QBAuthService {

    @FormUrlEncoded
    @POST("api/v2/auth/login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<String>

}
