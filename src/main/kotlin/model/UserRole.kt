package model

import discord4j.common.util.Snowflake

data class UserRole(
    val emoji: String,
    val id: Snowflake,
)