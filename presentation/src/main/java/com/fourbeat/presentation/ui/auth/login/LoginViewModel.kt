package com.fourbeat.presentation.ui.auth.login

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fourbeat.domain.usecase.auth.LoginUseCase
import com.fourbeat.presentation.model.auth.KakaoClient
import com.fourbeat.presentation.model.auth.OAuthUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    private val _sideEffect = Channel<LoginSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnLoginButtonClicked -> getKakaoEmail(event.context)
        }
    }

    /**
     * kakao에서 email 가져오기
     * 성공하면, 이어서 login 요청
     * 실패하면, 현재는 로깅
     * */
    private fun getKakaoEmail(context: Context) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            runCatching { KakaoClient.loginWithTalk(context) }
                .onSuccess { oAuthUser ->
                    login(oAuthUser)
                }
                .onFailure {
                }
            uiState = uiState.copy(isLoading = false)
        }
    }

    /**
     * 로그인 요청
     * 성공하면, 홈 화면으로 이동
     * 실패하면, 404시엔 회원가입 화면으로 이동 (TODO)
     * */
    private suspend fun login(oAuthUser: OAuthUser) {
        loginUseCase(oAuthUser.email)
            .onSuccess {
                _sideEffect.send(LoginSideEffect.NavigateToHome)
            }
            .onFailure {
                _sideEffect.send(LoginSideEffect.NavigateToRegister(oAuthUser))
            }
    }
}
