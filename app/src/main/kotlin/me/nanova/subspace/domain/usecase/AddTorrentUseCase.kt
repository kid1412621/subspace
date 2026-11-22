package me.nanova.subspace.domain.usecase

import me.nanova.subspace.domain.model.AddTorrentParams
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject

class AddTorrentUseCase @Inject constructor(
    private val torrentRepo: TorrentRepo
) {
    suspend operator fun invoke(params: AddTorrentParams): Result<Unit> {
        return runCatching {
            torrentRepo.addTorrent(params)
        }
    }
}
