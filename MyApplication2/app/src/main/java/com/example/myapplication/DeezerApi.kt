package com.example.myapplication


import retrofit2.http.GET
import retrofit2.Response

interface DeezerApi {
    @GET("chart/0/tracks/")
    suspend fun getTopTracks(): Response<TrackResponse>
}