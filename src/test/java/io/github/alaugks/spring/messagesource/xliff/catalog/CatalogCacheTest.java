package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceCacheNotExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("java:S5778")
class CatalogCacheTest {
    private CatalogCache catalogCache;
    private Locale locale;

    @BeforeEach
    void beforeEach() {
        this.catalogCache = new CatalogCache(
                Locale.forLanguageTag("en"),
                "messages",
                TestUtilities.getMockedCacheManager()
        );
        this.locale = Locale.forLanguageTag("en");
    }

    @Test
    void test_getAll() {
        var en = Locale.forLanguageTag("en");
        var de = Locale.forLanguageTag("de");

        this.catalogCache.put(en, "messages", "m_en_1", "targetValue_m_en_1");
        this.catalogCache.put(en, "messages", "m_en_2", "targetValue_m_en_2");
        this.catalogCache.put(en, "domain", "d_en_1", "targetValue_d_en_1");
        this.catalogCache.put(de, "messages", "m_de_1", "targetValue_m_de_1");
        this.catalogCache.put(de, "messages", "m_de_2", "targetValue_m_de_2");
        this.catalogCache.put(de, "domain", "d_de_1", "targetValue_d_de_1");

        var all = this.catalogCache.getAll();
        var transEn = all.get(en.toString());
        var transDe = all.get(de.toString());

        assertAll(
                () -> assertEquals("targetValue_m_en_1", transEn.get("messages.m_en_1")),
                () -> assertEquals("targetValue_m_en_2", transEn.get("messages.m_en_2")),
                () -> assertEquals("targetValue_d_en_1", transEn.get("domain.d_en_1")),
                () -> assertEquals("targetValue_m_de_1", transDe.get("messages.m_de_1")),
                () -> assertEquals("targetValue_m_de_2", transDe.get("messages.m_de_2")),
                () -> assertEquals("targetValue_d_de_1", transDe.get("domain.d_de_1"))
        );
    }

    @Test
    void test_put_get_withDomain() {
        this.catalogCache.put(this.locale, "domain", "code", "targetValue");
        assertEquals("targetValue", this.catalogCache.get(this.locale, "domain.code"));
    }

    @Test
    void test_put_get() {
        Locale locale = Locale.forLanguageTag("en");
        this.catalogCache.put(this.locale, "code", "targetValue");
        assertEquals("targetValue", this.catalogCache.get(this.locale, "code"));
    }

    @Test
    void test_get_onNull() {
        this.catalogCache.put(this.locale, "domain", "code", "targetValue");
        assertNull(this.catalogCache.get(this.locale, "domain.foo"));
    }

    @Test
    void test_get_onNull_localeEmpty() {
        this.catalogCache.put(this.locale, "domain", "code", "targetValue");
        assertNull(this.catalogCache.get(Locale.forLanguageTag(""), "domain.foo"));
    }

    @Test
    void test_put_not_overwrite() {
        this.catalogCache.put(this.locale, "domain", "code", "targetValue_1");
        this.catalogCache.put(this.locale, "domain", "code", "targetValue_2");
        this.catalogCache.put(this.locale, "domain", "code", "targetValue_3");
        assertEquals("targetValue_1", this.catalogCache.get(this.locale, "domain.code"));
    }

    @Test
    void test_exception_cacheNameNotExists() {
        CacheManager cacheManager = TestUtilities.getMockedCacheManager("CACHE_NAME_NOT_EXISTS");

        XliffMessageSourceCacheNotExistsException exception = assertThrows(
            XliffMessageSourceCacheNotExistsException.class, () -> {
                new CatalogCache(Locale.forLanguageTag("en"), "messages", cacheManager);
            }
        );
        assertEquals(
                "Cache with name [messagesource.xliff.catalog.CACHE] not available.",
                exception.getMessage()
        );
    }

    @Test
    void test_exception() {
        XliffMessageSourceCacheNotExistsException exception = assertThrows(
            XliffMessageSourceCacheNotExistsException.class, () -> {
                new CatalogCache(Locale.forLanguageTag("en"), "messages", null);
            }
        );
        assertEquals("org.springframework.cache.CacheManager not available.", exception.getMessage());
    }
}
