package com.goreecloud.filemanager.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileStatusTest {
    @Test
    fun syncedFileDoesNotBecomeRecoverable() {
        val status = PlatformFileStatus(
            sync = SyncState.SYNCED,
            syncEvidence = EvidenceState.VERIFIED,
            backup = BackupState.UNKNOWN,
            backupEvidence = EvidenceState.UNKNOWN,
        )

        assertFalse(status.canClaimRecoverable())
    }

    @Test
    fun securityRequiresVerifiedWardveilEvidence() {
        assertFalse(
            PlatformFileStatus(
                security = SecurityState.CLEAN,
                securityEvidence = EvidenceState.UNKNOWN,
            ).canClaimSecurityClean(),
        )

        assertTrue(
            PlatformFileStatus(
                security = SecurityState.CLEAN,
                securityEvidence = EvidenceState.VERIFIED,
            ).canClaimSecurityClean(),
        )
    }
}
