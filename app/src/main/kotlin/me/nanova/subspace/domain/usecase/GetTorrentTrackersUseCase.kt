package me.nanova.subspace.domain.usecase

import me.nanova.subspace.domain.model.TorrentTracker
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject

class GetTorrentTrackersUseCase @Inject constructor(
    private val torrentRepo: TorrentRepo
) {
    suspend operator fun invoke(hash: String): Result<List<TorrentTracker>> {
        return runCatching {
            torrentRepo.getTorrentTrackers(hash)
        }
    }
}
