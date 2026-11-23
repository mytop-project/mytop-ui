package dev.mytop.spike

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.plus
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.get
import platform.windows.FILETIME
import platform.windows.GetCurrentProcess
import platform.windows.GetModuleHandleA
import platform.windows.GetProcAddress
import platform.windows.GetProcessTimes
import platform.windows.GetSystemTimeAsFileTime
import platform.windows.HANDLE

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

/**
 * The working set in KiB. Kotlin/Native's platform.windows has no psapi, so K32GetProcessMemoryInfo
 * is looked up in kernel32 and called through a function pointer on a raw PROCESS_MEMORY_COUNTERS
 * (x64 layout: cb at 0, WorkingSetSize at 16, 72 bytes).
 */
@OptIn(ExperimentalForeignApi::class)
actual fun residentKb(): Long = memScoped {
    val proc = GetProcAddress(GetModuleHandleA("kernel32.dll"), "K32GetProcessMemoryInfo") ?: return -1
    val fn = proc.reinterpret<CFunction<(HANDLE?, CPointer<ByteVar>?, UInt) -> Int>>()
    val buf = allocArray<ByteVar>(72)
    buf.reinterpret<UIntVar>()[0] = 72u
    fn.invoke(GetCurrentProcess(), buf, 72u)
    (buf + 16)!!.reinterpret<ULongVar>()[0].toLong() / 1024
}

actual fun quitNow() {
    platform.posix.fflush(null)
    platform.posix._exit(0)
}
