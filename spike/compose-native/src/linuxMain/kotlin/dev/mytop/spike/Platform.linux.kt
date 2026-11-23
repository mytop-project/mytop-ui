package dev.mytop.spike

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString
import platform.posix._SC_CLK_TCK
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.sysconf

@OptIn(ExperimentalForeignApi::class)
private fun readText(path: String): String {
    val file = fopen(path, "r") ?: return ""
    val out = StringBuilder()
    try {
        memScoped {
            val buf = allocArray<ByteVar>(4096)
            while (true) {
                val n = fread(buf, 1u, 4095u, file).toInt()
                if (n <= 0) break
                buf[n] = 0
                out.append(buf.toKString())
            }
        }
    } finally {
        fclose(file)
    }
    return out.toString()
}

/** Uptime minus the process start time in /proc/self/stat (field 22, clock ticks since boot). */
actual fun msSinceProcessStart(): Long {
    val stat = readText("/proc/self/stat")
    val fields = stat.substringAfterLast(')').trim().split(' ')
    val startTicks = fields.getOrNull(19)?.toLongOrNull() ?: return -1
    val uptimeSeconds = readText("/proc/uptime").substringBefore(' ').toDoubleOrNull() ?: return -1
    @OptIn(ExperimentalForeignApi::class)
    val hz = sysconf(_SC_CLK_TCK).toDouble()
    return ((uptimeSeconds - startTicks / hz) * 1000.0).toLong()
}

actual fun residentKb(): Long =
    readText("/proc/self/status").lineSequence()
        .firstOrNull { it.startsWith("VmRSS:") }
        ?.removePrefix("VmRSS:")?.trim()?.substringBefore(' ')?.toLongOrNull() ?: -1

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun quitNow() {
    platform.posix.fflush(null)
    platform.posix._exit(0)
}
