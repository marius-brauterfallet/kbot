package handlers

object Handlers {
    fun registerHandlers() {
        RoleUpdateHandler.init()
        RoleReactionHandler.init()
    }
}
