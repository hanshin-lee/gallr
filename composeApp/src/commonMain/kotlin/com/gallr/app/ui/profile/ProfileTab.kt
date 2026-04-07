package com.gallr.app.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gallr.shared.data.model.AppLanguage
import com.gallr.shared.data.model.AuthState
import com.gallr.shared.repository.AuthRepository
import com.gallr.shared.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient

@Composable
fun ProfileTab(
    authState: AuthState,
    authRepository: AuthRepository,
    profileRepository: ProfileRepository,
    supabaseClient: SupabaseClient,
    lang: AppLanguage,
    modifier: Modifier = Modifier,
) {
    when (authState) {
        is AuthState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
            }
        }
        is AuthState.Anonymous -> {
            SignInScreen(
                supabaseClient = supabaseClient,
                lang = lang,
                modifier = modifier,
            )
        }
        is AuthState.Authenticated -> {
            ProfileScreen(
                user = authState.user,
                authRepository = authRepository,
                profileRepository = profileRepository,
                supabaseClient = supabaseClient,
                lang = lang,
                modifier = modifier,
            )
        }
    }
}
