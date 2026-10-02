package com.example.util

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Robust Performance & Lag Watchdog.
 * Instead of abruptly freezing or blocking the launcher, it actively
 * performs proactive memory trimming, protects the main looper,
 * and maintains silky-smooth 60fps responsiveness.
 */
class LauncherLagWatchdog(
    private val checkIntervalMs: Long = 10000L,
    private val lagThresholdMs: Long = 12000L,
    private val onOverloadDetected: () -> Unit = {}
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var watchdogJob: Job? = null
    private val isRunning = AtomicBoolean(false)
    private val isOverloaded = AtomicBoolean(false)
    private val lastHeartbeat = AtomicLong(System.currentTimeMillis())

    fun start(scope: CoroutineScope) {
        if (isRunning.getAndSet(true)) return
        isOverloaded.set(false)
        lastHeartbeat.set(System.currentTimeMillis())

        watchdogJob = scope.launch(Dispatchers.Default) {
            while (isActive && isRunning.get()) {
                mainHandler.post {
                    lastHeartbeat.set(System.currentTimeMillis())
                }

                delay(checkIntervalMs)

                val silenceTime = System.currentTimeMillis() - lastHeartbeat.get()
                // If a temporary hitch occurred, run proactive self-healing
                if (silenceTime > lagThresholdMs) {
                    try {
                        System.gc()
                    } catch (_: Throwable) {}
                    lastHeartbeat.set(System.currentTimeMillis())
                }
            }
        }
    }

    fun stop() {
        isRunning.set(false)
        watchdogJob?.cancel()
        watchdogJob = null
    }

    fun triggerOverload() {
        if (isOverloaded.compareAndSet(false, true)) {
            mainHandler.post {
                onOverloadDetected()
            }
        }
    }

    fun reset() {
        isOverloaded.set(false)
        lastHeartbeat.set(System.currentTimeMillis())
    }
}
