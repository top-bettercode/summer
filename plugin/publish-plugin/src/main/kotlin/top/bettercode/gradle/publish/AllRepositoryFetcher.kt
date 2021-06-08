package io.codearte.gradle.nexus.logic

import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import io.codearte.gradle.nexus.logic.BaseOperationExecutor
import io.codearte.gradle.nexus.logic.RepositoryState
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author Peter Wu
 */
class AllRepositoryFetcher(
    client: SimplifiedHttpJsonRestClient,
    nexusUrl: String
) : BaseOperationExecutor(client, nexusUrl) {
    private val log: Logger = LoggerFactory.getLogger(RepositoryFetcher::class.java)

    fun getRepositoryIdWithGivenStateForStagingProfileId(
        stagingProfileId: String,
        state: RepositoryState
    ): List<String> {
        log.info("Getting '$state' repository for staging profile '$stagingProfileId'")
        val allStagingRepositoriesResponseAsMap =
            client.get("$nexusUrl/staging/profile_repositories/$stagingProfileId")
        val data: List<Map<String, Any?>> =
            allStagingRepositoriesResponseAsMap["data"] as List<Map<String, Any?>>
        return data.filter { it["type"] == state.toString() }
            .map { it["repositoryId"] as String }
    }

}