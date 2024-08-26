package services

interface LunchService {
    suspend fun getMenus(withDate: Boolean): Result<String>
}