package me.nanova.subspace.data.api.dto

import com.squareup.moshi.Json
import me.nanova.subspace.domain.model.FilePriority
import me.nanova.subspace.domain.model.ServerTransferInfo
import me.nanova.subspace.domain.model.TorrentFile
import me.nanova.subspace.domain.model.TorrentPeer
import me.nanova.subspace.domain.model.TorrentProperties
import me.nanova.subspace.domain.model.TorrentTracker

data class TorrentPropertiesDto(
    @Json(name = "save_path") val savePath: String? = null,
    @Json(name = "creation_date") val creationDate: Long? = null,
    @Json(name = "piece_size") val pieceSize: Long? = null,
    @Json(name = "pieces_num") val piecesNum: Long? = null,
    @Json(name = "pieces_have") val piecesHave: Long? = null,
    @Json(name = "total_wasted") val totalWasted: Long? = null,
    @Json(name = "total_uploaded") val totalUploaded: Long? = null,
    @Json(name = "total_downloaded") val totalDownloaded: Long? = null,
    @Json(name = "total_size") val totalSize: Long? = null,
    @Json(name = "time_elapsed") val timeElapsed: Long? = null,
    @Json(name = "seeding_time") val seedingTime: Long? = null,
    @Json(name = "nb_connections") val nbConnections: Long? = null,
    @Json(name = "share_ratio") val shareRatio: Float? = null,
    @Json(name = "addition_date") val additionDate: Long? = null,
    @Json(name = "completion_date") val completionDate: Long? = null,
    @Json(name = "dl_speed") val dlSpeed: Long? = null,
    @Json(name = "up_speed") val upSpeed: Long? = null,
    val eta: Long? = null,
)

fun TorrentPropertiesDto.toDomain() = TorrentProperties(
    savePath = savePath.orEmpty(),
    creationDate = creationDate ?: 0,
    pieceSize = pieceSize ?: 0,
    piecesNum = piecesNum ?: 0,
    piecesHave = piecesHave ?: 0,
    totalWasted = totalWasted ?: 0,
    totalUploaded = totalUploaded ?: 0,
    totalDownloaded = totalDownloaded ?: 0,
    totalSize = totalSize ?: 0,
    timeElapsed = timeElapsed ?: 0,
    seedingTime = seedingTime ?: 0,
    nbConnections = nbConnections ?: 0,
    shareRatio = shareRatio ?: 0f,
    additionDate = additionDate ?: 0,
    completionDate = completionDate ?: 0,
    dlSpeed = dlSpeed ?: 0,
    upSpeed = upSpeed ?: 0,
    eta = eta ?: 0,
)

data class TorrentFileDto(
    val index: Int? = null,
    val name: String = "",
    val size: Long = 0,
    val progress: Float = 0f,
    val priority: Int = 1,
    @Json(name = "is_seed") val isSeed: Boolean? = null,
)

fun TorrentFileDto.toDomain(fallbackIndex: Int) = TorrentFile(
    index = index ?: fallbackIndex,
    name = name,
    size = size,
    progress = progress,
    priority = FilePriority.fromValue(priority),
    isSeed = isSeed ?: false,
)

data class TorrentTrackerDto(
    val url: String = "",
    val status: Int = 0,
    val tier: Int = 0,
    @Json(name = "num_peers") val numPeers: Int = 0,
    @Json(name = "num_seeds") val numSeeds: Int = 0,
    @Json(name = "num_leeches") val numLeeches: Int = 0,
    @Json(name = "num_downloaded") val numDownloaded: Int = 0,
    val msg: String = "",
)

fun TorrentTrackerDto.toDomain() = TorrentTracker(
    url = url,
    status = when (status) {
        0 -> "Disabled"
        1 -> "Not contacted"
        2 -> "Working"
        3 -> "Updating"
        4 -> "Not working"
        else -> "Unknown ($status)"
    },
    tier = tier,
    numPeers = numPeers,
    numSeeds = numSeeds,
    numLeeches = numLeeches,
    numDownloaded = numDownloaded,
    message = msg,
)

data class TorrentPeersResponseDto(
    val peers: Map<String, TorrentPeerDto>? = null,
)

data class TorrentPeerDto(
    val ip: String? = null,
    val port: Int? = null,
    val client: String? = null,
    val progress: Float? = null,
    @Json(name = "dl_speed") val dlSpeed: Long? = null,
    @Json(name = "up_speed") val upSpeed: Long? = null,
    val country: String? = null,
    @Json(name = "country_code") val countryCode: String? = null,
    val flags: String? = null,
)

fun TorrentPeerDto.toDomain(fallbackIp: String, fallbackPort: Int) = TorrentPeer(
    ip = ip ?: fallbackIp,
    port = port ?: fallbackPort,
    client = client.orEmpty(),
    progress = progress ?: 0f,
    downSpeed = dlSpeed ?: 0,
    upSpeed = upSpeed ?: 0,
    country = country.orEmpty(),
    countryCode = countryCode.orEmpty(),
    flags = flags.orEmpty(),
)

data class TransferInfoDto(
    @Json(name = "dl_info_speed") val dlInfoSpeed: Long? = null,
    @Json(name = "up_info_speed") val upInfoSpeed: Long? = null,
    @Json(name = "dl_info_data") val dlInfoData: Long? = null,
    @Json(name = "up_info_data") val upInfoData: Long? = null,
    @Json(name = "connection_status") val connectionStatus: String? = null,
    @Json(name = "dht_nodes") val dhtNodes: Long? = null,
)

fun TransferInfoDto.toDomain(altSpeedEnabled: Boolean = false) = ServerTransferInfo(
    dlInfoSpeed = dlInfoSpeed ?: 0,
    upInfoSpeed = upInfoSpeed ?: 0,
    dlInfoData = dlInfoData ?: 0,
    upInfoData = upInfoData ?: 0,
    connectionStatus = connectionStatus ?: "connected",
    dhtNodes = dhtNodes ?: 0,
    altSpeedLimitsEnabled = altSpeedEnabled,
)
