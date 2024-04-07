package io.github.alaugks.spring.messagesource.xliff.catalog.finder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogCache;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;


class CatalogCacheAdapterTest {
    @Test
    void test_find() {
        Cache cache = TestUtilities.getCache();
        CatalogCache catalog = new CatalogCache(Locale.forLanguageTag("en"), "messages", cache);

        catalog.put(Locale.forLanguageTag("en"), "domain", "key_1", "value_en_1");
        catalog.put(Locale.forLanguageTag("en"), "domain", "key_2", "value_en_2");
        catalog.put(Locale.forLanguageTag("en"), "domain", "key_1", "value_en_3");

        var adapter = new CatalogCacheAdapter(cache);

        assertEquals("value_en_1", adapter.find(Locale.forLanguageTag("en"), "domain.key_1"));
        assertNull(adapter.find(Locale.forLanguageTag("en"), "domain.not_exists"));
    }
}
