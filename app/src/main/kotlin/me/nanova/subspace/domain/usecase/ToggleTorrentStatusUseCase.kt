package me.nanova.subspace.domain.usecase

import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject

class ToggleTorrentStatusUseCase @Inject constructor(
    private val torrentRepo: TorrentRepo,
) {
    suspend operator fun invoke(torrent: Torrent): Result<Unit> {
        return runCatching {
            if (torrent.status.isStopped) {
                torrentRepo.start(listOf(torrent.hash))
            } else {
                torrentRepo.stop(listOf(torrent.hash))
            }
        }
    }
}
