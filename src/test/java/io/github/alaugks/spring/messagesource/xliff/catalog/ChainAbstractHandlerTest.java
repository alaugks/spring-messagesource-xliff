package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.XliffCacheableKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChainAbstractHandlerTest {

    private String domain;
    private Locale locale;
    private ConcurrentMapCacheManager cacheManager;

    @BeforeEach
    void beforeEach() {
        this.domain = "messages";
        this.locale = Locale.forLanguageTag("en");
        this.cacheManager = new ConcurrentMapCacheManager();
        this.cacheManager.setCacheNames(List.of(CatalogCache.CACHE_NAME));
    }

    @Test
    void test_get() {
        // Init CatalogCache
        var catalogCache = new CatalogCache(this.locale, this.domain, this.cacheManager);

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
        var cache = this.cacheManager.getCache(CatalogCache.CACHE_NAME);
        cache.evict(XliffCacheableKeyGenerator.createCode(this.locale, this.domain+".key"));

        // Now hit Catalog (Chain of Responsibility: CatalogCache -> Catalog)
        assertEquals("value_from_file", catalogCache.get(this.locale, "key"));
    }

    @Test
    void test_initCache() {
        // Init CatalogCache
        var catalogCache = new CatalogCache(this.locale, this.domain, this.cacheManager);

        // Init Catalog
        var catalog = new Catalog(this.locale, this.domain);

        // Set Chain of Responsibility
        catalogCache.setNextHandler(catalog);

        // Add item in CatalogCache and Catalog with the same Key
        catalog.put(this.locale, this.domain, "key", "value_from_file");

        // Create CacheKey
        var key = XliffCacheableKeyGenerator.createCode(this.locale, this.domain+".key");

        // Get Cache
        var cache = this.cacheManager.getCache(CatalogCache.CACHE_NAME);
        var cacheAsArray = TestUtilities.cacheToArray(cache);

        // Key must not exist
        assertNull(cacheAsArray.get(key));

        // Init Cache (Chain of Responsibility: CatalogCache -> Catalog)
        catalogCache.initCache();

        // Get Cache
        cache = this.cacheManager.getCache(CatalogCache.CACHE_NAME);
        cacheAsArray = TestUtilities.cacheToArray(cache);

        // Key must exist
        assertEquals("value_from_file", cacheAsArray.get(key));
    }
}
