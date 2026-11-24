package com.checo.techforge.domain

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.security.MessageDigest

data class HashInfo(val bytesRead: Long, val sha256: String)
data class LimitedText(val text: String, val truncated: Boolean, val bytesRead: Int)

/** Calculates SHA-256 incrementally; file size does not determine memory use. */
fun sha256(input: InputStream): HashInfo {
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(64 * 1024)
    var total = 0L
    while (true) {
        val read = input.read(buffer)
        if (read < 0) break
        if (read == 0) continue
        digest.update(buffer, 0, read)
        total += read
    }
    val hash = digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
    return HashInfo(total, hash)
}

/** Reads at most maxBytes + 1 to detect truncation without loading an unbounded file. */
fun readUtf8Limited(input: InputStream, maxBytes: Int): LimitedText {
    require(maxBytes >= 0) { "maxBytes must be non-negative" }
    val output = ByteArrayOutputStream(minOf(maxBytes, 64 * 1024))
    val buffer = ByteArray(16 * 1024)
    var remaining = maxBytes.toLong() + 1L
    while (remaining > 0L) {
        val requested = minOf(buffer.size.toLong(), remaining).toInt()
        val read = input.read(buffer, 0, requested)
        if (read < 0) break
        if (read == 0) continue
        output.write(buffer, 0, read)
        remaining -= read
    }
    val bytes = output.toByteArray()
    val truncated = bytes.size > maxBytes
    val content = if (truncated) bytes.copyOf(maxBytes) else bytes
    return LimitedText(content.toString(Charsets.UTF_8), truncated, content.size)
}
