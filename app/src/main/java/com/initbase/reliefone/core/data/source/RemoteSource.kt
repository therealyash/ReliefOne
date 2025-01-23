package com.initbase.reliefone.core.data.source

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.initbase.reliefone.BuildConfig
import com.initbase.reliefone.features.sos.data.source.remote.SosService
import com.initbase.reliefone.features.sos.domain.use_cases.ErrorResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


object RemoteSource {
    private const val BASE_URL = "http://dummy.restapiexample.com/api/"

    private fun getRetrofit(context: Context): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val client = OkHttpClient.Builder()
        if (BuildConfig.DEBUG)
            client.addInterceptor(NetworkConnectionInterceptor(context))
                .addInterceptor(loggingInterceptor)


        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .build()
    }

    fun sosService(context: Context): SosService = getRetrofit(context).create(SosService::class.java)
}

class NetworkConnectionInterceptor(val context: Context) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isConnected)
            return Response.Builder()
                .request(chain.request())
                .headers(chain.request().headers)
                .protocol(Protocol.HTTP_1_1)
                .code(503)
                .message("No internet connection")
                .body(Gson().toJson(ErrorResponse(code = 503, message = "No internet connection"))
                    .toResponseBody(chain.request().body?.contentType()))
                .build()

        return chain.proceed(chain.request())
    }

    private val isConnected: Boolean
        get() {
            val connectivityManager =
                ContextCompat.getSystemService(context, ConnectivityManager::class.java) ?: return false

            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

            return if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_USB) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) -> true
                    else -> false
                }
            } else {
                false
            }
        }

}