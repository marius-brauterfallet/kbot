package services

import model.lunch.MenuItem

interface LunchService {
    suspend fun getMenus(withDate: Boolean): Result<String>
    fun parseMenu(menuHtml: String): Result<List<MenuItem>>
}