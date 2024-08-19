package services

import model.UserRole
import reactor.core.publisher.Flux

interface GuildRolesService {
//    fun updateRoles(): Mono<Unit>
    fun getRoleByEmoji(emoji: String): UserRole?
    fun updateUserRoles(): Flux<Void>
}