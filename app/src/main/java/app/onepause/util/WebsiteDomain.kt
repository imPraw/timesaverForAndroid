package app.onepause.util

import java.net.IDN
import java.net.URI
import java.util.Locale

/** Converts a URL or hostname to a canonical DNS name; paths and ports are ignored. */
object WebsiteDomain {
    fun parse(input: String): String? {
        val value = input.trim()
        if (value.isEmpty() || value.any { it.isWhitespace() }) return null
        return runCatching {
            val uri = URI(if (value.contains("://")) value else "https://$value")
            if (uri.scheme !in listOf("http", "https") || uri.rawUserInfo != null) return null
            val host = uri.host ?: return null
            val domain = IDN.toASCII(host.trimEnd('.')).lowercase(Locale.ROOT)
            if (domain.length > 253 || !domain.contains('.') || domain.split('.').any {
                    it.isEmpty() || it.length > 63 || it.startsWith('-') || it.endsWith('-') ||
                        it.any { ch -> !ch.isLetterOrDigit() && ch != '-' }
                }) null else domain
        }.getOrNull()
    }

    fun matches(host: String, blocked: String): Boolean =
        host == blocked || host.endsWith(".$blocked")
}
