package com.gallr.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gallr.shared.data.model.AppLanguage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    supabaseClient: SupabaseClient,
    lang: AppLanguage,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "gallr",
            style = MaterialTheme.typography.displayLarge,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = when (lang) {
                AppLanguage.KO -> "취향으로 발견하는 전시"
                AppLanguage.EN -> "discover exhibitions through taste"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(64.dp))

        // Google Sign-In (opens browser OAuth on both platforms)
        OutlinedButton(
            onClick = {
                scope.launch {
                    try {
                        supabaseClient.auth.signInWith(Google) {
                            // Deeplink callback URL for OAuth redirect
                            // supabase-kt handles the redirect parsing automatically
                        }
                    } catch (e: Exception) {
                        // TODO: show error via snackbar
                        println("ERROR [SignIn] Google: ${e.message}")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RectangleShape,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
            ),
        ) {
            Text(
                text = when (lang) {
                    AppLanguage.KO -> "Google로 계속하기"
                    AppLanguage.EN -> "Continue with Google"
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(12.dp))

        // Apple Sign-In
        // On iOS this triggers native Apple Sign-In sheet.
        // On Android this button is hidden or opens browser OAuth.
        OutlinedButton(
            onClick = {
                scope.launch {
                    try {
                        supabaseClient.auth.signInWith(io.github.jan.supabase.auth.providers.Apple)
                    } catch (e: Exception) {
                        println("ERROR [SignIn] Apple: ${e.message}")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RectangleShape,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
            ),
        ) {
            Text(
                text = when (lang) {
                    AppLanguage.KO -> "Apple로 계속하기"
                    AppLanguage.EN -> "Continue with Apple"
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(24.dp))

        // Skip link
        TextButton(onClick = { /* Already anonymous, user can switch tabs */ }) {
            Text(
                text = when (lang) {
                    AppLanguage.KO -> "건너뛰기"
                    AppLanguage.EN -> "Skip for now"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
