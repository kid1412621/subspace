package me.nanova.subspace.domain.usecase

import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject

class RecheckTorrentUseCase @Inject constructor(
    private val torrentRepo: TorrentRepo
) {
    suspend operator fun invoke(hashes: List<String>): Result<Unit> {
        return runCatching {
            torrentRepo.recheck(hashes)
        }
    }
}
