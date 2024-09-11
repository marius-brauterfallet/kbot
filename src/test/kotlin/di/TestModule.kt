package di

import io.ktor.client.*
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.slf4j.Logger
import services.LunchService
import services.LunchServiceImpl

val testModule = module {
    single<Logger> { mockk(relaxed = true) }
    single<HttpClient> { mockk(relaxed = true) }
    single<CoroutineScope> { mockk(relaxed = true) }

    singleOf(::LunchServiceImpl) bind LunchService::class
}