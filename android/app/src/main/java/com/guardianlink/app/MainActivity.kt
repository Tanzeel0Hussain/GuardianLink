package com.guardianlink.app

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.mlkit.codescanner.GmsBarcodeScannerOptions
import com.google.android.gms.mlkit.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
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

        b.serverUrl.setText(
            prefs.getString("server", "")
        )

        b.deviceName.setText(
            android.os.Build.MODEL
        )

        b.scanQrButton.setOnClickListener {
            scanPairingQr()
        }

        b.pairButton.setOnClickListener {
            pair()
        }

        b.heartbeatButton.setOnClickListener {
            heartbeat()
        }

        b.checkCommandButton.setOnClickListener {
            checkCommand()
        }

        if (prefs.contains("device_id")) {
            status("Device already paired")
        }
    }

    /*
     * ---------------------------------------------------------
     * QR SCANNER
     * ---------------------------------------------------------
     */

    private fun scanPairingQr() {

        status("Opening QR scanner...")

        val options =
            GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE
                )
                .enableAutoZoom()
                .build()

        val scanner =
            GmsBarcodeScanning.getClient(
                this,
                options
            )

        scanner.startScan()

            .addOnSuccessListener { barcode ->

                val value =
                    barcode.rawValue

                if (value.isNullOrBlank()) {
                    status("QR code is empty")
                    return@addOnSuccessListener
                }

                processPairingQr(value)
            }

            .addOnCanceledListener {

                status("QR scan cancelled")
            }

            .addOnFailureListener { error ->

                status(
                    "QR scanner error: ${error.message}"
                )
            }
    }

    /*
     * Expected QR:
     *
     * guardianlink://pair
     * ?server=http://192.168.x.x:8000
     * &pairing_id=XXXXXXXX
     */

    private fun processPairingQr(qrValue: String) {

        try {

            val uri =
                Uri.parse(qrValue)

            if (
                uri.scheme != "guardianlink" ||
                uri.host != "pair"
            ) {

                status("Invalid GuardianLink QR code")
                return
            }

            val server =
                uri.getQueryParameter("server")
                    ?.trim()
                    ?.trimEnd('/')

            val pairingId =
                uri.getQueryParameter("pairing_id")
                    ?.trim()

            if (
                server.isNullOrBlank() ||
                pairingId.isNullOrBlank()
            ) {

                status(
                    "QR code does not contain pairing information"
                )

                return
            }

            /*
             * Show detected values in UI.
             */

            b.serverUrl.setText(server)

            b.pairingId.setText(pairingId)

            status(
                "QR scanned. Pairing device..."
            )

            /*
             * Automatically pair.
             *
             * User does NOT need to press
             * Pair button afterwards.
             */

            pair()

        } catch (e: Exception) {

            status(
                "Invalid QR code: ${e.message}"
            )
        }
    }

    /*
     * ---------------------------------------------------------
     * PAIR DEVICE
     * ---------------------------------------------------------
     */

    private fun pair() {

        val server =
            b.serverUrl.text
                .toString()
                .trim()
                .trimEnd('/')

        val pairingId =
            b.pairingId.text
                .toString()
                .trim()

        val name =
            b.deviceName.text
                .toString()
                .trim()

        if (
            server.isBlank() ||
            pairingId.isBlank()
        ) {

            toast(
                "Scan QR or enter server URL and pairing ID"
            )

            return
        }

        status("Pairing device...")

        val body =
            JSONObject()
                .put(
                    "device_name",
                    name
                )
                .put(
                    "model",
                    android.os.Build.MODEL
                )
                .put(
                    "android_version",
                    android.os.Build.VERSION.RELEASE
                )
                .toString()
                .toRequestBody(json)

        thread {

            try {

                val request =
                    Request.Builder()
                        .url(
                            "$server/api/pairing/$pairingId/claim"
                        )
                        .post(body)
                        .build()

                http.newCall(request)
                    .execute()
                    .use { response ->

                        val text =
                            response.body
                                ?.string()
                                .orEmpty()

                        if (
                            response.isSuccessful
                        ) {

                            val result =
                                JSONObject(text)

                            val deviceId =
                                result.getString(
                                    "device_id"
                                )

                            val deviceToken =
                                result.getString(
                                    "device_token"
                                )

                            prefs.edit()
                                .putString(
                                    "server",
                                    server
                                )
                                .putString(
                                    "device_id",
                                    deviceId
                                )
                                .putString(
                                    "device_token",
                                    deviceToken
                                )
                                .apply()

                            status(
                                "Paired successfully ✓"
                            )

                        } else {

                            status(
                                "Pairing failed: ${response.code}"
                            )
                        }
                    }

            } catch (e: Exception) {

                status(
                    e.message ?: "Pairing error"
                )
            }
        }
    }

    /*
     * ---------------------------------------------------------
     * HEARTBEAT
     * ---------------------------------------------------------
     */

    private fun heartbeat() {

        val state =
            state() ?: return

        thread {

            try {

                val request =
                    Request.Builder()
                        .url(
                            "${state.first}/api/devices/${state.second}/heartbeat"
                        )
                        .header(
                            "Authorization",
                            "Bearer ${state.third}"
                        )
                        .post(
                            "{}".toRequestBody(json)
                        )
                        .build()

                http.newCall(request)
                    .execute()
                    .use {

                        status(
                            if (
                                it.isSuccessful
                            )
                                "Heartbeat sent"
                            else
                                "Heartbeat failed"
                        )
                    }

            } catch (e: Exception) {

                status(
                    e.message ?: "Error"
                )
            }
        }
    }

    /*
     * ---------------------------------------------------------
     * COMMANDS
     * ---------------------------------------------------------
     */

    private fun checkCommand() {

        val state =
            state() ?: return

        thread {

            try {

                val request =
                    Request.Builder()
                        .url(
                            "${state.first}/api/devices/${state.second}/commands/next"
                        )
                        .header(
                            "Authorization",
                            "Bearer ${state.third}"
                        )
                        .build()

                http.newCall(request)
                    .execute()
                    .use {

                        val responseText =
                            it.body
                                ?.string()
                                .orEmpty()

                        if (!it.isSuccessful) {

                            status(
                                "Command request failed"
                            )

                            return@use
                        }

                        val result =
                            JSONObject(
                                responseText
                            )

                        if (
                            result.isNull(
                                "command"
                            )
                        ) {

                            status(
                                "No commands"
                            )

                        } else {

                            val command =
                                result.getString(
                                    "command"
                                )

                            status(
                                "Command: $command"
                            )
                        }
                    }

            } catch (e: Exception) {

                status(
                    e.message ?: "Error"
                )
            }
        }
    }

    /*
     * ---------------------------------------------------------
     * STORED DEVICE STATE
     * ---------------------------------------------------------
     */

    private fun state():
        Triple<String, String, String>? {

        val server =
            prefs.getString(
                "server",
                null
            )

        val id =
            prefs.getString(
                "device_id",
                null
            )

        val token =
            prefs.getString(
                "device_token",
                null
            )

        if (
            server == null ||
            id == null ||
            token == null
        ) {

            toast(
                "Pair this device first"
            )

            return null
        }

        return Triple(
            server,
            id,
            token
        )
    }

    private fun status(
        text: String
    ) {

        runOnUiThread {

            b.status.text =
                text
        }
    }

    private fun toast(
        text: String
    ) {

        runOnUiThread {

            Toast.makeText(
                this,
                text,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
