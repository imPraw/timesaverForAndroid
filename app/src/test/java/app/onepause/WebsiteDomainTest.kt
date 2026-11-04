package app.onepause

import app.onepause.util.WebsiteDomain
import org.junit.Assert.*
import org.junit.Test

class WebsiteDomainTest {
    @Test fun parsesUrlsAndNormalizesHost() {
        assertEquals("www.instagram.com", WebsiteDomain.parse("https://WWW.Instagram.com:443/path?q=1"))
        assertEquals("instagram.com", WebsiteDomain.parse("instagram.com"))
        assertNull(WebsiteDomain.parse("https://instagram.com.evil.com@evil.com"))
        assertNull(WebsiteDomain.parse("not a website"))
        assertNull(WebsiteDomain.parse("javascript:alert(1)"))
    }
    @Test fun matchesOnlyDomainAndSubdomains() {
        assertTrue(WebsiteDomain.matches("instagram.com", "instagram.com"))
        assertTrue(WebsiteDomain.matches("www.instagram.com", "instagram.com"))
        assertFalse(WebsiteDomain.matches("notinstagram.com", "instagram.com"))
        assertFalse(WebsiteDomain.matches("instagram.com.evil.com", "instagram.com"))
    }
}
