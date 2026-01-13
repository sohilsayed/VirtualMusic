package com.canopus.Vmusic.di

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import com.canopus.Vmusic.auth.AuthRepository
import com.canopus.Vmusic.data.api.HolodexApiService
import com.canopus.Vmusic.data.db.AppDatabase
import com.canopus.Vmusic.data.repository.DownloadRepository
import com.canopus.Vmusic.data.repository.DownloadRepositoryImpl
import com.canopus.Vmusic.data.repository.RoomSearchHistoryRepository
import com.canopus.Vmusic.data.repository.SearchHistoryRepository
import com.canopus.Vmusic.data.repository.UserPreferencesRepository
import com.canopus.Vmusic.data.repository.YouTubeStreamRepository
import com.canopus.Vmusic.data.repository.userPreferencesDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.openid.appauth.AuthorizationService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {


    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(
        impl: RoomSearchHistoryRepository // <--- Using the new Room-based implementation
    ): SearchHistoryRepository

    @Binds
    @Singleton
    @UnstableApi
    abstract fun bindDownloadRepository(
        impl: DownloadRepositoryImpl
    ): DownloadRepository

    companion object {


        // Helper to provide the new DAO
        @Provides
        fun provideSearchHistoryDao(db: AppDatabase) = db.searchHistoryDao()

        // NOTE: providePlaylistRepository was DELETED.
        // PlaylistRepository has an @Inject constructor, so Hilt creates it automatically.

        @Provides
        @Singleton
        fun provideYouTubeStreamRepository(
            sharedPreferences: SharedPreferences,
        ): YouTubeStreamRepository {
            return YouTubeStreamRepository(sharedPreferences)
        }

        @Provides
        @Singleton
        fun provideAuthRepository(
            holodexApiService: HolodexApiService,
            authService: AuthorizationService
        ): AuthRepository {
            return AuthRepository(holodexApiService, authService)
        }

        @Provides
        @Singleton
        fun provideUserPreferencesRepository(@ApplicationContext context: Context): UserPreferencesRepository {
            return UserPreferencesRepository(context.userPreferencesDataStore)
        }

        @OptIn(UnstableApi::class)
        @Provides
        @Singleton
        @UpstreamDataSource
        fun provideUpstreamDataSourceFactory(): DataSource.Factory {
            return DefaultHttpDataSource.Factory()
                .setUserAgent("HolodexAppDownloader/1.0")
                .setConnectTimeoutMs(30000)
                .setReadTimeoutMs(30000)
                .setAllowCrossProtocolRedirects(true)
        }
    }
}