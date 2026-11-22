package me.nanova.subspace.domain.model

data class TorrentProperties(
    val progress: Float = 0f,
    val savePath: String = "",
    val creationDate: Long = 0,
    val pieceSize: Long = 0,
    val piecesNum: Long = 0,
    val piecesHave: Long = 0,
    val totalWasted: Long = 0,
    val totalUploaded: Long = 0,
    val totalDownloaded: Long = 0,
    val totalSize: Long = 0,
    val timeElapsed: Long = 0,
    val seedingTime: Long = 0,
    val nbConnections: Long = 0,
    val shareRatio: Float = 0f,
    val additionDate: Long = 0,
    val completionDate: Long = 0,
    val dlSpeed: Long = 0,
    val upSpeed: Long = 0,
    val eta: Long = 0,
)

data class TorrentFile(
    val index: Int,
    val name: String,
    val size: Long,
    val progress: Float,
    val priority: FilePriority,
    val isSeed: Boolean = false,
)

enum class FilePriority(val value: Int) {
    DO_NOT_DOWNLOAD(0),
    NORMAL(1),
    HIGH(6),
    MAXIMAL(7);

    companion object {
        fun fromValue(value: Int): FilePriority =
            entries.find { it.value == value } ?: NORMAL
    }
}

data class TorrentTracker(
    val url: String,
    val status: String,
    val tier: Int = 0,
    val numPeers: Int = 0,
    val numSeeds: Int = 0,
    val numLeeches: Int = 0,
    val numDownloaded: Int = 0,
    val message: String = "",
)

data class TorrentPeer(
    val ip: String,
    val port: Int,
    val client: String,
    val progress: Float,
    val downSpeed: Long,
    val upSpeed: Long,
    val country: String = "",
    val countryCode: String = "",
    val flags: String = "",
)

data class ServerTransferInfo(
    val dlInfoSpeed: Long = 0,
    val upInfoSpeed: Long = 0,
    val dlInfoData: Long = 0,
    val upInfoData: Long = 0,
    val connectionStatus: String = "connected",
    val dhtNodes: Long = 0,
    val altSpeedLimitsEnabled: Boolean = false,
)
