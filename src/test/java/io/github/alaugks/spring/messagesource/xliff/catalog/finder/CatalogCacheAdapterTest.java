package io.github.alaugks.spring.messagesource.xliff.catalog.finder;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogCache;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class CatalogCacheAdapterTest {
    @Test
    void test_find() {
        CacheManager cacheManager = TestUtilities.getMockedCacheManager();
        CatalogCache catalog = new CatalogCache(Locale.forLanguageTag("en"), "messages", cacheManager);

        catalog.put(Locale.forLanguageTag("en"), "domain", "key_1", "value_en_1");
        catalog.put(Locale.forLanguageTag("en"), "domain", "key_2", "value_en_2");
        catalog.put(Locale.forLanguageTag("en"), "domain", "key_1", "value_en_3");

        var adapter = new CatalogCacheAdapter(cacheManager.getCache(CatalogCache.CACHE_NAME));

        assertEquals("value_en_3", adapter.find(Locale.forLanguageTag("en"), "domain.key_1"));
        assertNull(adapter.find(Locale.forLanguageTag("en"), "domain.not_exists"));
    }
}
