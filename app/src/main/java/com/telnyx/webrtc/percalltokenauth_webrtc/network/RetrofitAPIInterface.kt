package com.telnyx.webrtc.percalltokenauth_webrtc.network

import com.telnyx.webrtc.percalltokenauth_webrtc.network.receive.ApiResponse
import com.telnyx.webrtc.percalltokenauth_webrtc.network.send.Connection
import retrofit2.Call
import retrofit2.http.*

const val API_KEY = "<API_KEY>"

interface RetrofitAPIInterface {

    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer $API_KEY",
    )
    @POST("telephony_credentials")
    fun createConnection(@Body connection_id: Connection): Call<ApiResponse>

    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer $API_KEY",
    )
    @POST("telephony_credentials/{connection_id}/token")
    fun createToken(@Path("connection_id") connectionId: String): Call<String>

}

object ApiUtils {
    private const val BASE_URL = "https://api.telnyx.com/v2/"
    val apiService: RetrofitAPIInterface?
        get() = RetrofitAPIClient.getClient(BASE_URL)?.create(RetrofitAPIInterface::class.java)
}
