package com.fourbeat.presentation.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fourbeat.presentation.theme.Black
import com.fourbeat.presentation.theme.Gray200
import com.fourbeat.presentation.theme.PrimaryColor
import com.fourbeat.presentation.theme.White
import com.fourbeat.presentation.theme.bold18
import com.fourbeat.presentation.theme.corderRadius
import com.fourbeat.presentation.ui.util.noRippleClickable

@Composable
fun GroupMemberChip(
    modifier: Modifier = Modifier,
    value: Int,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
    val backgroundColor = if (isSelected) PrimaryColor else White
    val borderColor = if (isSelected) PrimaryColor else Gray200
    val contentColor = if (isSelected) White else Black
    val shape = RoundedCornerShape(size = corderRadius)

    Box(
        modifier = modifier
            .aspectRatio(ratio = 1f)
            .noRippleClickable(onClick = onSelected)
            .background(color = backgroundColor, shape = shape)
            .border(border = BorderStroke(width = 1.dp, color = borderColor), shape = shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = value.toString(),
            color = contentColor,
            style = bold18,
        )
    }
}
