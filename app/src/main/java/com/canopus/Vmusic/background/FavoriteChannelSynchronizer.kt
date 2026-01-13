package com.canopus.Vmusic.background

import com.canopus.Vmusic.data.api.AuthenticatedMusicdexApiService
import com.canopus.Vmusic.data.api.HolodexApiService
import com.canopus.Vmusic.data.api.PatchOperation
import com.canopus.Vmusic.data.db.UnifiedMetadataEntity
import com.canopus.Vmusic.data.repository.SyncRepository
import javax.inject.Inject

class FavoriteChannelSynchronizer @Inject constructor(
    private val syncRepository: SyncRepository,
    private val authApiService: AuthenticatedMusicdexApiService,
    private val holodexApiService: HolodexApiService,
    private val logger: SyncLogger
) : ISynchronizer {

    override val name: String = "FAV_CHANNELS"
    private val TYPE = "FAV_CHANNEL"

    override suspend fun synchronize(): Boolean {
        logger.startSection(name)
        try {


            val pendingDeletes = syncRepository.getPendingDeleteItems(TYPE)
            val dirtyItems = syncRepository.getDirtyItems(TYPE)


            val validDirtyItems = mutableListOf<com.canopus.Vmusic.data.db.UserInteractionEntity>()

            for (item in dirtyItems) {
                val meta = syncRepository.getMetadata(item.itemId)


                if (meta?.org != "External" && meta?.artistName != "External") {
                    validDirtyItems.add(item)
                } else {

                    logger.info("  Skipping external channel sync for: ${meta?.title} (${item.itemId})")
                    syncRepository.markAsSynced(item.itemId, TYPE, "local_only")
                }
            }


            if (pendingDeletes.isNotEmpty() || validDirtyItems.isNotEmpty()) {
                logger.info("Phase 1: Sending PATCH with ${pendingDeletes.size} removals, ${validDirtyItems.size} additions.")

                val patchOps = mutableListOf<PatchOperation>()
                pendingDeletes.forEach { patchOps.add(PatchOperation("remove", it.itemId)) }
                validDirtyItems.forEach { patchOps.add(PatchOperation("add", it.itemId)) }

                val response = authApiService.patchFavoriteChannels(patchOps)

                if (response.isSuccessful) {
                    if (pendingDeletes.isNotEmpty()) syncRepository.confirmBatchDeletion(
                        pendingDeletes.map { it.itemId },
                        TYPE
                    )
                    if (validDirtyItems.isNotEmpty()) syncRepository.markBatchSynced(
                        validDirtyItems.map { it.itemId },
                        TYPE
                    )
                    logger.info("  -> Upstream PATCH successful.")
                } else {
                    logger.warning("  -> Upstream PATCH failed: ${response.code()}")
                    logger.endSection(name, false)
                    return false
                }
            } else {
                logger.info("Phase 1: No local changes to push.")
            }




            logger.info("Phase 2: Fetching remote channels and reconciling...")

            val remoteRes = holodexApiService.getFavoriteChannels()
            if (!remoteRes.isSuccessful) throw Exception("Failed to get remote channels: ${remoteRes.code()}")

            val remoteChannels = remoteRes.body() ?: emptyList()
            val localSynced = syncRepository.getSyncedItems(TYPE)

            val remoteIdMap = remoteChannels.associateBy { it.id }
            val localIdMap = localSynced.associateBy { it.itemId }


            val newFromServer = remoteChannels.filter { !localIdMap.containsKey(it.id) }
            if (newFromServer.isNotEmpty()) logger.info("  -> Found ${newFromServer.size} new channels from server.")

            for (remote in newFromServer) {
                val meta = UnifiedMetadataEntity(
                    id = remote.id,
                    title = remote.name ?: remote.english_name ?: "Unknown Channel",
                    artistName = remote.org ?: "",
                    type = "CHANNEL",
                    specificArtUrl = remote.photo,
                    uploaderAvatarUrl = remote.photo,
                    duration = 0,
                    channelId = remote.id,
                    description = null,
                    org = remote.org
                )


                syncRepository.insertRemoteItem(remote.id, TYPE, remote.id, meta)
                logger.logItemAction(
                    LogAction.DOWNSTREAM_INSERT_LOCAL,
                    remote.name,
                    null,
                    remote.id
                )
            }


            val deletedOnServer = localSynced.filter { !remoteIdMap.containsKey(it.itemId) }
            if (deletedOnServer.isNotEmpty()) logger.info("  -> Found ${deletedOnServer.size} channels removed on server.")

            for (local in deletedOnServer) {
                syncRepository.removeRemoteItem(local.itemId, TYPE)
                logger.logItemAction(
                    LogAction.DOWNSTREAM_DELETE_LOCAL,
                    local.itemId,
                    null,
                    local.serverId
                )
            }

            logger.endSection(name, true)
            return true
        } catch (e: Exception) {
            logger.error(e, "Channel Sync Failed")
            logger.endSection(name, false)
            return false
        }
    }
}