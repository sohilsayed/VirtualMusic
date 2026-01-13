package com.canopus.Vmusic.di

import com.canopus.Vmusic.background.FavoriteChannelSynchronizer
import com.canopus.Vmusic.background.ISynchronizer
import com.canopus.Vmusic.background.LikesSynchronizer
import com.canopus.Vmusic.background.PlaylistSynchronizer
import com.canopus.Vmusic.background.StarredPlaylistSynchronizer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @IntoSet
    abstract fun bindLikesSynchronizer(impl: LikesSynchronizer): ISynchronizer

    @Binds
    @IntoSet
    abstract fun bindPlaylistSynchronizer(impl: PlaylistSynchronizer): ISynchronizer

    @Binds
    @IntoSet
    abstract fun bindFavoriteChannelSynchronizer(impl: FavoriteChannelSynchronizer): ISynchronizer

    @Binds
    @IntoSet
    abstract fun bindStarredPlaylistSynchronizer(impl: StarredPlaylistSynchronizer): ISynchronizer


}