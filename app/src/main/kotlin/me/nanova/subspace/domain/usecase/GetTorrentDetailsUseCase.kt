package me.nanova.subspace.domain.usecase

import me.nanova.subspace.domain.model.TorrentProperties
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject

class GetTorrentDetailsUseCase @Inject constructor(
    private val torrentRepo: TorrentRepo
) {
    suspend operator fun invoke(hash: String): Result<TorrentProperties> {
        return runCatching {
            torrentRepo.getTorrentProperties(hash)
        }
    }
}
