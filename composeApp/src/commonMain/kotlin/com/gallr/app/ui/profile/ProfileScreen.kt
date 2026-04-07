package com.gallr.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.auth.auth
import com.gallr.shared.data.model.AppLanguage
import com.gallr.shared.data.model.GallrUser
import com.gallr.shared.data.model.Profile
import com.gallr.shared.repository.AuthRepository
import com.gallr.shared.repository.ProfileRepository
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    user: GallrUser,
    authRepository: AuthRepository,
    profileRepository: ProfileRepository,
    supabaseClient: io.github.jan.supabase.SupabaseClient,
    lang: AppLanguage,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf<Profile?>(null) }

    // Fetch profile from DB (more reliable than JWT metadata on iOS)
    androidx.compose.runtime.LaunchedEffect(user.id) {
        val userId = user.id.takeIf { it.isNotBlank() }
            ?: try { supabaseClient.auth.retrieveUserForCurrentSession()?.id } catch (_: Exception) { null }
        if (userId != null) {
            try { profile = profileRepository.getProfile(userId) } catch (_: Exception) {}
        }
    }

    val displayName = profile?.displayName?.takeIf { it.isNotBlank() }
        ?: user.displayName.takeIf { it.isNotBlank() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))

        // Avatar
        val initial = (displayName ?: "?").first().uppercase()
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = displayName ?: when (lang) {
                AppLanguage.KO -> "이름 없음"
                AppLanguage.EN -> "No name"
            },
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(Modifier.height(32.dp))

        // Empty diary state
        Text(
            text = when (lang) {
                AppLanguage.KO -> "전시 일기가 비어있어요.\n북마크를 추가해서 기록을 시작해보세요."
                AppLanguage.EN -> "Your exhibition diary is empty.\nBookmark exhibitions to start logging."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(48.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))

        // Settings section
        Text(
            text = when (lang) {
                AppLanguage.KO -> "설정"
                AppLanguage.EN -> "SETTINGS"
            },
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))

        // Logout
        OutlinedButton(
            onClick = { scope.launch { authRepository.signOut() } },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RectangleShape,
        ) {
            Text(
                text = when (lang) {
                    AppLanguage.KO -> "로그아웃"
                    AppLanguage.EN -> "Sign Out"
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(12.dp))

        // Delete Account
        if (showDeleteConfirm) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
            ) {
                Text(
                    text = when (lang) {
                        AppLanguage.KO -> "계정을 삭제하시겠습니까? 모든 북마크와 감상이 영구적으로 삭제됩니다."
                        AppLanguage.EN -> "Delete your account? All bookmarks and thoughts will be permanently deleted."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = false },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RectangleShape,
                    ) {
                        Text(
                            when (lang) {
                                AppLanguage.KO -> "취소"
                                AppLanguage.EN -> "Cancel"
                            },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                authRepository.deleteAccount()
                                showDeleteConfirm = false
                            }
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RectangleShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        Text(
                            when (lang) {
                                AppLanguage.KO -> "삭제"
                                AppLanguage.EN -> "Delete"
                            },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        } else {
            TextButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = when (lang) {
                        AppLanguage.KO -> "계정 삭제"
                        AppLanguage.EN -> "Delete Account"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
