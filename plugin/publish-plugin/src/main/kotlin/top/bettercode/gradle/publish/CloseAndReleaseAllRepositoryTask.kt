package io.codearte.gradle.nexus

import io.codearte.gradle.nexus.BaseStagingTask
import io.codearte.gradle.nexus.NexusStagingExtension
import io.codearte.gradle.nexus.logic.AllRepositoryFetcher
import io.codearte.gradle.nexus.logic.RepositoryState
import io.codearte.gradle.nexus.logic.RetryingRepositoryTransitioner
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 *
 * @author Peter Wu
 */
open class CloseAndReleaseAllRepositoryTask @Inject constructor(
    project: Project,
    extension: NexusStagingExtension
) : BaseStagingTask(project, extension) {

    @SuppressWarnings("unused")
    @TaskAction
    fun closeAndReleaseRepository() {
        val stagingProfileId = getConfiguredStagingProfileIdOrFindAndCacheOne(
            createProfileFetcherWithGivenClient(createClient())
        )
        val repositoryFetcher = AllRepositoryFetcher(createClient(), serverUrl)
        val repositoryIds =
            repositoryFetcher.getRepositoryIdWithGivenStateForStagingProfileId(
                stagingProfileId,
                RepositoryState.OPEN
            )
        repositoryIds.forEach {
            closeRepositoryByIdAndProfileIdWithRetrying(it, stagingProfileId)
            releaseRepositoryByIdAndProfileIdWithRetrying(it, stagingProfileId)
        }
    }

    private fun closeRepositoryByIdAndProfileIdWithRetrying(
        repositoryId: String,
        stagingProfileId: String
    ) {
        val repositoryCloser = createRepositoryCloserWithGivenClient(createClient())
        val repositoryStateFetcher = createRepositoryStateFetcherWithGivenClient(createClient())
        val retrier = createOperationRetrier<RepositoryState>()
        val retryingCloser =
            RetryingRepositoryTransitioner(repositoryCloser, repositoryStateFetcher, retrier)

        retryingCloser.performWithRepositoryIdAndStagingProfileId(repositoryId, stagingProfileId)
        logger.info("Repository '$repositoryId' has been effectively closed")
    }

    private fun releaseRepositoryByIdAndProfileIdWithRetrying( repositoryId:String, stagingProfileId:String) {
        val repositoryReleaser = createRepositoryReleaserWithGivenClient(createClient())
        val repositoryStateFetcher = createRepositoryStateFetcherWithGivenClient(createClient())
        val retrier = createOperationRetrier<RepositoryState>()
        val retryingReleaser = RetryingRepositoryTransitioner(repositoryReleaser, repositoryStateFetcher, retrier)

        retryingReleaser.performWithRepositoryIdAndStagingProfileId(repositoryId, stagingProfileId)
        logger.info("Repository '$repositoryId' has been effectively released")
    }
}