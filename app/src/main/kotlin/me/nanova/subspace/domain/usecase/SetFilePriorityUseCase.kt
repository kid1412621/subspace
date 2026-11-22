package me.nanova.subspace.domain.usecase

import me.nanova.subspace.domain.model.FilePriority
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject

class SetFilePriorityUseCase @Inject constructor(
    private val torrentRepo: TorrentRepo
) {
    suspend operator fun invoke(
        hash: String,
        fileIds: List<Int>,
        priority: FilePriority
    ): Result<Unit> {
        return runCatching {
            torrentRepo.setFilePriority(hash, fileIds, priority)
        }
    }
}
