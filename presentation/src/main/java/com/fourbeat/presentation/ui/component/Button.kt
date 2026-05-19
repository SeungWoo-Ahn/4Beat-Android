package com.fourbeat.presentation.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fourbeat.presentation.theme.PrimaryColor
import com.fourbeat.presentation.theme.White
import com.fourbeat.presentation.theme.bold18
import com.fourbeat.presentation.theme.corderRadius

@Composable
fun FourBeatButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    enabled: Boolean,
    text: String,
    containerColor: Color = PrimaryColor,
    contentColor: Color = White,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        enabled = enabled,
        shape = RoundedCornerShape(size = corderRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = if (isLoading) containerColor else containerColor.copy(alpha = 0.1f),
            contentColor = contentColor,
            disabledContentColor = contentColor
        ),
        onClick = onClick
    ) {
        Text(
            text = if (isLoading) "로딩중..." else text,
            style = bold18
        )
    }
}