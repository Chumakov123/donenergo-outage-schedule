package com.chumakov123.outageschedule.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.chumakov123.outageschedule.data.settings.AppSettingsKeys
import com.chumakov123.outageschedule.data.settings.dataStore
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreAppSettingsRepository(
    private val context: Context
) : AppSettingsRepository {

    override val isOnboardingCompletedFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[AppSettingsKeys.ONBOARDING_COMPLETED] ?: false
        }

    override val selectedBranchUrlsFlow: Flow<Set<String>> =
        context.dataStore.data.map { prefs ->
            prefs[AppSettingsKeys.SELECTED_BRANCH_URLS] ?: emptySet()
        }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AppSettingsKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    override suspend fun setSelectedBranchUrls(urls: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[AppSettingsKeys.SELECTED_BRANCH_URLS] = urls
        }
    }
}