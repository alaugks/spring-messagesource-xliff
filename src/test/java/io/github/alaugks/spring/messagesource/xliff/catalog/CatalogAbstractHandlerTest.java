package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

class CatalogAbstractHandlerTest {

    private String domain;
    private Locale locale;
    private Cache cache;

    @BeforeEach
    void beforeEach() {
        this.domain = "messages";
        this.locale = Locale.forLanguageTag("en");
        this.cache = TestUtilities.getCache();
    }

    @Test
    void test_get() {
        // Init CatalogCache
        var catalogCache = new CatalogCache(this.locale, this.domain, this.cache);

        // Init Catalog
        var catalog = new Catalog(this.locale, this.domain);

        // Set Chain of Responsibility
        catalogCache.setNextHandler(catalog);

        // Add item in CatalogCache and Catalog with the same Key
        catalogCache.put(this.locale, this.domain, "key", "value_from_cache");
        catalog.put(this.locale, this.domain, "key", "value_from_file");

        // CatalogCache hit
        assertEquals("value_from_cache", catalogCache.get(locale, "key"));

        // Remove items from CatalogCache
        cache.evict(CatalogUtilities.createCode(this.locale, this.domain + ".key"));

        // Now hit Catalog (Chain of Responsibility: CatalogCache -> Catalog)
        assertEquals("value_from_file", catalogCache.get(this.locale, "key"));
    }

    @Test
    void test_initCache() {
        // Create CacheKey
        var key = CatalogUtilities.createCode(this.locale, this.domain + ".key");

        // Init CatalogCache
        var catalogCache = new CatalogCache(this.locale, this.domain, this.cache);

        // Init Catalog
        var catalog = new Catalog(this.locale, this.domain);

        // Set Chain of Responsibility
        catalogCache.setNextHandler(catalog);

        // Add item in CatalogCache and Catalog with the same Key
        catalog.put(this.locale, this.domain, "key", "value");

        // Get Cache;
        var cacheAsArray = TestUtilities.cacheToArray(this.cache);

        // Key must not exist
        assertNull(cacheAsArray.get("key"));

        // Init Cache (Chain of Responsibility: CatalogCache -> Catalog)
        catalogCache.initCache();

        // Get Cache
        cacheAsArray = TestUtilities.cacheToArray(this.cache);

        // Key must exist
        assertEquals(
            "value",
            cacheAsArray.get(key)
        );
    }
}
