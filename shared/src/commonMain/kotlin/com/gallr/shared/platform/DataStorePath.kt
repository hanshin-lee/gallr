package com.gallr.shared.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

internal const val DATASTORE_FILE_NAME = "gallr_bookmarks.preferences_pb"

expect fun createDataStore(): DataStore<Preferences>
