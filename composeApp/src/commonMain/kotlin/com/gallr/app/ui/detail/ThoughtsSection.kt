package com.gallr.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.gallr.app.ui.theme.GallrAccent
import com.gallr.app.ui.theme.GallrSpacing
import com.gallr.shared.data.model.AppLanguage
import com.gallr.shared.data.model.AuthState
import com.gallr.shared.data.model.Thought
import com.gallr.shared.repository.ThoughtRepository

@Composable
fun ThoughtsSection(
    exhibitionId: String,
    thoughtRepository: ThoughtRepository,
    authState: AuthState,
    lang: AppLanguage,
    onSignInNeeded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentUserId = (authState as? AuthState.Authenticated)?.user?.id
    var thoughts by remember { mutableStateOf<List<Thought>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showComposer by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val hasUserThought = currentUserId != null && thoughts.any { it.userId == currentUserId }

    LaunchedEffect(exhibitionId, refreshTrigger) {
        isLoading = true
        try {
            thoughts = thoughtRepository.getThoughtsForExhibition(exhibitionId)
        } catch (_: Exception) {
            thoughts = emptyList()
        }
        isLoading = false
        showComposer = false
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when (lang) {
                    AppLanguage.KO -> "감상평"
                    AppLanguage.EN -> "THOUGHTS"
                },
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.weight(1f))
            if (thoughts.isNotEmpty()) {
                Text(
                    text = "${thoughts.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(GallrSpacing.md))

        if (isLoading) {
            Text(
                text = "...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (thoughts.isEmpty() && !showComposer) {
            // Empty state
            Text(
                text = when (lang) {
                    AppLanguage.KO -> "아직 감상평이 없어요. 첫 번째로 나눠보세요."
                    AppLanguage.EN -> "No thoughts yet. Be the first to share."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            // Thought list
            val scope = rememberCoroutineScope()
            thoughts.forEach { thought ->
                ThoughtCard(
                    thought = thought,
                    lang = lang,
                    isOwn = thought.userId == currentUserId,
                    onDelete = {
                        scope.launch {
                            try {
                                thoughtRepository.deleteThought(thought.id)
                                refreshTrigger++
                            } catch (_: Exception) {}
                        }
                    },
                )
            }
        }

        Spacer(Modifier.height(GallrSpacing.md))

        // Composer or CTA (hide if user already posted)
        if (showComposer) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ThoughtComposer(
                exhibitionId = exhibitionId,
                thoughtRepository = thoughtRepository,
                lang = lang,
                onDismiss = { showComposer = false },
                onSubmitted = {
                    showComposer = false
                    refreshTrigger++
                },
            )
        } else if (!hasUserThought) {
            OutlinedButton(
                onClick = {
                    if (authState is AuthState.Authenticated) {
                        showComposer = true
                    } else {
                        onSignInNeeded()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RectangleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background,
                ),
            ) {
                Text(
                    text = when (lang) {
                        AppLanguage.KO -> "✍️ 감상평 남기기"
                        AppLanguage.EN -> "✍\uFE0F Share your thoughts"
                    },
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
fun ThoughtCard(
    thought: Thought,
    lang: AppLanguage,
    isOwn: Boolean = false,
    onDelete: (() -> Unit)? = null,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = GallrSpacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar
            val initial = thought.authorDisplayName.firstOrNull()?.uppercase() ?: "?"
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(GallrSpacing.sm))
            Column {
                Text(
                    text = thought.authorDisplayName.ifEmpty {
                        when (lang) {
                            AppLanguage.KO -> "익명"
                            AppLanguage.EN -> "Anonymous"
                        }
                    },
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = thought.createdAt.take(10), // YYYY-MM-DD
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(GallrSpacing.xs))
        Text(
            text = thought.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (isOwn && onDelete != null) {
            Spacer(Modifier.height(GallrSpacing.xs))
            if (showDeleteConfirm) {
                Row {
                    Text(
                        text = when (lang) {
                            AppLanguage.KO -> "삭제하시겠습니까?"
                            AppLanguage.EN -> "Delete?"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(GallrSpacing.sm))
                    Text(
                        text = when (lang) {
                            AppLanguage.KO -> "삭제"
                            AppLanguage.EN -> "Delete"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable { onDelete(); showDeleteConfirm = false },
                    )
                    Spacer(Modifier.width(GallrSpacing.sm))
                    Text(
                        text = when (lang) {
                            AppLanguage.KO -> "취소"
                            AppLanguage.EN -> "Cancel"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { showDeleteConfirm = false },
                    )
                }
            } else {
                Text(
                    text = when (lang) {
                        AppLanguage.KO -> "삭제"
                        AppLanguage.EN -> "Delete"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { showDeleteConfirm = true },
                )
            }
        }
        Spacer(Modifier.height(GallrSpacing.sm))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
