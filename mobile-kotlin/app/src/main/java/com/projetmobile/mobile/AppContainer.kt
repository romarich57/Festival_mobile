package com.projetmobile.mobile

import android.content.Context
import com.projetmobile.mobile.data.remote.auth.AuthApiService
import com.projetmobile.mobile.data.remote.festival.FestivalApiService
import com.projetmobile.mobile.data.database.AuthPreferenceStore
import com.projetmobile.mobile.data.database.PersistentCookieJar
import com.projetmobile.mobile.data.remote.reservation.ReservationApiService
import com.projetmobile.mobile.data.repository.reservation.ReservationRepository
import com.projetmobile.mobile.data.repository.reservation.ReservationRepositoryImpl
import com.projetmobile.mobile.data.repository.auth.AuthRepository
import com.projetmobile.mobile.data.repository.auth.AuthRepositoryImpl
import com.projetmobile.mobile.data.repository.festival.FestivalRepository
import com.projetmobile.mobile.data.repository.festival.FestivalRepositoryImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.projetmobile.mobile.data.remote.common.ApiJson
import com.projetmobile.mobile.data.repository.workflow.WorkflowRepository
import com.projetmobile.mobile.data.repository.workflow.WorkflowRepositoryImpl
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AppContainer(context: Context) {
    private val applicationContext = context.applicationContext

    private val authPreferenceStore by lazy {
        AuthPreferenceStore(applicationContext)
    }

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }
    }

    private val moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val cookieJar by lazy {
        PersistentCookieJar(applicationContext)
    }

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .cookieJar(cookieJar)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(httpClient)
            .addConverterFactory(ApiJson.instance.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val authApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    private val festivalApiService by lazy {
        retrofit.create(FestivalApiService::class.java)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            authApiService = authApiService,
            authPreferenceStore = authPreferenceStore,
        )
    }

    val festivalRepository: FestivalRepository by lazy {
        FestivalRepositoryImpl(festivalApiService)
    }

    private val reservationApiService: ReservationApiService by lazy {
        retrofit.create(ReservationApiService::class.java)
    }

    val reservationRepository: ReservationRepository by lazy {
        ReservationRepositoryImpl(reservationApiService)
    }

    val workflowRepository: WorkflowRepository by lazy {
        WorkflowRepositoryImpl(reservationApiService)
    }
}
