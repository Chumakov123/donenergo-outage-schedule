package com.chumakov123.outageschedule.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "app_settings")

object AppSettingsKeys {
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val SELECTED_BRANCH_URLS = stringSetPreferencesKey("selected_branch_urls")
    val FILTER_ONLY_TRACKED_PLACES = booleanPreferencesKey("filter_only_tracked_places")
    val OUTAGE_SYNC_INTERVAL_HOURS = intPreferencesKey("outage_sync_interval_hours")
    val NOTIFICATION_LEAD_HOURS = stringSetPreferencesKey("notification_lead_hours")
}