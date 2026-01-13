package com.canopus.Vmusic.background

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.canopus.Vmusic.data.db.UnifiedDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class CachePruningWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val unifiedDao: UnifiedDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val now = System.currentTimeMillis()

            val threshold = now - TimeUnit.HOURS.toMillis(24)

            Timber.i("CachePruningWorker: Pruning metadata older than $threshold")


            unifiedDao.pruneOrphanedMetadata(threshold)

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Cache pruning failed")
            Result.retry()
        }
    }
}