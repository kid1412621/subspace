package me.nanova.subspace.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import me.nanova.subspace.data.AccountType

@Entity
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // schema + host + port + path
    val url: String = "",
    val type: AccountType,
    val name: String = "",
    val user: String = "",
    // api key or password
    val pass: String = "",
    val created: Long = System.currentTimeMillis()
)
