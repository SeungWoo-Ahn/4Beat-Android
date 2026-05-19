package com.fourbeat.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fourbeat.presentation.theme.Black
import com.fourbeat.presentation.theme.White
import com.fourbeat.presentation.theme.corderRadius
import com.fourbeat.presentation.theme.normal14
import com.fourbeat.presentation.ui.util.noRippleClickable

@Composable
fun ErrorComponent(
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .background(
                    color = Black,
                    shape = RoundedCornerShape(size = corderRadius)
                )
                .padding(all = 16.dp)
                .noRippleClickable(onClick = onRefresh),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "다시 시도하기",
                color = White,
                style = normal14,
            )
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "ic-refresh",
                tint = White
            )
        }
    }
}
