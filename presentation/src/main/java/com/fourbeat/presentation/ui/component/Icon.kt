package com.fourbeat.presentation.ui.component

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.fourbeat.presentation.R

@Composable
fun BackIcon(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_back),
        contentDescription = "ic_back",
        modifier = modifier,
    )
}

@Composable
fun AtIcon(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_at),
        contentDescription = "ic_at",
        modifier = modifier,
    )
}

@Composable
fun MultiplyIcon(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_multiply),
        contentDescription = "ic_multiply",
        modifier = modifier,
    )
}

@Composable
fun PlusCircleIcon(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_plus_circle),
        contentDescription = "ic_plus_circle",
        modifier = modifier,
    )
}

@Composable
fun ShareIcon(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_share),
        contentDescription = "ic_share",
        modifier = modifier,
    )
}

@Composable
fun UploadIcon(modifier: Modifier = Modifier) {
    androidx.compose.material3.Icon(
        painter = painterResource(R.drawable.ic_upload),
        contentDescription = "ic_upload",
        modifier = modifier,
    )
}
