package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.LatLng
import java.time.LocalDate

/**
 * A 1-degree block, named by its south-west corner in whole degrees: it holds every cell whose centre
 * lies in `[south, south + 1)` by `[west, west + 1)`. The forecast project publishes one file per group
 * per week per block (D55); a cell belongs to the block holding its centre (planner message 2 on
 * dispatch 2026-09-28-03).
 */
data class ForecastBlock(val south: Int, val west: Int) {
    companion object {
        /** Tests-first stub (map layers L0b, B5). */
        fun containing(centre: LatLng): ForecastBlock = ForecastBlock(0, 0)

        /** Tests-first stub (map layers L0b, B5). */
        fun touching(south: Double, west: Double, north: Double, east: Double): Set<ForecastBlock> = emptySet()
    }
}

/** One of a cell's top drivers, as the file carries it (D55: "list of `{label, value}`, top first"). */
data class ForecastDriver(val label: String, val value: String)

/**
 * One 0.1-degree weather cell of one group's forecast for one week, with the properties D55 names.
 * [centre] is the polygon's centre, on multiples of 0.1 degree.
 */
data class ForecastCell(
    val group: String,
    val week: LocalDate,
    val centre: LatLng,
    val chance: Double,
    val uncertaintyLow: Double,
    val uncertaintyHigh: Double,
    val applicable: Boolean,
    val drivers: List<ForecastDriver>,
    val weatherThrough: LocalDate,
    val modelVersion: String,
)

/** A feature a block file carried that did not become a [ForecastCell], and why. */
data class ForecastCellRejection(val featureIndex: Int, val reason: String)

/** A block file's features, parsed: the cells, and every feature that was rejected. */
data class ParsedForecastCells(val cells: List<ForecastCell>, val rejected: List<ForecastCellRejection>)

/** Tests-first stub (map layers L0b, B5). */
fun parseForecastCells(geoJson: String): Result<ParsedForecastCells> = Result.success(ParsedForecastCells(emptyList(), emptyList()))

/** What a [ForecastCellStore] answers for one group, week and set of blocks. */
sealed interface ForecastCellsResult {
    /** The store holds no forecast data for this group and week. An explicit answer, never an empty [Cells]. */
    data object NoForecastData : ForecastCellsResult

    /** The cells the store holds for the requested blocks, and how many features were rejected reading them. */
    data class Cells(val cells: List<ForecastCell>, val rejectedCount: Int) : ForecastCellsResult
}

/** Which groups a [ForecastCellStore] holds data for, for one week. */
sealed interface ForecastAvailability {
    /** No group has data for this week. */
    data object NoForecastData : ForecastAvailability

    data class Groups(val groups: Set<String>) : ForecastAvailability
}

/**
 * The stored-data interface for forecast cells (map layers L0b, B5): what the map reads its colour
 * fields from. Keyed by group, week and 1-degree block (planner message 2). A real, downloaded store
 * will stand behind this later; no downloader exists, and nothing here touches the network.
 */
interface ForecastCellStore {
    suspend fun availability(week: LocalDate): ForecastAvailability

    suspend fun cells(group: String, week: LocalDate, blocks: Set<ForecastBlock>): ForecastCellsResult
}

/** Tests-first stub (map layers L0b, B5). */
object AbsentForecastCellStore : ForecastCellStore {
    override suspend fun availability(week: LocalDate): ForecastAvailability = ForecastAvailability.Groups(emptySet())

    override suspend fun cells(group: String, week: LocalDate, blocks: Set<ForecastBlock>): ForecastCellsResult =
        ForecastCellsResult.Cells(emptyList(), 0)
}

/** Tests-first stub (map layers L0b, B5). */
fun isoWeekStart(date: LocalDate): LocalDate = date
