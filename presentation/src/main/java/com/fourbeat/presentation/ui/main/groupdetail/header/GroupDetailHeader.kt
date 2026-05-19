package com.fourbeat.presentation.ui.main.groupdetail.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fourbeat.presentation.theme.Gray500
import com.fourbeat.presentation.theme.PrimaryColor
import com.fourbeat.presentation.theme.contentPadding
import com.fourbeat.presentation.theme.medium32
import com.fourbeat.presentation.theme.medium40
import com.fourbeat.presentation.theme.normal14
import com.fourbeat.presentation.ui.component.ShareIcon
import com.fourbeat.presentation.ui.component.UploadIcon

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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = group.name,
                style = medium32,
            )
            Text(
                text = ".",
                color = PrimaryColor,
                style = medium40,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = group.capacity,
                style = normal14,
                color = Gray500,
            )
            IconButton(onClick = { viewModel.onEvent(GroupDetailHeaderEvent.OnPlusIconClicked) }) {
                UploadIcon()
            }
            IconButton(onClick = { viewModel.onEvent(GroupDetailHeaderEvent.OnHashIconClicked) }) {
                ShareIcon()
            }
        }
    }
}
