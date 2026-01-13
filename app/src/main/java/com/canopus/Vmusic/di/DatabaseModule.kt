package com.canopus.Vmusic.di

import android.app.Application
import com.canopus.Vmusic.data.db.AppDatabase
import com.canopus.Vmusic.data.db.ParentVideoMetadataDao
import com.canopus.Vmusic.data.db.PlaylistDao
import com.canopus.Vmusic.data.db.StarredPlaylistDao
import com.canopus.Vmusic.data.db.SyncMetadataDao
import com.canopus.Vmusic.data.db.UnifiedDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase = AppDatabase.getDatabase(app)

    @Provides
    fun provideUnifiedDao(db: AppDatabase): UnifiedDao = db.unifiedDao()

    // Keep the rest of the DAOs that are still in use
    @Provides
    fun providePlaylistDao(db: AppDatabase): PlaylistDao = db.playlistDao()
    @Provides
    fun provideParentVideoMetadataDao(db: AppDatabase): ParentVideoMetadataDao =
        db.parentVideoMetadataDao()

    @Provides
    fun provideSyncMetadataDao(db: AppDatabase): SyncMetadataDao = db.syncMetadataDao()
    @Provides
    fun provideStarredPlaylistDao(db: AppDatabase): StarredPlaylistDao = db.starredPlaylistDao()
}