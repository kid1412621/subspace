package me.nanova.subspace.domain.usecase

import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.AppSettings
import me.nanova.subspace.domain.repo.SettingsRepo
import javax.inject.Inject

class GetAppSettingsUseCase @Inject constructor(
    private val settingsRepo: SettingsRepo
) {
    operator fun invoke(): Flow<AppSettings> = settingsRepo.settings
}
