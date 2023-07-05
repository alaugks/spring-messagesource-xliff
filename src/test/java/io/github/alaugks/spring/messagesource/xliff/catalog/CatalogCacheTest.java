package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceCacheNotExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import java.util.HashMap;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class CatalogCacheTest {
    private CatalogCache catalogCache;
    private Locale locale;

    @BeforeEach
    void beforeEach() {
        this.catalogCache = new CatalogCache(TestUtilities.getMockedCacheManager());
        this.locale = Locale.forLanguageTag("en");
    }

    @Test
    void test_initCache() {
        Catalog catalog = new Catalog();
        catalog.put(this.locale, "domain", "code", "targetValue");
        this.catalogCache.initCache(catalog);
        assertTrue(catalog.has(locale, "domain.code"));
    }

    @Test
    void test_getAll() {
        assertInstanceOf(HashMap.class, this.catalogCache.getAll());
    }

    @Test
    void test_put_get_withDomain() {
        this.catalogCache.put(this.locale, "domain", "code", "targetValue");
        assertEquals("targetValue", this.catalogCache.get(locale, "domain.code"));
    }

    @Test
    void test_put_has_withDomain() {
        Locale locale = Locale.forLanguageTag("en");
        this.catalogCache.put(this.locale, "domain", "code", "targetValue");
        assertTrue(this.catalogCache.has(locale, "domain.code"));
        assertFalse(this.catalogCache.has(locale, "domain.bar"));
    }

    @Test
    void test_put_get() {
        Locale locale = Locale.forLanguageTag("en");
        this.catalogCache.put(this.locale, "code", "targetValue");
        assertEquals("targetValue", this.catalogCache.get(locale, "code"));
    }

    @Test
    void test_put_has() {
        Locale locale = Locale.forLanguageTag("en");
        this.catalogCache.put(this.locale, "code", "targetValue");
        assertTrue(this.catalogCache.has(locale, "code"));
        assertFalse(this.catalogCache.has(locale, "bar"));
    }

    @Test
    void test_get_onNull() {
        this.catalogCache.put(this.locale, "domain", "code", "targetValue");
        assertNull(this.catalogCache.get(locale, "domain.foo"));
    }

    @Test
    void test_exception_cacheNameNotExists() {
        CacheManager cacheManager = TestUtilities.getMockedCacheManager("CACHE_NAME_NOT_EXISTS");

        XliffMessageSourceCacheNotExistsException exception = assertThrows(
            XliffMessageSourceCacheNotExistsException.class, () -> {
                new CatalogCache(cacheManager);
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
                new CatalogCache(null);
            }
        );
        assertEquals("org.springframework.cache.CacheManager not available.", exception.getMessage());
    }

    @Test
    void test_Constants() {
        assertEquals("messagesource.xliff.catalog.CACHE", CatalogCache.CACHE_NAME);
    }
}
