package com.gallr.shared.repository

import com.gallr.shared.data.model.AuthState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<AuthState>
    suspend fun signOut()
    suspend fun deleteAccount()
}
