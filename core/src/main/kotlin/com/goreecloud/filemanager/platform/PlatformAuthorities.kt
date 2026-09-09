package com.goreecloud.filemanager.platform

import com.goreecloud.filemanager.model.AuthorityEvidence
import com.goreecloud.filemanager.model.FileEntry
import com.goreecloud.filemanager.model.PlatformFileStatus

/**
 * These interfaces establish authority boundaries only. Their existence is not runtime acceptance.
 */
interface GoreeCloudDriveGateway {
    suspend fun refresh(entry: FileEntry): FileEntry?
}

interface GoreeCloudSyncEvidenceSource {
    suspend fun evidenceFor(entry: FileEntry): AuthorityEvidence
}

interface GoreeCloudBackupEvidenceSource {
    suspend fun evidenceFor(entry: FileEntry): AuthorityEvidence
}

interface EverkeepEvidenceSource {
    suspend fun evidenceFor(entry: FileEntry): AuthorityEvidence
}

interface PrivacyShieldEvidenceSource {
    suspend fun evidenceFor(entry: FileEntry, operation: String): AuthorityEvidence
}

interface WardveilEvidenceSource {
    suspend fun evidenceFor(entry: FileEntry, operation: String): AuthorityEvidence
}

interface GoreeCloudIdentityGateway {
    suspend fun accessEvidenceFor(entry: FileEntry, operation: String): AuthorityEvidence
}

interface GoreeCloudMeshGateway {
    suspend fun publishBoundedFileEvent(eventType: String, entry: FileEntry): Result<Unit>
}

interface PlatformStatusComposer {
    suspend fun statusFor(entry: FileEntry): PlatformFileStatus
}
