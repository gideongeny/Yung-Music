package com.gideongeng.music.desktop

import com.sun.net.httpserver.HttpServer
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

object AudioProxy {
    private var server: HttpServer? = null
    var port: Int = 0
        private set

    fun wrap(targetUrl: String): String {
        start()
        val encoded = URLEncoder.encode(targetUrl, StandardCharsets.UTF_8)
        return "http://127.0.0.1:$port/stream?url=$encoded"
    }

    fun start() {
        if (server != null) return
        server = HttpServer.create(InetSocketAddress(0), 0)
        port = server!!.address.port
        server!!.executor = Executors.newCachedThreadPool()
        server!!.createContext("/stream") { exchange ->
            try {
                println("AudioProxy: Received ${exchange.requestMethod} request: ${exchange.requestURI}")
                val query = exchange.requestURI.query
                val urlParam = query.substringAfter("url=").substringBefore("&")
                val targetUrl = URLDecoder.decode(urlParam, "UTF-8")
                println("AudioProxy: Target URL: ${targetUrl.take(100)}...")

                val url = URL(targetUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = exchange.requestMethod
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                conn.setRequestProperty("Referer", "https://music.youtube.com/")
                conn.setRequestProperty("Origin", "https://music.youtube.com")
                
                // Pass Range header if requested
                val rangeHeader = exchange.requestHeaders.getFirst("Range")
                if (rangeHeader != null) {
                    conn.setRequestProperty("Range", rangeHeader)
                    println("AudioProxy: Range header: $rangeHeader")
                }

                conn.connect()
                
                val responseCode = conn.responseCode
                val contentLength = conn.contentLengthLong
                println("AudioProxy: Response code: $responseCode, Content-Length: $contentLength")

                // Forward response headers
                val responseHeaders = exchange.responseHeaders
                conn.headerFields.forEach { (key, values) ->
                    if (key != null && !key.equals("Transfer-Encoding", ignoreCase = true)) {
                        responseHeaders.put(key, values)
                    }
                }
                
                // Ensure content-type is set for VLC
                if (!responseHeaders.containsKey("Content-Type")) {
                    responseHeaders.set("Content-Type", "audio/webm")
                }
                println("AudioProxy: Content-Type: ${responseHeaders.get("Content-Type")}")

                // Important: handle 206 Partial Content
                if (responseCode == 200 || responseCode == 206) {
                    exchange.sendResponseHeaders(responseCode, if (contentLength > 0) contentLength else 0)
                    if (exchange.requestMethod.equals("GET", ignoreCase = true)) {
                        conn.inputStream.use { input ->
                            exchange.responseBody.use { output ->
                                input.copyTo(output)
                            }
                        }
                        println("AudioProxy: Stream completed successfully")
                    } else {
                        exchange.responseBody.close()
                    }
                } else {
                    println("AudioProxy: Error response code: $responseCode")
                    exchange.sendResponseHeaders(responseCode, -1)
                }
            } catch (e: Exception) {
                println("AudioProxy: Exception: ${e.message}")
                e.printStackTrace()
                try {
                    exchange.sendResponseHeaders(500, -1)
                } catch (ignored: Exception) {}
            } finally {
                exchange.close()
            }
        }
        server!!.start()
    }
}
