package com.fourbeat.presentation.ui.main

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import com.fourbeat.presentation.navigation.MainScreen
import com.fourbeat.presentation.navigation.ScreenGraph
import com.fourbeat.presentation.ui.FourBeatAppState
import com.fourbeat.presentation.ui.main.creategroup.CreateGroupRoute
import com.fourbeat.presentation.ui.main.groupdetail.GroupDetailRoute
import com.fourbeat.presentation.ui.main.home.HomeRoute
import com.fourbeat.presentation.ui.main.joingroup.JoinGroupRoute
import com.fourbeat.domain.model.post.Song
import com.fourbeat.presentation.ui.main.camera.CameraRoute
import com.fourbeat.presentation.ui.main.createpost.CreatePostRoute
import com.fourbeat.presentation.ui.main.selectsong.SelectSongRoute
import com.fourbeat.presentation.ui.main.sharegroupcode.ShareGroupCodeRoute

const val VIDEO_PATH_KEY = "videoFileUri"

fun NavGraphBuilder.nestedMainGraph(appState: FourBeatAppState) {
    val navController = appState.naveController

    navigation<ScreenGraph.Main>(startDestination = MainScreen.Home) {
        composable<MainScreen.Home> {
            HomeRoute(
                navigateToCreateGroup = navController::navigateToCreateGroup,
                navigateToJoinGroupDialog = navController::navigateToJoinGroupDialog,
                navigateToGroupDetail = navController::navigateToGroupDetail,
            )
        }
        composable<MainScreen.CreateGroup> {
            CreateGroupRoute(
                navigateToGroupDetail = navController::navigateToGroupDetail,
                navigateToBack = navController::popBackStack,
            )
        }
        dialog<MainScreen.JoinGroupDialog> {
            JoinGroupRoute(
                navigateToGroupDetail = navController::navigateToGroupDetail,
            )
        }
        composable<MainScreen.GroupDetail> {
            GroupDetailRoute(
                navigateToSelectSong = navController::navigateToSelectSong,
                showGroupCodeDialog = navController::navigateToShareGroupCodeDialog,
            )
        }
        dialog<MainScreen.ShareGroupCodeDialog> {
            ShareGroupCodeRoute()
        }
        composable<MainScreen.SelectSong> {
            SelectSongRoute(
                navigateToCreatePost = { groupId, song -> navController.navigateToCreatePost(groupId, song) },
                navigateToBack = navController::popBackStack,
            )
        }
        composable<MainScreen.CreatePost> {
            val backStackEntry by navController.currentBackStackEntryAsState()

            CreatePostRoute(
                backStackEntry = backStackEntry,
                navigateToGroupDetail = {
                    navController.popBackStack<MainScreen.GroupDetail>(inclusive = false)
                },
                navigateToCamera = navController::navigateToCamera,
                navigateToBack = navController::popBackStack,
            )
        }
        composable<MainScreen.Camera> {
            CameraRoute(
                navigateBack = { filePath ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(VIDEO_PATH_KEY, filePath)
                    navController.popBackStack()
                },
            )
        }
    }
}

fun NavController.navigateToMainGraph() = navigate(ScreenGraph.Main) {
    popUpTo(id = graph.id) { inclusive = true }
}

fun NavController.navigateToCreateGroup() = navigate(MainScreen.CreateGroup)

fun NavController.navigateToJoinGroupDialog() = navigate(MainScreen.JoinGroupDialog)

fun NavController.navigateToGroupDetail(groupId: Long) = navigate(MainScreen.GroupDetail(groupId)) {
    popUpTo(MainScreen.Home) { inclusive = false }
}

fun NavController.navigateToShareGroupCodeDialog(code: String) =
    navigate(MainScreen.ShareGroupCodeDialog(code))

fun NavController.navigateToSelectSong(groupId: Long) = navigate(MainScreen.SelectSong(groupId))

fun NavController.navigateToCamera() = navigate(MainScreen.Camera)

fun NavController.navigateToCreatePost(groupId: Long, song: Song) = navigate(
    MainScreen.CreatePost(
        groupId = groupId,
        songTitle = song.title,
        songArtist = song.artist,
        songImageUrl = song.albumImageUrl,
    )
)