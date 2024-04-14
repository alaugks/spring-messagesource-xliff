package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import java.util.Locale;
import java.util.Map;
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
    void test_handling_catalog_and_caching() {
        Map<Object, Object> cache;

        // Init CatalogCache
        var catalogCache = new CatalogCache(this.cache);
        // Init Catalog
        var catalog = new Catalog(this.locale, this.domain);
        // Set Chain of Responsibility
        catalogCache.nextHandle(catalog);

        // Put translation to catalog
        catalog.put(this.locale, this.domain, "key", "value_from_file");
        assertEquals("value_from_file", catalog.get(this.locale, this.domain + ".key"));

        // Is translation in catalogCache (NOT)
        cache = TestUtilities.cacheToArray(this.cache);
        assertNull(cache.get("en|key"));

        // Catalog Hit
        assertEquals("value_from_file", catalogCache.get(locale, "key"));

        // Is translation in catalogCache (YES)
        cache = TestUtilities.cacheToArray(this.cache);
        assertEquals("value_from_file", cache.get("en|key"));

        // CatalogCache Hit
        // Overwrite cacheItem to test translation is from Cache
        this.cache.put("en|key", "value_from_cache");
        assertEquals("value_from_cache", catalogCache.get(locale, "key"));
    }
}
