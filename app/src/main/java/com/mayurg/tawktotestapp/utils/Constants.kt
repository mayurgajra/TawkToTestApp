package com.mayurg.tawktotestapp.utils

object Constants {


    /**
     * Basic auth with "username:password" to get more quota of requests for github api
     * e.g val auth = "Basic " + Base64.encode("username:password".toByteArray(), 0)
     */
    val auth = ""

    /**
     * Name of the users database fetched from github
     */
    const val GITHUB_DATABASE_NAME = "github_db"

    /**
     * Starting url to make api calls
     */
    const val BASE_URL = "https://api.github.com/"

    /**
     * Argument name to be passed from list to profile activity
     */
    const val ARG_LOGIN_ID = "login"
}