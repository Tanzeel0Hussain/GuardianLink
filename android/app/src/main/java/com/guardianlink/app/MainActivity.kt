package com.guardianlink.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.guardianlink.app.databinding.ActivityMainBinding
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private val http = OkHttpClient()
    private val json = "application/json".toMediaType()

    private val prefs by lazy {
        getSharedPreferences("guardianlink", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.serverUrl.setText(prefs.getString("server", ""))
        b.deviceName.setText(android.os.Build.MODEL)

        b.pairButton.setOnClickListener { pair() }
        b.heartbeatButton.setOnClickListener { heartbeat() }
        b.checkCommandButton.setOnClickListener { checkCommand() }
    }

    private fun pair() {
        val server = b.serverUrl.text.toString().trim().trimEnd('/')
        val pairingId = b.pairingId.text.toString().trim()
        val name = b.deviceName.text.toString().trim()

        if (server.isBlank() || pairingId.isBlank()) {
            toast("Enter server URL and pairing ID")
            return
        }

        val body = JSONObject()
            .put("device_name", name)
            .put("model", android.os.Build.MODEL)
            .put("android_version", android.os.Build.VERSION.RELEASE)
            .toString()
            .toRequestBody(json)

        thread {
            try {
                val request = Request.Builder()
                    .url("$server/api/pairing/$pairingId/claim")
                    .post(body)
                    .build()

                http.newCall(request).execute().use { response ->
                    val text = response.body?.string().orEmpty()
                    if (response.isSuccessful) {
                        val result = JSONObject(text)
                        prefs.edit()
                            .putString("server", server)
                            .putString("device_id", result.getString("device_id"))
                            .putString("device_token", result.getString("device_token"))
                            .apply()
                        status("Paired successfully")
                    } else {
                        status("Pairing failed: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                status(e.message ?: "Error")
            }
        }
    }

    private fun heartbeat() {
        val state = state() ?: return
        thread {
            try {
                val request = Request.Builder()
                    .url("${state.first}/api/devices/${state.second}/heartbeat")
                    .header("Authorization", "Bearer ${state.third}")
                    .post("{}".toRequestBody(json))
                    .build()
                http.newCall(request).execute().use {
                    status(if (it.isSuccessful) "Heartbeat sent" else "Heartbeat failed")
                }
            } catch (e: Exception) {
                status(e.message ?: "Error")
            }
        }
    }

    private fun checkCommand() {
        val state = state() ?: return
        thread {
            try {
                val request = Request.Builder()
                    .url("${state.first}/api/devices/${state.second}/commands/next")
                    .header("Authorization", "Bearer ${state.third}")
                    .build()
                http.newCall(request).execute().use {
                    val result = JSONObject(it.body?.string().orEmpty())
                    status(if (result.isNull("command")) "No commands"
                           else "Command: ${result.getString("command")}")
                }
            } catch (e: Exception) {
                status(e.message ?: "Error")
            }
        }
    }

    private fun state(): Triple<String, String, String>? {
        val server = prefs.getString("server", null)
        val id = prefs.getString("device_id", null)
        val token = prefs.getString("device_token", null)
        if (server == null || id == null || token == null) {
            toast("Pair this device first")
            return null
        }
        return Triple(server, id, token)
    }

    private fun status(text: String) = runOnUiThread { b.status.text = text }
    private fun toast(text: String) =
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}
