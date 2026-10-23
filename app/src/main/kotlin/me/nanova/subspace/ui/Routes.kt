package me.nanova.subspace.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Serializable
data object AccountCreationRoute : NavKey

@Serializable
data class AccountDetailsRoute(val accountId: Long) : NavKey
