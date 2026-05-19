package com.ssafy.ssabree.core.utils

import com.google.gson.GsonBuilder
import com.ssafy.ssabree.app.ApplicationClass
import com.ssafy.ssabree.core.datasource.local.AuthDataStore
import com.ssafy.ssabree.core.datasource.remote.AuthService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

//import com.ssafy.ssabree.BuildConfig


object RetrofitClient {
    const val SERVER_URL = "http://10.0.2.2:8080/"

    private val authDataStore by lazy {
        AuthDataStore(ApplicationClass.encryptedSharedPrefManager)
    }

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val refreshClient: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(5000, TimeUnit.MILLISECONDS)
        .connectTimeout(5000, TimeUnit.MILLISECONDS)
        .build()

    private val refreshService: AuthService by lazy {
        Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .client(refreshClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthService::class.java)
    }

    val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(5000, TimeUnit.MILLISECONDS)
        .connectTimeout(5000, TimeUnit.MILLISECONDS)
        .addInterceptor(AuthInterceptor(authDataStore))
        .authenticator(AuthAuthenticator(authDataStore, refreshService))
//        .addInterceptor(AddCookiesInterceptor())
//        .addInterceptor(ReceivedCookiesInterceptor())
        // 로그캣에 okhttp.OkHttpClient로 검색하면 http 통신 내용을 보여줍니다
//            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    // 이미지 업로드용 클라이언트 (타임아웃 60초)
    private val uploadClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .callTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor(authDataStore))
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .authenticator(AuthAuthenticator(authDataStore, refreshService))
        .build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create(gson)
            ).build()
    }

    // 이미지 업로드용 Retrofit 인스턴스
    val uploadInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .client(uploadClient)
            .addConverterFactory(
                GsonConverterFactory.create(gson)
            ).build()
    }


//    val commentService = instance.create(CommentService::class.java)
//    val orderService = ApplicationClass.retrofit.create(OrderService::class.java)
//    val productService = ApplicationClass.retrofit.create(ProductService::class.java)
//    val userService = instance.create(UserService::class.java)

}
