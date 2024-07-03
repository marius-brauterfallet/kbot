package resources

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import model.UserRoles

private val rolesResource = {}.javaClass.getResourceAsStream("/roles.json")
    ?: throw Exception("Could not get resource 'roles.json'")

@OptIn(ExperimentalSerializationApi::class)
val guildUserRoles = Json.decodeFromStream<UserRoles>(rolesResource).roles