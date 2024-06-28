package model

import kotlinx.serialization.Serializable

@Serializable
data class UserRole(
    val name: String,
    val emoji: String
)

@Serializable
data class UserRoles(
    val roles: List<UserRole>
)