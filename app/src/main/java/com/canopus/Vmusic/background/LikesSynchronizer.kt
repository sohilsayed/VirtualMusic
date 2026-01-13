package com.canopus.Vmusic.background

import com.canopus.Vmusic.data.api.AuthenticatedMusicdexApiService
import com.canopus.Vmusic.data.api.LikeRequest
import com.canopus.Vmusic.data.db.UnifiedMetadataEntity
import com.canopus.Vmusic.data.repository.PlaylistRepository
import com.canopus.Vmusic.data.repository.SyncRepository
import javax.inject.Inject

class LikesSynchronizer @Inject constructor(
    private val syncRepository: SyncRepository,
    private val playlistRepository: PlaylistRepository,
    private val apiService: AuthenticatedMusicdexApiService,
    private val logger: SyncLogger
) : ISynchronizer {

    override val name: String = "LIKES"
    private val TYPE = "LIKE"


    private val PENDING_DELETE_TIMEOUT_MS = 35 * 60 * 1000L

    override suspend fun synchronize(): Boolean {
        logger.startSection(name)
        try {


            logger.info("Phase 0: Checking for orphaned local likes to repair...")
            val dirtyItems = syncRepository.getDirtyItems(TYPE)
            val orphanedLikes = dirtyItems.filter { it.serverId == null }

            if (orphanedLikes.isNotEmpty()) {
                logger.info("  -> Found ${orphanedLikes.size} orphaned items. Attempting to repair...")
                for (orphan in orphanedLikes) {

                    val videoId = orphan.itemId.substringBeforeLast('_')
                    val startTime = orphan.itemId.substringAfterLast('_').toIntOrNull()

                    if (startTime != null) {

                        val result = playlistRepository.fetchVideoAndFindSong(videoId, startTime)
                        val song = result?.second

                        if (song?.id != null) {


                            val repaired = orphan.copy(serverId = song.id)
                            syncRepository.updateServerId(repaired.itemId, TYPE, song.id)


                            logger.logItemAction(
                                LogAction.RECONCILE_SKIP,
                                "Song_${orphan.itemId}",
                                null,
                                song.id,
                                "Repaired orphan song UUID"
                            )
                        } else {
                            logger.warning("  -> FAILED repair for '${orphan.itemId}'. Could not find matching song on server.")
                        }
                    } else {


                        syncRepository.updateServerId(orphan.itemId, TYPE, orphan.itemId)
                        logger.logItemAction(
                            LogAction.RECONCILE_SKIP,
                            "Video_${orphan.itemId}",
                            null,
                            orphan.itemId,
                            "Repaired orphan video ID"
                        )
                    }
                }
            }
            logger.info("Phase 0 complete.")




            logger.info("Phase 1: Pushing local changes to server...")


            val pendingDeletes = syncRepository.getPendingDeleteItems(TYPE)
            for (item in pendingDeletes) {
                if (item.serverId == null) {

                    syncRepository.confirmDeletion(item.itemId, TYPE)
                    continue
                }

                val response = apiService.deleteLike(LikeRequest(song_id = item.serverId))
                if (response.isSuccessful || response.code() == 404) {
                    syncRepository.confirmDeletion(item.itemId, TYPE)
                    logger.logItemAction(
                        LogAction.UPSTREAM_DELETE_SUCCESS,
                        item.itemId,
                        null,
                        item.serverId
                    )
                } else {
                    logger.logItemAction(
                        LogAction.UPSTREAM_DELETE_FAILED,
                        item.itemId,
                        null,
                        item.serverId,
                        "Code: ${response.code()}"
                    )
                }
            }


            val readyToUpload = syncRepository.getDirtyItems(TYPE).filter { it.serverId != null }

            for (item in readyToUpload) {
                val response = apiService.addLike(LikeRequest(song_id = item.serverId!!))
                if (response.isSuccessful) {
                    syncRepository.markAsSynced(item.itemId, TYPE, item.serverId!!)
                    logger.logItemAction(
                        LogAction.UPSTREAM_UPSERT_SUCCESS,
                        item.itemId,
                        null,
                        item.serverId
                    )
                } else {
                    logger.logItemAction(
                        LogAction.UPSTREAM_UPSERT_FAILED,
                        item.itemId,
                        null,
                        item.serverId,
                        "Code: ${response.code()}"
                    )
                }
            }
            logger.info("Phase 1 complete.")




            logger.info("Phase 2: Fetching states and reconciling...")


            val allRemoteLikes = mutableListOf<com.canopus.Vmusic.data.api.LikedSongApiDto>()
            var page = 1
            while (true) {
                val res = apiService.getLikes(page = page, paginated = true)
                if (!res.isSuccessful) throw Exception("Failed to fetch likes page $page")
                val body = res.body() ?: break
                allRemoteLikes.addAll(body.content)
                if (page >= body.page_count) break
                page++
            }


            val localSynced = syncRepository.getSyncedItems(TYPE)

            val remoteIdMap = allRemoteLikes.associateBy { it.id }
            val localServerIdMap = localSynced.associateBy { it.serverId }

            val newFromServer = allRemoteLikes.filter { !localServerIdMap.containsKey(it.id) }
            if (newFromServer.isNotEmpty()) logger.info("  Found ${newFromServer.size} new likes from server.")

            for (remote in newFromServer) {

                val localItemId = "${remote.video_id}_${remote.start}"

                val meta = UnifiedMetadataEntity(
                    id = localItemId, title = remote.name,
                    artistName = remote.original_artist ?: "Unknown",
                    type = "SEGMENT",
                    specificArtUrl = remote.art,
                    uploaderAvatarUrl = remote.channel?.photo,
                    duration = (remote.end - remote.start).toLong(),
                    channelId = remote.channel_id,
                    description = null,
                    startSeconds = remote.start.toLong(),
                    endSeconds = remote.end.toLong(),
                    parentVideoId = remote.video_id,
                    lastUpdatedAt = System.currentTimeMillis()
                )



                syncRepository.insertRemoteItem(localItemId, TYPE, remote.id, meta)
                logger.logItemAction(
                    LogAction.DOWNSTREAM_INSERT_LOCAL,
                    remote.name,
                    null,
                    remote.id
                )
            }


            val deletedOnServer =
                localSynced.filter { it.serverId != null && !remoteIdMap.containsKey(it.serverId) }

            if (deletedOnServer.isNotEmpty()) logger.info("  Found ${deletedOnServer.size} likes removed on server.")

            for (local in deletedOnServer) {
                syncRepository.removeRemoteItem(local.itemId, TYPE)
                logger.logItemAction(
                    LogAction.DOWNSTREAM_DELETE_LOCAL,
                    local.itemId,
                    null,
                    local.serverId
                )
            }


            val pendingToCheck = syncRepository.getPendingDeleteItems(TYPE)
            val now = System.currentTimeMillis()

            for (pending in pendingToCheck) {
                if (pending.serverId != null && remoteIdMap.containsKey(pending.serverId)) {

                    if (now - pending.timestamp > PENDING_DELETE_TIMEOUT_MS) {

                        syncRepository.markAsSynced(pending.itemId, TYPE, pending.serverId!!)
                        logger.logItemAction(
                            LogAction.RECONCILE_SKIP,
                            pending.itemId,
                            null,
                            pending.serverId,
                            "Pending delete timed out. Reverted to SYNCED."
                        )
                    }
                }
            }

            logger.endSection(name, true)
            return true
        } catch (e: Exception) {
            logger.error(e, "Likes Sync Failed")
            logger.endSection(name, false)
            return false
        }
    }
}