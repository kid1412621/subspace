package me.nanova.subspace.data.mapper

import me.nanova.subspace.data.model.qb.QBState
import me.nanova.subspace.data.model.tr.TRState
import me.nanova.subspace.domain.model.DomainTorrentState
import org.junit.Assert.assertEquals
import org.junit.Test

class TorrentStateMapperTest {

    // --- QBState Enum to DomainTorrentState ---
    @Test
    fun `mapQBStateToDomainState with QBState ERROR should return ERROR`() {
        assertEquals(DomainTorrentState.ERROR, mapQBStateToDomainState(QBState.ERROR))
    }

    @Test
    fun `mapQBStateToDomainState with QBState PAUSED_UP should return PAUSED`() {
        assertEquals(DomainTorrentState.PAUSED, mapQBStateToDomainState(QBState.PAUSED_UP))
    }

    @Test
    fun `mapQBStateToDomainState with QBState PAUSED_DL should return PAUSED`() {
        assertEquals(DomainTorrentState.PAUSED, mapQBStateToDomainState(QBState.PAUSED_DL))
    }

    @Test
    fun `mapQBStateToDomainState with QBState UPLOADING should return SEEDING`() {
        assertEquals(DomainTorrentState.SEEDING, mapQBStateToDomainState(QBState.UPLOADING))
    }

    @Test
    fun `mapQBStateToDomainState with QBState STALLED_UP should return SEEDING`() {
        assertEquals(DomainTorrentState.SEEDING, mapQBStateToDomainState(QBState.STALLED_UP))
    }

    @Test
    fun `mapQBStateToDomainState with QBState CHECKING_UP should return CHECKING`() {
        assertEquals(DomainTorrentState.CHECKING, mapQBStateToDomainState(QBState.CHECKING_UP))
    }

    @Test
    fun `mapQBStateToDomainState with QBState CHECKING_DL should return CHECKING`() {
        assertEquals(DomainTorrentState.CHECKING, mapQBStateToDomainState(QBState.CHECKING_DL))
    }

     @Test
    fun `mapQBStateToDomainState with QBState CHECKING_RESUME_DATA should return CHECKING`() {
        assertEquals(DomainTorrentState.CHECKING, mapQBStateToDomainState(QBState.CHECKING_RESUME_DATA))
    }

    @Test
    fun `mapQBStateToDomainState with QBState DOWNLOADING should return DOWNLOADING`() {
        assertEquals(DomainTorrentState.DOWNLOADING, mapQBStateToDomainState(QBState.DOWNLOADING))
    }

    @Test
    fun `mapQBStateToDomainState with QBState STALLED_DL should return DOWNLOADING`() {
        assertEquals(DomainTorrentState.DOWNLOADING, mapQBStateToDomainState(QBState.STALLED_DL))
    }

    @Test
    fun `mapQBStateToDomainState with QBState FORCED_DL should return QUEUED`() {
        assertEquals(DomainTorrentState.QUEUED, mapQBStateToDomainState(QBState.FORCED_DL))
    }

    @Test
    fun `mapQBStateToDomainState with QBState FORCED_UP should return QUEUED`() {
        assertEquals(DomainTorrentState.QUEUED, mapQBStateToDomainState(QBState.FORCED_UP))
    }

    @Test
    fun `mapQBStateToDomainState with QBState META_DL should return METADATA_DOWNLOAD`() {
        assertEquals(DomainTorrentState.METADATA_DOWNLOAD, mapQBStateToDomainState(QBState.META_DL))
    }

    @Test
    fun `mapQBStateToDomainState with QBState ALLOCATING should return ALLOCATING`() {
        assertEquals(DomainTorrentState.ALLOCATING, mapQBStateToDomainState(QBState.ALLOCATING))
    }

    @Test
    fun `mapQBStateToDomainState with QBState MOVING should return MOVING`() {
        assertEquals(DomainTorrentState.MOVING, mapQBStateToDomainState(QBState.MOVING))
    }

    @Test
    fun `mapQBStateToDomainState with QBState UNKNOWN should return UNKNOWN`() {
        assertEquals(DomainTorrentState.UNKNOWN, mapQBStateToDomainState(QBState.UNKNOWN))
    }

    @Test
    fun `mapQBStateToDomainState with QBState MISSING_FILES should return UNKNOWN`() {
        assertEquals(DomainTorrentState.UNKNOWN, mapQBStateToDomainState(QBState.MISSING_FILES))
    }


    // --- QBState String to DomainTorrentState ---
    @Test
    fun `mapQBStateToDomainState with string error should return ERROR`() {
        assertEquals(DomainTorrentState.ERROR, mapQBStateToDomainState("error"))
    }

    @Test
    fun `mapQBStateToDomainState with string pausedup should return PAUSED`() {
        assertEquals(DomainTorrentState.PAUSED, mapQBStateToDomainState("pausedup"))
    }

    @Test
    fun `mapQBStateToDomainState with string uploading should return SEEDING`() {
        assertEquals(DomainTorrentState.SEEDING, mapQBStateToDomainState("uploading"))
    }

    @Test
    fun `mapQBStateToDomainState with string checkingdl should return CHECKING`() {
        assertEquals(DomainTorrentState.CHECKING, mapQBStateToDomainState("checkingdl"))
    }

    @Test
    fun `mapQBStateToDomainState with string downloading should return DOWNLOADING`() {
        assertEquals(DomainTorrentState.DOWNLOADING, mapQBStateToDomainState("downloading"))
    }

    @Test
    fun `mapQBStateToDomainState with string forceddl should return QUEUED`() {
        assertEquals(DomainTorrentState.QUEUED, mapQBStateToDomainState("forceddl"))
    }

    @Test
    fun `mapQBStateToDomainState with string metadl should return METADATA_DOWNLOAD`() {
        assertEquals(DomainTorrentState.METADATA_DOWNLOAD, mapQBStateToDomainState("metadl"))
    }

     @Test
    fun `mapQBStateToDomainState with string allocating should return ALLOCATING`() {
        assertEquals(DomainTorrentState.ALLOCATING, mapQBStateToDomainState("allocating"))
    }

    @Test
    fun `mapQBStateToDomainState with string moving should return MOVING`() {
        assertEquals(DomainTorrentState.MOVING, mapQBStateToDomainState("moving"))
    }

    @Test
    fun `mapQBStateToDomainState with string unknown should return UNKNOWN`() {
        assertEquals(DomainTorrentState.UNKNOWN, mapQBStateToDomainState("unknown"))
    }

    @Test
    fun `mapQBStateToDomainState with string missingfiles should return UNKNOWN`() {
        assertEquals(DomainTorrentState.UNKNOWN, mapQBStateToDomainState("missingfiles"))
    }

    @Test
    fun `mapQBStateToDomainState with arbitrary string should return UNKNOWN`() {
        assertEquals(DomainTorrentState.UNKNOWN, mapQBStateToDomainState("some_other_state"))
    }


    // --- TRState Enum to DomainTorrentState ---
    @Test
    fun `mapTRStateToDomainState with TRState STOPPED should return PAUSED`() {
        assertEquals(DomainTorrentState.PAUSED, mapTRStateToDomainState(TRState.STOPPED))
    }

    @Test
    fun `mapTRStateToDomainState with TRState CHECK_WAIT should return CHECKING`() {
        assertEquals(DomainTorrentState.CHECKING, mapTRStateToDomainState(TRState.CHECK_WAIT))
    }

    @Test
    fun `mapTRStateToDomainState with TRState CHECK should return CHECKING`() {
        assertEquals(DomainTorrentState.CHECKING, mapTRStateToDomainState(TRState.CHECK))
    }

    @Test
    fun `mapTRStateToDomainState with TRState DOWNLOAD_WAIT should return QUEUED`() {
        assertEquals(DomainTorrentState.QUEUED, mapTRStateToDomainState(TRState.DOWNLOAD_WAIT))
    }

    @Test
    fun `mapTRStateToDomainState with TRState DOWNLOAD should return DOWNLOADING`() {
        assertEquals(DomainTorrentState.DOWNLOADING, mapTRStateToDomainState(TRState.DOWNLOAD))
    }

    @Test
    fun `mapTRStateToDomainState with TRState SEED_WAIT should return QUEUED`() {
        assertEquals(DomainTorrentState.QUEUED, mapTRStateToDomainState(TRState.SEED_WAIT))
    }

    @Test
    fun `mapTRStateToDomainState with TRState SEED should return SEEDING`() {
        assertEquals(DomainTorrentState.SEEDING, mapTRStateToDomainState(TRState.SEED))
    }

    // TRState does not have a direct UNKNOWN or ERROR in the provided enum,
    // but the mapper's `else` branch handles unmapped states to UNKNOWN.
    // To test this, we would need to add a hypothetical unmapped state to TRState or pass an int.
    // For now, we assume the provided TRState enum covers common cases.
}
