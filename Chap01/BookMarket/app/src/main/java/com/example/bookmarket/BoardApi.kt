package com.example.bookmarket

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.util.concurrent.Executors

object BoardApi {
    private const val BASE_URL = "http://10.0.2.2:8765"
    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    fun request(path: String, data: JSONObject? = null, callback: (Result<JSONObject>) -> Unit) {
        executor.execute {
            val result = runCatching {
                val connection = URI.create(BASE_URL + path).toURL().openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = 8000
                    connection.readTimeout = 8000
                    connection.setRequestProperty("Accept", "application/json")
                    if (data != null) {
                        connection.requestMethod = "POST"
                        connection.doOutput = true
                        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                        connection.outputStream.use { it.write(data.toString().toByteArray(Charsets.UTF_8)) }
                    }
                    val code = connection.responseCode
                    val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                    val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                    val json = JSONObject(body)
                    if (code !in 200..299) error(json.optString("error", "서버 요청에 실패했습니다"))
                    json
                } finally {
                    connection.disconnect()
                }
            }
            main.post { callback(result) }
        }
    }
}
