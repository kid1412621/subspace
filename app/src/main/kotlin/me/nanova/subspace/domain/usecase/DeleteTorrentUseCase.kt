package me.nanova.subspace.domain.usecase

import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject

class DeleteTorrentUseCase @Inject constructor(
    private val torrentRepo: TorrentRepo
) {
    suspend operator fun invoke(hashes: List<String>, deleteFiles: Boolean): Result<Unit> {
        return runCatching {
            torrentRepo.delete(hashes, deleteFiles)
        }
    }
}
