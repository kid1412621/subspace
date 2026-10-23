package me.nanova.subspace.domain.repo

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    val networkStatus: Flow<Boolean>
}
