package com.fourbeat.presentation.ui.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.fourbeat.presentation.theme.PrimaryColor
import com.fourbeat.presentation.theme.logoStyle

@Composable
fun Logo(fontSize: Int) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = PrimaryColor)) { append("4") }
            append("Beat")
            withStyle(SpanStyle(color = PrimaryColor)) { append(".") }
        },
        style = logoStyle.copy(fontSize = fontSize.sp),
    )
}
