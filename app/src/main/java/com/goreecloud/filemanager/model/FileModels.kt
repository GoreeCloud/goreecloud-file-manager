package com.goreecloud.filemanager.model

import java.time.Instant

enum class FileItemType {
    FILE,
    FOLDER,
}

enum class FileLocationKind {
    APP_PRIVATE_LOCAL,
    LOCAL_DEVICE,
    EXTERNAL,
    NETWORK,
    GOREECLOUD_DRIVE,
    SYNCHRONIZED_DEVICE,
    BACKUP_OR_RECOVERY_RECORD,
}

data class FileEntry(
    val providerId: String,
    val resourceId: String,
    val displayName: String,
    val absolutePath: String,
    val type: FileItemType,
    val locationKind: FileLocationKind,
    val sizeBytes: Long?,
    val modifiedAt: Instant?,
)

enum class EvidenceState {
    VERIFIED,
    PENDING,
    UNKNOWN,
    UNAVAILABLE,
    NOT_APPLICABLE,
}

enum class SyncState {
    SYNCED,
    PENDING,
    ACTIVE,
    CONFLICT,
    ERROR,
    LOCAL_ONLY,
    CLOUD_ONLY,
    UNKNOWN,
}

enum class BackupState {
    RECOVERABLE,
    EXISTS_UNVERIFIED,
    PENDING,
    NOT_PROTECTED,
    UNKNOWN,
}

enum class ContinuityState {
    VERIFIED,
    PROTECTED_UNVERIFIED,
    PRESERVATION_PENDING,
    NOT_PROTECTED,
    UNKNOWN,
}

enum class PrivacyState {
    ALLOWED,
    ALLOWED_WITH_CONSTRAINTS,
    REQUIRES_USER_DECISION,
    DENIED,
    UNKNOWN,
}

enum class SecurityState {
    CLEAN,
    SUSPICIOUS,
    MALICIOUS,
    HELD,
    UNSUPPORTED,
    UNKNOWN,
}

data class AuthorityEvidence(
    val producer: String,
    val state: EvidenceState,
    val observedAt: Instant? = null,
    val expiresAt: Instant? = null,
    val reference: String? = null,
)

data class PlatformFileStatus(
    val sync: SyncState = SyncState.UNKNOWN,
    val syncEvidence: EvidenceState = EvidenceState.UNKNOWN,
    val backup: BackupState = BackupState.UNKNOWN,
    val backupEvidence: EvidenceState = EvidenceState.UNKNOWN,
    val continuity: ContinuityState = ContinuityState.UNKNOWN,
    val continuityEvidence: EvidenceState = EvidenceState.UNKNOWN,
    val privacy: PrivacyState = PrivacyState.UNKNOWN,
    val privacyEvidence: EvidenceState = EvidenceState.UNKNOWN,
    val security: SecurityState = SecurityState.UNKNOWN,
    val securityEvidence: EvidenceState = EvidenceState.UNKNOWN,
) {
    fun canClaimSecurityClean(): Boolean =
        security == SecurityState.CLEAN && securityEvidence == EvidenceState.VERIFIED

    fun canClaimRecoverable(): Boolean =
        backup == BackupState.RECOVERABLE && backupEvidence == EvidenceState.VERIFIED
}
