package me.nanova.subspace.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account")
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
    val created: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "use_lan_switch")
    val useLanSwitch: Boolean = false,
    @ColumnInfo(name = "lan_url")
    val lanUrl: String = "",
    @ColumnInfo(name = "lan_ssid")
    val lanSSID: String = "",
)
