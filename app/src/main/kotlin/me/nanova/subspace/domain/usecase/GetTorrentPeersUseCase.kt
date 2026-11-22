package me.nanova.subspace.domain.usecase

import me.nanova.subspace.domain.model.TorrentPeer
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject

class GetTorrentPeersUseCase @Inject constructor(
    private val torrentRepo: TorrentRepo
) {
    suspend operator fun invoke(hash: String): Result<List<TorrentPeer>> {
        return runCatching {
            torrentRepo.getTorrentPeers(hash)
        }
    }
}
