package com.canopus.Vmusic.ui

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.canopus.Vmusic.domain.action.GlobalMediaActionHandler
import com.canopus.Vmusic.ui.composables.FullPlayerActions
import com.canopus.Vmusic.ui.composables.FullPlayerScreenContent
import com.canopus.Vmusic.ui.composables.MainScreenLayout
import com.canopus.Vmusic.ui.composables.MiniPlayerWithProgressBar
import com.canopus.Vmusic.ui.composables.PlaylistManagementDialogs
import com.canopus.Vmusic.ui.navigation.AppDestinations
import com.canopus.Vmusic.ui.navigation.GlobalUiEvent
import com.canopus.Vmusic.ui.navigation.HolodexNavHost
import com.canopus.Vmusic.ui.screens.navigation.BottomNavItem
import com.canopus.Vmusic.ui.theme.HolodexMusicTheme
import com.canopus.Vmusic.viewmodel.FavoritesViewModel
import com.canopus.Vmusic.viewmodel.PlaybackViewModel
import com.canopus.Vmusic.viewmodel.PlaylistManagementViewModel
import com.canopus.Vmusic.viewmodel.SettingsViewModel
import com.canopus.Vmusic.viewmodel.VideoListSideEffect
import com.canopus.Vmusic.viewmodel.VideoListViewModel
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectSideEffect

private const val TAG = "MainScreenScaffold"

@SuppressLint("UnstableApi")
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenScaffold(
    navController: NavHostController,
    activity: ComponentActivity,
    player: ExoPlayer,
    actionHandler: GlobalMediaActionHandler
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // ViewModels
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val playbackViewModel: PlaybackViewModel = hiltViewModel()
    val playlistManagementViewModel: PlaylistManagementViewModel = hiltViewModel(activity)
    val videoListViewModel: VideoListViewModel = hiltViewModel(activity)

    // UI State
    var showFullPlayerSheet by remember { mutableStateOf(false) }
    val fullPlayerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Navigation State
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    LaunchedEffect(actionHandler) {
        actionHandler.uiEvents.collect { event ->
            when (event) {
                is GlobalUiEvent.NavigateToVideo -> {
                    navController.navigate(AppDestinations.videoDetailRoute(event.videoId))
                }

                is GlobalUiEvent.NavigateToChannel -> {
                    navController.navigate("channel_details/${event.channelId}")
                }

                is GlobalUiEvent.ShowPlaylistDialog -> {
                    playlistManagementViewModel.prepareItemForPlaylistAddition(event.item)
                }

                is GlobalUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    videoListViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is VideoListSideEffect.NavigateTo -> {
                when (val destination = sideEffect.destination) {
                    is VideoListViewModel.NavigationDestination.VideoDetails -> {
                        navController.navigate(AppDestinations.videoDetailRoute(destination.videoId))
                    }

                    is VideoListViewModel.NavigationDestination.HomeScreenWithSearch -> {
                        navController.navigate(AppDestinations.HOME_ROUTE) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                        }
                        videoListViewModel.setSearchActive(true)
                    }
                }
            }

            is VideoListSideEffect.ShowToast -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    HolodexMusicTheme(settingsViewModel = settingsViewModel) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainScreenLayout(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    Column {
                        MiniPlayerWithProgressBar(
                            playbackViewModel = playbackViewModel,
                            onTapped = { showFullPlayerSheet = true }
                        )
                        NavigationBar {
                            val navItems = listOf(
                                BottomNavItem.Discover,
                                BottomNavItem.Browse,
                                BottomNavItem.Library,
                                BottomNavItem.Downloads
                            )
                            navItems.forEach { item ->
                                val isSelected = currentRoute == item.route
                                NavigationBarItem(
                                    icon = { Icon(item.icon, contentDescription = null) },
                                    label = { Text(stringResource(item.titleResId)) },
                                    selected = isSelected,
                                    onClick = {
                                        if (item.route == AppDestinations.DISCOVERY_ROUTE &&
                                            navController.graph.startDestinationId == navController.graph.findNode(
                                                AppDestinations.DISCOVERY_ROUTE
                                            )?.id
                                        ) {
                                            navController.navigate(item.route) {
                                                popUpTo(0) { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        } else {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            ) { dynamicPadding ->
                Box(modifier = Modifier.fillMaxSize()) {
                    HolodexNavHost(
                        navController = navController,
                        videoListViewModel = videoListViewModel,
                        playlistManagementViewModel = playlistManagementViewModel,
                        activity = activity,
                        contentPadding = dynamicPadding,
                        actionHandler = actionHandler
                    )
                }
            }
        }
        if (showFullPlayerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFullPlayerSheet = false },
                sheetState = fullPlayerSheetState,
                containerColor = Color.Transparent,
                shape = RoundedCornerShape(0),
                scrimColor = Color.Black.copy(alpha = 0.6f),
                dragHandle = null
            ) {
                FullPlayerScreenDestination(
                    player = player,
                    navController = navController,
                    onNavigateUp = {
                        coroutineScope.launch { fullPlayerSheetState.hide() }.invokeOnCompletion {
                            if (!fullPlayerSheetState.isVisible) showFullPlayerSheet = false
                        }
                    }
                )
            }
        }

        PlaylistManagementDialogs(playlistManagementViewModel)
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun FullPlayerScreenDestination(
    player: Player?,
    navController: NavHostController,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playbackViewModel: PlaybackViewModel = hiltViewModel()
    val favoritesViewModel: FavoritesViewModel = hiltViewModel()
    val playlistManagementViewModel: PlaylistManagementViewModel = hiltViewModel()
    val videoListViewModel: VideoListViewModel = hiltViewModel()
    val context = LocalContext.current

    val playerActions = remember(
        playbackViewModel,
        favoritesViewModel,
        videoListViewModel,
        playlistManagementViewModel
    ) {
        FullPlayerActions(
            onNavigateUp = onNavigateUp,
            onTogglePlayPause = { playbackViewModel.togglePlayPause() },
            onSeekTo = { positionSec -> playbackViewModel.seekTo(positionSec) },
            onSkipToNext = { playbackViewModel.skipToNext() },
            onSkipToPrevious = { playbackViewModel.skipToPrevious() },
            onToggleRepeatMode = { playbackViewModel.toggleRepeatMode() },
            onToggleShuffleMode = { playbackViewModel.toggleShuffleMode() },
            onPlayQueueItemAtIndex = { index -> playbackViewModel.playQueueItemAtIndex(index) },
            onReorderQueueItem = { from, to -> playbackViewModel.reorderQueueItem(from, to) },
            onRemoveQueueItem = { index -> playbackViewModel.removeItemFromQueue(index) },
            onClearQueue = { playbackViewModel.clearCurrentQueue() },
            onToggleLike = { playbackItem -> favoritesViewModel.toggleLike(playbackItem) },
            onFindArtist = { channelId -> videoListViewModel.setBrowseContextAndNavigate(channelId = channelId) },
            onOpenAudioSettings = { audioSessionId ->
                Toast.makeText(context, "Audio FX Session ID: $audioSessionId", Toast.LENGTH_SHORT)
                    .show()
            },
            onAddToPlaylist = { playbackItem ->
                playlistManagementViewModel.prepareItemForPlaylistAdditionFromPlaybackItem(
                    playbackItem
                )
            }
        )
    }

    FullPlayerScreenContent(
        player = player,
        navController = navController,
        actions = playerActions,
        modifier = modifier
    )
}