package me.nanova.subspace.data.db.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.AccountType

@Entity(tableName = "account")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String = "",
    val type: AccountType,
    val name: String = "",
    val user: String = "",
    val pass: String = "",
    val created: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "use_lan_switch")
    val useLanSwitch: Boolean = false,
    @ColumnInfo(name = "lan_url")
    val lanUrl: String = "",
    @ColumnInfo(name = "lan_ssid")
    val lanSSID: String = "",
    val version: String = ""
)

fun AccountEntity.toDomain() = Account(
    id = id,
    url = url,
    type = type,
    name = name,
    user = user,
    pass = pass,
    created = created,
    useLanSwitch = useLanSwitch,
    lanUrl = lanUrl,
    lanSSID = lanSSID,
    version = version
)

fun Account.toEntity() = AccountEntity(
    id = id,
    url = url,
    type = type,
    name = name,
    user = user,
    pass = pass,
    created = created,
    useLanSwitch = useLanSwitch,
    lanUrl = lanUrl,
    lanSSID = lanSSID,
    version = version
)
