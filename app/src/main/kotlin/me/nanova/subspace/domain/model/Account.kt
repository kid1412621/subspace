package me.nanova.subspace.domain.model

data class Account(
    val id: Long = 0,
    // schema + host + port + path
    val url: String = "",
    val type: AccountType,
    val name: String = "",
    val user: String = "",
    // api key or password
    val pass: String = "",
    val created: Long = System.currentTimeMillis(),
    val useLanSwitch: Boolean = false,
    val lanUrl: String = "",
    val lanSSID: String = "",
    val version: String = ""
)
