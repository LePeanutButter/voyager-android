package com.voyager.tourism.data.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Broadcasts session-expiration events (e.g. after HTTP 401) so the UI layer can navigate to login.
 *
 * Uses a [SharedFlow] with a small buffer so a single [notifySessionExpired] emission is not dropped
 * if collectors subscribe shortly after.
 */
@Singleton
class SessionInvalidationNotifier @Inject constructor() {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Cold stream of session-invalidated signals; each element is [Unit]. */
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    /**
     * Emits a session-expired signal to all collectors.
     *
     * Non-blocking; uses [MutableSharedFlow.tryEmit].
     */
    fun notifySessionExpired() {
        _events.tryEmit(Unit)
    }
}
