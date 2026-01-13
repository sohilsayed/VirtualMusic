package com.canopus.Vmusic.ui.navigation

import com.canopus.Vmusic.viewmodel.FullListViewModel
import com.canopus.Vmusic.viewmodel.MusicCategoryType // FIX: Correct import
import com.canopus.Vmusic.viewmodel.PlaylistDetailsViewModel
import com.canopus.Vmusic.viewmodel.VideoDetailsViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object AppDestinations {
    const val HOME_ROUTE = "home"
    const val DISCOVERY_ROUTE = "discover"
    const val LIBRARY_ROUTE = "library"
    const val DOWNLOADS_ROUTE = "downloads"
    const val SETTINGS_ROUTE = "settings"
    const val LOGIN_ROUTE = "login"
    const val FOR_YOU_ROUTE = "for_you"

    const val VIDEO_DETAILS_ROUTE_TEMPLATE = "video_details/{${VideoDetailsViewModel.VIDEO_ID_ARG}}"
    fun videoDetailRoute(videoId: String) = "video_details/$videoId"

    const val FULL_LIST_VIEW_ROUTE_TEMPLATE =
        "full_list/{${FullListViewModel.CATEGORY_TYPE_ARG}}/{${FullListViewModel.ORG_ARG}}"

    // FIX: Use MusicCategoryType directly
    fun fullListViewRoute(category: MusicCategoryType, org: String): String {
        val encodedOrg = URLEncoder.encode(org, StandardCharsets.UTF_8.toString())
        return "full_list/${category.name}/$encodedOrg"
    }

    const val PLAYLIST_DETAILS_ROUTE_TEMPLATE =
        "playlist_details/{${PlaylistDetailsViewModel.PLAYLIST_ID_ARG}}"

    fun playlistDetailsRoute(playlistId: String): String {
        val encodedId = URLEncoder.encode(playlistId, StandardCharsets.UTF_8.toString())
        return "playlist_details/$encodedId"
    }
}