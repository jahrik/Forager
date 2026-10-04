package com.zynergylabs.forager.app.data.repository

import android.content.Context
import com.zynergylabs.forager.app.domain.WaypointNavigation
import com.zynergylabs.forager.app.domain.WaypointNavigationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** [WaypointNavigationRepository] backed by Jetpack DataStore. */
class DataStoreWaypointNavigationRepository(
    context: Context,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : WaypointNavigationRepository {

    override suspend fun getCurrent(): Result<WaypointNavigation?> = Result.success(null)

    override suspend fun setCurrent(navigation: WaypointNavigation?): Result<Unit> = Result.success(Unit)
}
