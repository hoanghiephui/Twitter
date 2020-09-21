package com.twidere.twiderex.db.model

import androidx.room.*
import com.twidere.twiderex.model.PlatformType
import com.twidere.twiderex.model.UserKey

@Entity(
    tableName = "timeline",
    indices = [Index(value = ["statusId"], unique = true)],
)
data class DbTimeline(
    @PrimaryKey
    val _id: String,
    val userKey: UserKey,
    val platformType: PlatformType,
    val timestamp: Long,
    var isGap: Boolean,
    val statusId: String,
    val retweetId: String?,
    val quoteId: String?,
)

data class DbTimelineWithStatus(
    @Embedded
    val timeline: DbTimeline,

    @Relation(
        parentColumn = "statusId",
        entityColumn = "statusId",
        entity = DbStatus::class,
    )
    val status: DbStatusWithMedia,
    @Relation(
        parentColumn = "retweetId",
        entityColumn = "statusId",
        entity = DbStatus::class,
    )
    val retweet: DbStatusWithMedia?,
    @Relation(
        parentColumn = "quoteId",
        entityColumn = "statusId",
        entity = DbStatus::class,
    )
    val quote: DbStatusWithMedia?,
)