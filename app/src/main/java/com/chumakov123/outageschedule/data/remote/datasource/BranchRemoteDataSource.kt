package com.chumakov123.outageschedule.data.remote.datasource

import org.jsoup.Jsoup

class BranchRemoteDataSource {

    private val baseUrl = "https://www.donenergo.ru/grafik-otklyucheniy/"

    fun fetchIndexHtml(): String {
        return Jsoup.connect(baseUrl)
            .timeout(15_000)
            .get()
            .html()
    }
}