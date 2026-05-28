package com.chumakov123.outageschedule.data.remote.datasource

import org.jsoup.Jsoup

class OutageRemoteDataSource {

    private val baseUrl = "https://www.donenergo.ru/grafik-otklyucheniy/"
    fun fetchHtml(branchUrl: String): String {
        return Jsoup.connect(baseUrl+branchUrl)
            .timeout(15_000)
            .get()
            .html()
    }
}