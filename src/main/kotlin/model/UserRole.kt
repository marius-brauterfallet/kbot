package model

import kotlinx.serialization.Serializable

@Serializable
data class UserRole(
    val emoji: String,
    val id: Long,
    val name: String
)

@Serializable
data class UserRoles(
    val roles: List<UserRole>
)