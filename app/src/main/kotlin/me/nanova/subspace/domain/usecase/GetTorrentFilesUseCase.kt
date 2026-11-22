package me.nanova.subspace.domain.usecase

import me.nanova.subspace.domain.model.TorrentFile
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject

class GetTorrentFilesUseCase @Inject constructor(
    private val torrentRepo: TorrentRepo
) {
    suspend operator fun invoke(hash: String): Result<List<TorrentFile>> {
        return runCatching {
            torrentRepo.getTorrentFiles(hash)
        }
    }
}
