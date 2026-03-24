package com.gideongeng.music.utils

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object ErrorHandler {
    private val _errorEvents = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val errorEvents = _errorEvents.asSharedFlow()

    fun report(throwable: Throwable, userMessage: String? = null) {
        throwable.printStackTrace()
        val msg = userMessage ?: throwable.localizedMessage ?: "An unexpected error occurred"
        _errorEvents.tryEmit(msg)
    }
}
