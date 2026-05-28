package com.chumakov123.outageschedule.data.repository

import com.chumakov123.outageschedule.data.remote.datasource.BranchRemoteDataSource
import com.chumakov123.outageschedule.data.remote.parser.BranchIndexParser
import com.chumakov123.outageschedule.domain.model.Branch
import com.chumakov123.outageschedule.domain.repository.BranchRepository
import org.jsoup.Jsoup

class DonEnergoBranchRepository(
    private val remote: BranchRemoteDataSource,
    private val parser: BranchIndexParser
) : BranchRepository {

    override suspend fun getBranches(): List<Branch> {
        val html = remote.fetchIndexHtml()
        val doc = Jsoup.parse(html)

        return parser.parse(doc)
    }
}