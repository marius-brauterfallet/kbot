import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import model.UserRole
import model.UserRoles

@OptIn(ExperimentalSerializationApi::class)
object Resources {
    val userRoles: List<UserRole>

    init {
        val rolesResource = javaClass.getResourceAsStream("roles.json")
            ?: throw Exception("Could not get resource 'roles.json'")

        userRoles = Json.decodeFromStream<UserRoles>(rolesResource).roles
    }
}