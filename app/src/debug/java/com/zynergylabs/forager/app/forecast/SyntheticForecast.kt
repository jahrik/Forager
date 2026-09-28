package com.zynergylabs.forager.app.forecast

import android.content.Context
import com.zynergylabs.forager.app.domain.ForecastAvailability
import com.zynergylabs.forager.app.domain.ForecastBlock
import com.zynergylabs.forager.app.domain.ForecastCellStore
import com.zynergylabs.forager.app.domain.ForecastCellsResult
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Tests-first stub (map layers L0b, B6). */
object SyntheticForecastGenerator {
    fun blockGeoJson(group: String, week: LocalDate, block: ForecastBlock): String = """{"type":"FeatureCollection","features":[]}"""
}

/** The Diagnostics screen's "Synthetic forecast layers" switch, as the panel reads and writes it. */
interface SyntheticForecastSwitch {
    suspend fun isEnabled(): Result<Boolean>

    suspend fun setEnabled(enabled: Boolean): Result<Unit>
}

/** Tests-first stub (map layers L0b, B6). */
class SyntheticForecastCellStore(
    context: Context,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : ForecastCellStore, SyntheticForecastSwitch {
    override suspend fun isEnabled(): Result<Boolean> = Result.failure(NotImplementedError("L0b B6"))

    override suspend fun setEnabled(enabled: Boolean): Result<Unit> = Result.failure(NotImplementedError("L0b B6"))

    override suspend fun availability(week: LocalDate): ForecastAvailability = ForecastAvailability.Groups(emptySet())

    override suspend fun cells(group: String, week: LocalDate, blocks: Set<ForecastBlock>): ForecastCellsResult =
        ForecastCellsResult.Cells(emptyList(), 0)
}

/** Tests-first stub (map layers L0b, B6): the debug build's store. */
fun forecastCellStore(context: Context): ForecastCellStore = SyntheticForecastCellStore(context)
