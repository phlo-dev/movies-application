package com.pedro.domain.usecases

import com.pedro.domain.models.Response
import com.pedro.domain.models.ThreadContextProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

abstract class UseCase<P, R>(private val scope: CoroutineScope) : KoinComponent {
    private val threadContextProvider: ThreadContextProvider by inject()

    protected abstract suspend fun getResult(params: P): Response<R>

    fun execute(
        params: P,
        onSuccess: (R) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        scope.launch(threadContextProvider.io) {
            val result = getResult(params)
            withContext(threadContextProvider.main) {
                when (result) {
                    is Response.Success -> onSuccess(result.data)
                    is Response.Failure -> onFailure(result.throwable)
                }
            }
        }
    }

    fun cancel() = scope.coroutineContext.cancel()
}