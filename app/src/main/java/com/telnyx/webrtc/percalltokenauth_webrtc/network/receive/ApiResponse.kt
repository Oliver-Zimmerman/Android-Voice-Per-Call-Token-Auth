package com.telnyx.webrtc.percalltokenauth_webrtc.network.receive

import com.google.gson.annotations.SerializedName

data class ApiResponse (
    @SerializedName("data") val data : Data
)

data class Data (
    @SerializedName("created_at") val created_at : String,
    @SerializedName("expired") val expired : Boolean,
    @SerializedName("expires_at") val expires_at : String,
    @SerializedName("id") val id : String,
    @SerializedName("name") val name : String,
    @SerializedName("record_type") val record_type : String,
    @SerializedName("resource_id") val resource_id : String,
    @SerializedName("sip_password") val sip_password : String,
    @SerializedName("sip_username") val sip_username : String,
    @SerializedName("status") val status : String,
    @SerializedName("tag") val tag : String,
    @SerializedName("updated_at") val updated_at : String,
    @SerializedName("user_id") val user_id : String
)
