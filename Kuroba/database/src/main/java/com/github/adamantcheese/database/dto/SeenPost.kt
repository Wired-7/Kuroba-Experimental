package com.github.adamantcheese.database.dto

import org.joda.time.DateTime

data class SeenPost(
        val postUid: String,
        val parentLoadableUid: String,
        val postId: Long,
        val insertedAt: DateTime
)