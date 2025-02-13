package com.telnyx.webrtc.percalltokenauth_webrtc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.telnyx.webrtc.percalltokenauth_webrtc.databinding.ActivityMainBinding
import com.telnyx.webrtc.percalltokenauth_webrtc.network.ApiUtils
import com.telnyx.webrtc.percalltokenauth_webrtc.network.RetrofitAPIInterface
import com.telnyx.webrtc.percalltokenauth_webrtc.network.receive.ApiResponse
import com.telnyx.webrtc.percalltokenauth_webrtc.network.send.Connection
import com.telnyx.webrtc.sdk.TelnyxClient
import com.telnyx.webrtc.sdk.TokenConfig
import com.telnyx.webrtc.sdk.model.LogLevel
import com.telnyx.webrtc.sdk.model.SocketMethod
import com.telnyx.webrtc.sdk.verto.receive.ReceivedMessageBody
import com.telnyx.webrtc.sdk.verto.receive.SocketObserver
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Step 1. Declare API service for token generation
    private val apiService: RetrofitAPIInterface? = ApiUtils.apiService
    private var connectionCreationResponse: ApiResponse? = null
    private var haveConnectionId = false
    private var haveToken = false
    private var callToken: String? = null
    private var telnyxClient: TelnyxClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.buttonCallId.setOnClickListener {
            if (haveConnectionId) {
                startCallSequence(connectionCreationResponse!!)
            } else {
                Log.d("TelnyxSocketListener","Can't create token and start call, we don't have a connection ID")
            }
        }

        createConnection()
    }

    private fun createConnection() {
        // connection_id is from SIP connections Basic Info
       apiService?.createConnection(Connection("<SIP CONNECTION>"))?.enqueue(object :
           Callback<ApiResponse> {
           override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
               if (response.isSuccessful) {
                   connectionCreationResponse = response.body()
                   haveConnectionId = true
               }
           }

           override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
               Log.d("CreateConnection","Could not create connection :: $t")
               haveConnectionId = false
               haveToken = false
           }
       })
    }

    private fun getToken(connectionResponse: ApiResponse) {
        if (haveConnectionId) {
            apiService?.createToken(connectionResponse.data.id)?.enqueue(object :
                Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        callToken = response.body()
                        Log.d("CreateToken","$callToken")
                        tokenLogin(callToken!!)
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d("CreateToken","Failed to get token :: $t")
                }
            })
        } else {
            Log.d("CreateToken","We have not created a connection ID yet, cannot create token")
        }
    }

    private fun tokenLogin(token: String) {
        val tokenLoginConfig = TokenConfig(
            sipToken = token,
            sipCallerIDName = null,
            sipCallerIDNumber = null,
            fcmToken = null,
            ringtone = null,
            ringBackTone = null,
            logLevel = LogLevel.ALL
        )
        telnyxClient = TelnyxClient(this)

        telnyxClient?.connect(tokenConfig = tokenLoginConfig)

        // Setup Listener:
        telnyxClient?.getSocketResponse()?.observe(this, object : SocketObserver<ReceivedMessageBody>() {
            override fun onConnectionEstablished() {
                Log.d("TelnyxSocketListener","Connection Established")
            }

            override fun onMessageReceived(data: ReceivedMessageBody?) {
                when (data?.method) {
                    SocketMethod.CLIENT_READY.methodName -> {
                        Log.d("TelnyxSocketListener","Client is ready, I can make a call")
                        telnyxClient?.newInvite("callerName",
                            "0000000",
                            "<DESTINATION>",
                            "clientState")
                    }
                    SocketMethod.LOGIN.methodName -> {
                        Log.d("TelnyxSocketListener","Successfully logged in. Starting call once client is ready")
                    }

                    SocketMethod.INVITE.methodName -> {
                        Log.d("TelnyxSocketListener","Received an invite.")
                    }

                    SocketMethod.ANSWER.methodName -> {
                        Log.d("TelnyxSocketListener","Received an answer")

                    }

                    SocketMethod.BYE.methodName -> {
                        Log.d("TelnyxSocketListener","Received a bye response")
                        telnyxClient?.onDisconnect()
                    }
                }
            }

            override fun onLoading() {
                // Show loading dialog
            }

            override fun onSocketDisconnect() {
                Log.d("TelnyxSocketListener","Disconnected from the socket")
            }

            override fun onError(message: String?) {
                Log.d("TelnyxSocketListener","Error :: $message")
            }
        })
    }

    private fun startCallSequence(connectionResponse: ApiResponse) {
       getToken(connectionResponse)
    }
}
