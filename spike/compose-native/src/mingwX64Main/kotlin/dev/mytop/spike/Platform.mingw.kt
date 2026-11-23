package dev.mytop.spike

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.windows.FILETIME
import platform.windows.GetCurrentProcess
import platform.windows.GetProcessTimes
import platform.windows.GetSystemTimeAsFileTime
import platform.windows.K32GetProcessMemoryInfo
import platform.windows.PROCESS_MEMORY_COUNTERS

private fun FILETIME.ticks(): Long = (dwHighDateTime.toLong() shl 32) or dwLowDateTime.toLong()

/** Now minus the process creation time, both FILETIME (100 ns units). */
@OptIn(ExperimentalForeignApi::class)
actual fun msSinceProcessStart(): Long = memScoped {
    val created = alloc<FILETIME>()
    val exited = alloc<FILETIME>()
    val kernel = alloc<FILETIME>()
    val user = alloc<FILETIME>()
    val now = alloc<FILETIME>()
    GetProcessTimes(GetCurrentProcess(), created.ptr, exited.ptr, kernel.ptr, user.ptr)
    GetSystemTimeAsFileTime(now.ptr)
    (now.ticks() - created.ticks()) / 10_000
}

/** The working set, in KiB. */
@OptIn(ExperimentalForeignApi::class)
actual fun residentKb(): Long = memScoped {
    val counters = alloc<PROCESS_MEMORY_COUNTERS>()
    counters.cb = sizeOf<PROCESS_MEMORY_COUNTERS>().toUInt()
    K32GetProcessMemoryInfo(GetCurrentProcess(), counters.ptr, counters.cb)
    counters.WorkingSetSize.toLong() / 1024
}
