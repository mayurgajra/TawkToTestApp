package com.mayurg.tawktotestapp.data.remote

import com.mayurg.tawktotestapp.data.entities.ModelUserResponseItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API calls to fetch data from github
 */
interface GithubAPI {

    /**
     * Gets the user's list from github api
     * @param since  is the user id that indicates from where api has to start fetching [e.g after id=30]
     */
    @GET("users")
    suspend fun getUserListing(
        @Header("Authorization") authorization: String,
        @Query("since") since: Int
    ): Response<List<ModelUserResponseItem>>

    /**
     * Gets the user from github api
     * @param login is login id of the user which we require info about
     */
    @GET("users/{login}")
    suspend fun getUser(
        @Header("Authorization") authorization: String,
        @Path("login") login: String
    ): Response<ModelUserResponseItem>
}