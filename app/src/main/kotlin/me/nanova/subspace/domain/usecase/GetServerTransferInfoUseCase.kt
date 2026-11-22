package me.nanova.subspace.domain.usecase

import me.nanova.subspace.domain.model.ServerTransferInfo
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject

class GetServerTransferInfoUseCase @Inject constructor(
    private val torrentRepo: TorrentRepo
) {
    suspend operator fun invoke(): Result<ServerTransferInfo> {
        return runCatching {
            torrentRepo.getTransferInfo()
        }
    }
}
