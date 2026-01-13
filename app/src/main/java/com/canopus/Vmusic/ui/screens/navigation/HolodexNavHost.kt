package com.canopus.Vmusic.ui.navigation

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.canopus.Vmusic.auth.LoginScreen
import com.canopus.Vmusic.domain.action.GlobalMediaActionHandler
import com.canopus.Vmusic.ui.screens.ChannelDetailsScreen
import com.canopus.Vmusic.ui.screens.DiscoveryScreen
import com.canopus.Vmusic.ui.screens.ForYouScreen
import com.canopus.Vmusic.ui.screens.FullListViewScreen
import com.canopus.Vmusic.ui.screens.HomeScreen
import com.canopus.Vmusic.ui.screens.LibraryScreen
import com.canopus.Vmusic.ui.screens.PlaylistDetailsScreen
import com.canopus.Vmusic.ui.screens.SettingsScreen
import com.canopus.Vmusic.ui.screens.StandardMediaListScreen
import com.canopus.Vmusic.ui.screens.VideoDetailsScreen
import com.canopus.Vmusic.viewmodel.ChannelDetailsViewModel
import com.canopus.Vmusic.viewmodel.FullListViewModel
import com.canopus.Vmusic.viewmodel.LibraryType
import com.canopus.Vmusic.viewmodel.MusicCategoryType
import com.canopus.Vmusic.viewmodel.PlaylistDetailsViewModel
import com.canopus.Vmusic.viewmodel.PlaylistManagementViewModel
import com.canopus.Vmusic.viewmodel.SettingsViewModel
import com.canopus.Vmusic.viewmodel.VideoDetailsViewModel
import com.canopus.Vmusic.viewmodel.VideoListViewModel
import org.orbitmvi.orbit.compose.collectAsState

@SuppressLint("UnstableApi")
@Composable
fun HolodexNavHost(
    navController: NavHostController,
    videoListViewModel: VideoListViewModel,
    playlistManagementViewModel: PlaylistManagementViewModel,
    activity: ComponentActivity,
    contentPadding: PaddingValues,
    actionHandler: GlobalMediaActionHandler,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.LIBRARY_ROUTE,
        modifier = modifier.fillMaxSize()
    ) {
        composable(AppDestinations.DISCOVERY_ROUTE) {
            DiscoveryScreen(
                navController = navController,
                contentPadding = contentPadding
            )
        }

        composable(AppDestinations.FOR_YOU_ROUTE) {
            ForYouScreen(navController = navController)
        }

        composable(AppDestinations.HOME_ROUTE) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val state by settingsViewModel.collectAsState()

            // Simplified check: if key is missing, show placeholder, else show Home
            if (state.currentApiKey.isBlank()) {
                // You can create a simple composable for this or navigate to settings
                SettingsScreen(
                    navController = navController,
                    onNavigateUp = { /* No up navigation */ },
                    onApiKeySavedRestartNeeded = { /* handled in VM */ }
                )
            } else {
                HomeScreen(
                    navController = navController,
                    videoListViewModel = videoListViewModel,
                    contentPadding = contentPadding,
                    actionHandler = actionHandler
                )
            }
        }

        composable(AppDestinations.LIBRARY_ROUTE) {
            LibraryScreen(
                navController = navController,
                playlistManagementViewModel = playlistManagementViewModel,
                contentPadding = contentPadding,
                actionHandler = actionHandler
            )
        }

        composable(AppDestinations.DOWNLOADS_ROUTE) {
            StandardMediaListScreen(
                libraryType = LibraryType.DOWNLOADS,
                actions = actionHandler,
                contentPadding = contentPadding
            )
        }

        composable(AppDestinations.SETTINGS_ROUTE) {
            val vListVm: VideoListViewModel = hiltViewModel(activity)
            SettingsScreen(
                navController = navController,
                onNavigateUp = { navController.popBackStack() },
                onApiKeySavedRestartNeeded = { vListVm.refreshCurrentListViaPull() }
            )
        }
        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(onLoginSuccess = { navController.popBackStack() })
        }

        composable(
            route = "channel_details/{${ChannelDetailsViewModel.CHANNEL_ID_ARG}}",
            arguments = listOf(navArgument(ChannelDetailsViewModel.CHANNEL_ID_ARG) {
                type = NavType.StringType
            })
        ) {
            ChannelDetailsScreen(
                navController = navController,
                onNavigateUp = { navController.popBackStack() },
                actionHandler = actionHandler // <--- PASS IT HERE
            )
        }

        composable(
            route = AppDestinations.FULL_LIST_VIEW_ROUTE_TEMPLATE,
            arguments = listOf(
                navArgument(FullListViewModel.CATEGORY_TYPE_ARG) { type = NavType.StringType },
                navArgument(FullListViewModel.ORG_ARG) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryName =
                backStackEntry.arguments?.getString(FullListViewModel.CATEGORY_TYPE_ARG)
                    ?: MusicCategoryType.TRENDING.name
            val category = try {
                MusicCategoryType.valueOf(categoryName)
            } catch (e: Exception) {
                MusicCategoryType.TRENDING
            }
            FullListViewScreen(navController = navController, categoryType = category)
        }

        composable(
            AppDestinations.PLAYLIST_DETAILS_ROUTE_TEMPLATE,
            arguments = listOf(navArgument(PlaylistDetailsViewModel.PLAYLIST_ID_ARG) {
                type = NavType.StringType
            })
        ) {
            PlaylistDetailsScreen(
                navController = navController,
                onNavigateUp = { navController.popBackStack() },
                playlistManagementViewModel = playlistManagementViewModel,
                contentPadding = contentPadding,
                actionHandler = actionHandler
            )
        }

        composable(
            AppDestinations.VIDEO_DETAILS_ROUTE_TEMPLATE,
            arguments = listOf(navArgument(VideoDetailsViewModel.VIDEO_ID_ARG) {
                type = NavType.StringType
            })
        ) {
            VideoDetailsScreen(
                navController = navController,
                onNavigateUp = { navController.popBackStack() },
                actionHandler = actionHandler,
                contentPadding = contentPadding
            )
        }
    }
}