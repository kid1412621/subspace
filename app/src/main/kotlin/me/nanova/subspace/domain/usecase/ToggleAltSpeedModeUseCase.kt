package me.nanova.subspace.domain.usecase

import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject

class ToggleAltSpeedModeUseCase @Inject constructor(
    private val torrentRepo: TorrentRepo
) {
    suspend operator fun invoke(): Result<Unit> {
        return runCatching {
            torrentRepo.toggleSpeedLimitsMode()
        }
    }
}
