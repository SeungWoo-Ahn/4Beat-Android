package com.fourbeat.presentation.ui.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fourbeat.presentation.model.auth.OAuthUser
import com.fourbeat.presentation.theme.Black
import com.fourbeat.presentation.theme.Gray500
import com.fourbeat.presentation.theme.PrimaryColor
import com.fourbeat.presentation.theme.White
import com.fourbeat.presentation.theme.contentPadding
import com.fourbeat.presentation.theme.normal16
import com.fourbeat.presentation.theme.medium56
import com.fourbeat.presentation.ui.component.FourBeatButton
import com.fourbeat.presentation.ui.component.FourBeatSpacer
import com.fourbeat.presentation.ui.component.Logo

@Composable
fun LoginRoute(
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit,
    navigateToRegister: (OAuthUser) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                LoginSideEffect.NavigateToHome -> navigateToHome()
                is LoginSideEffect.NavigateToRegister -> navigateToRegister(effect.oAuthUser)
            }
        }
    }

    LoginScreen(
        modifier = modifier,
        uiState = viewModel.uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun LoginScreen(
    modifier: Modifier,
    uiState: LoginUiState,
    onEvent: (LoginEvent) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = White)
            .padding(all = contentPadding),
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Logo(fontSize = 32)
        }
        FourBeatSpacer(modifier = Modifier.weight(1f))
        Text(
            text = buildAnnotatedString {
                append("오늘\n뭐 들었어")
                withStyle(SpanStyle(color = PrimaryColor)) { append("?") }
            },
            style = medium56
        )
        FourBeatSpacer(size = 16)
        Text(
            text = "한 곡, 4초 영상. 매일 한 박자씩 쌓아봐.",
            color = Gray500,
            style = normal16
        )
        FourBeatSpacer(size = 80)
        FourBeatButton(
            isLoading = uiState.isLoading,
            enabled = uiState.isLoading.not(),
            text = "카카오로 계속하기",
            containerColor = Color(0xFFF1E105),
            contentColor = Black,
            onClick = { onEvent(LoginEvent.OnLoginButtonClicked(context)) }
        )
    }
}