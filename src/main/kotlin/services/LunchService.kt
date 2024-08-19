package services

interface LunchService {
    fun getMenus(withDate: Boolean): Result<String>
}