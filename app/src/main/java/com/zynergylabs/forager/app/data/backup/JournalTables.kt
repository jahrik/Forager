package com.zynergylabs.forager.app.data.backup

/**
 * What the backup knows about the database's tables, because `ForagerDatabase` declares no `@ForeignKey`
 * (CLAUDE.md, Room-for-relations pitfall): the links between records are plain columns, so a restore has to
 * be told which column names which record. **Stated once, here, and guarded by a test that reads the exported
 * schema** so a new table cannot be left out unnoticed.
 *
 * A table is one of three kinds, and the kind is what a Merge does with its rows:
 * - [Kind.RECORD]: a record with its own id (`waypoints`, `tracks`, `offline_regions`, `log_photos`,
 *   `mushroom_log_entries`, `cartography_entries`). Inserted when its id is absent; an id already present is
 *   skipped and the phone's copy wins.
 * - [Kind.OWNED]: rows that belong to one record ([TableSpec.owner]). Inserted only with their owner, and only
 *   when every record they name ([TableSpec.needs]) exists on the phone afterwards; otherwise dropped, so no row
 *   dangles. `track_points` carries an auto-generated id that is a per-phone counter, so it is inserted without it.
 * - links on a RECORD ([TableSpec.softLinks]): a nullable column naming another record. Set to NULL when the
 *   target does not exist after the merge, the rule the app already applies when a target is deleted.
 */
internal object JournalTables {

    enum class Kind { RECORD, OWNED }

    /** [column] on the owning table holds the id of a record in [table] (whose key column is [targetKey]). */
    data class Reference(val column: String, val table: String, val targetKey: String = "id")

    data class TableSpec(
        val name: String,
        val kind: Kind,
        /** The primary key columns as the schema declares them. */
        val keyColumns: List<String>,
        /** `track_points.id`: an auto-generated key that is never carried across phones. */
        val autoKey: Boolean = false,
        /** OWNED only: the column naming the owning record, and that record's table. */
        val owner: Reference? = null,
        /** OWNED only: the other records a row names; each must exist on the phone or the row is dropped by a Merge. */
        val needs: List<Reference> = emptyList(),
        /** RECORD only: nullable link columns, set NULL by a Merge when their target is not on the phone. */
        val softLinks: List<Reference> = emptyList(),
    )

    /** The journal tables, in the order a Merge inserts them (records first, then what hangs off them). */
    val journal: List<TableSpec> = emptyList()

    /** Tables in the schema that are deliberately not journal data, and why (ruling 3 B does not list them). */
    val excluded: Map<String, String> = emptyMap()
}
