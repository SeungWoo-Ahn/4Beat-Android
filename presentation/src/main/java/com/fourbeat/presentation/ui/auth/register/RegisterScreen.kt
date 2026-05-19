package com.fourbeat.presentation.ui.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fourbeat.presentation.theme.Gray500
import com.fourbeat.presentation.theme.PrimaryColor
import com.fourbeat.presentation.theme.White
import com.fourbeat.presentation.theme.contentPadding
import com.fourbeat.presentation.theme.normal16
import com.fourbeat.presentation.theme.medium32
import com.fourbeat.presentation.ui.component.FourBeatButton
import com.fourbeat.presentation.ui.component.FourBeatSpacer
import com.fourbeat.presentation.ui.component.FourBeatTextField
import com.fourbeat.presentation.ui.component.TopbarForBack

@Composable
fun RegisterRoute(
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is RegisterSideEffect.NavigateToHome -> navigateToHome()
                RegisterSideEffect.NavigateToBack -> onBack()
            }
        }
    }

    RegisterScreen(
        modifier = modifier,
        uiState = viewModel.uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun RegisterScreen(
    modifier: Modifier,
    uiState: RegisterUiState,
    onEvent: (RegisterEvent) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = White)
    ) {
        TopbarForBack(
            onBack = { onEvent(RegisterEvent.OnBackClicked) }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = contentPadding)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("처음이지")
                        withStyle(SpanStyle(color = PrimaryColor)) { append("?") }
                    },
                    style = medium32,
                )
                Text(
                    text = "어떻게 부르면 좋을지 알려줘.",
                    color = Gray500,
                    style = normal16
                )
            }
            FourBeatSpacer(size = 64)
            Column(
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                FourBeatTextField(
                    value = uiState.name,
                    label = "이름",
                    placeholder = "이름을 알려주세요",
                    maxLength = RegisterUiState.NAME_MAX_LENGTH,
                    onValueChange = { value -> onEvent(RegisterEvent.OnNameChanged(value)) }
                )
                FourBeatTextField(
                    value = uiState.nickname,
                    label = "닉네임",
                    placeholder = "닉네임을 알려주세요",
                    maxLength = RegisterUiState.NICKNAME_MAX_LENGTH,
                    imeAction = ImeAction.Done,
                    onValueChange = { value -> onEvent(RegisterEvent.OnNicknameChanged(value)) }
                )
            }
            FourBeatSpacer(modifier = Modifier.weight(1f))
            FourBeatButton(
                isLoading = uiState.isLoading,
                enabled = uiState.isValid,
                text = "가입하기",
                onClick = { onEvent(RegisterEvent.OnRegisterButtonClicked) }
            )
        }

    }
}
