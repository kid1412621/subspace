package me.nanova.subspace.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TorrentStatusTest {
    @Test
    fun downloadingIsSharedByBothClients() {
        AccountType.entries.forEach { clientType ->
            assertEquals(
                TorrentStatus.DOWNLOADING,
                TorrentStatus.from(clientType, " Downloading ", 0.5f),
            )
        }
    }

    @Test
    fun clientSpecificAliasesUseTheirOwnMapper() {
        assertEquals(TorrentStatus.DOWNLOADING, TorrentStatus.fromQBittorrent("forcedMetaDL"))
        assertEquals(TorrentStatus.UNKNOWN, TorrentStatus.fromTransmission("forcedMetaDL"))
        assertEquals(TorrentStatus.DOWNLOADING, TorrentStatus.fromTransmission("4"))
        assertEquals(TorrentStatus.UNKNOWN, TorrentStatus.fromQBittorrent("4"))
        assertEquals(TorrentStatus.QUEUED, TorrentStatus.fromTransmission("tr_status_check_wait"))
        assertEquals(TorrentStatus.QUEUED, TorrentStatus.fromTransmission("tr_status_seed_wait"))
    }

    @Test
    fun completedAndIncompleteTorrentsKeepTheirStoppedStatus() {
        assertEquals(TorrentStatus.PAUSED, TorrentStatus.fromQBittorrent("pausedDL"))
        assertEquals(TorrentStatus.COMPLETED, TorrentStatus.fromQBittorrent("stoppedUP"))
        assertEquals(TorrentStatus.STOPPED, TorrentStatus.fromTransmission("0", 0.5f))
        assertEquals(TorrentStatus.COMPLETED, TorrentStatus.fromTransmission("0", 1.0f))
    }
}
