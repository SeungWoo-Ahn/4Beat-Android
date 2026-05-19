package com.fourbeat.presentation.ui.main.groupdetail.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fourbeat.presentation.theme.Gray500
import com.fourbeat.presentation.theme.contentPadding
import com.fourbeat.presentation.theme.normal14
import com.fourbeat.presentation.theme.medium32
import com.fourbeat.presentation.ui.component.FourBeatSpacer

@Composable
fun GroupDetailHeader(
    modifier: Modifier = Modifier,
    navigateToSelectSong: (Long) -> Unit,
    showGroupCodeDialog: (String) -> Unit,
    viewModel: GroupDetailHeaderViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is GroupDetailHeaderSideEffect.NavigateToSelectSong -> navigateToSelectSong(effect.groupId)
                is GroupDetailHeaderSideEffect.ShowGroupCodeDialog -> showGroupCodeDialog(effect.code)
            }
        }
    }

    val group = viewModel.uiState.group

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = group.capacity,
                style = normal14,
                color = Gray500,
            )
            FourBeatSpacer(size = 8)
            Text(
                text = group.name,
                style = medium32,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = { viewModel.onEvent(GroupDetailHeaderEvent.OnPlusIconClicked) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "ic-plus",
                )
            }
            IconButton(onClick = { viewModel.onEvent(GroupDetailHeaderEvent.OnHashIconClicked) }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "ic-share",
                )
            }
        }
    }
}
