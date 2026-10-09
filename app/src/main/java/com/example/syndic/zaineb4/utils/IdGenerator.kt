package com.example.syndic.zaineb4.utils

import java.util.concurrent.atomic.AtomicLong

/**
 * Thread-safe monotonic ID generator based on timestamps.
 * Guarantees that every generated ID is strictly unique and sequentially ordered,
 * avoiding collisions even when items are created within the same millisecond.
 */
object IdGenerator {
    private val lastTimestamp = AtomicLong(0L)

    fun generateId(): Long {
        while (true) {
            val now = System.currentTimeMillis()
            val last = lastTimestamp.get()
            val next = if (now > last) now else last + 1L
            if (lastTimestamp.compareAndSet(last, next)) {
                return next
            }
        }
    }
}
