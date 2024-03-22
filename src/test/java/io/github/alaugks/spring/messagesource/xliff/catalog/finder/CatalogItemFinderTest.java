package io.github.alaugks.spring.messagesource.xliff.catalog.finder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.catalog.Catalog;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogCache;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

class CatalogItemFinderTest {

    @Test
    void test_ioFinder() {
        Catalog catalog = new Catalog(Locale.forLanguageTag("en"), "messages");
        catalog.put(Locale.forLanguageTag("en"), "domain", "key_1", "value_en_1");
        catalog.put(Locale.forLanguageTag("en"), "domain", "key_2", "value_en_2");
        catalog.put(Locale.forLanguageTag("en"), "domain", "key_1", "value_en_3");
        catalog.put(Locale.forLanguageTag("de"), "domain", "key_1", "value_de_1");
        catalog.put(Locale.forLanguageTag("de"), "domain", "key_2", "value_de_2");
        catalog.put(Locale.forLanguageTag("de"), "domain", "key_1", "value_de_3");

        var finder = new CatalogFinder(
                new CatalogFileAdapter(catalog.getAll()),
                Locale.forLanguageTag("en"),
                "domain"
        );

        assertEquals("value_en_1", finder.find(Locale.forLanguageTag("en"), "domain.key_1"));
        assertEquals("value_en_1", finder.find(Locale.forLanguageTag("en"), "key_1"));
        assertEquals("value_en_1", finder.find(Locale.forLanguageTag("en-UK"), "domain.key_1"));
        assertEquals("value_en_1", finder.find(Locale.forLanguageTag("en-UK"), "key_1"));
        assertEquals("value_en_1", finder.find(Locale.forLanguageTag("jp"), "key_1"));
    }

    @Test
    void test_cacheFinder() {
        CacheManager cacheManager = TestUtilities.getMockedCacheManager();
        CatalogCache catalog = new CatalogCache(Locale.forLanguageTag("en"), "messages", cacheManager);

        catalog.put(Locale.forLanguageTag("en"), "domain", "key_1", "value_en_1");
        catalog.put(Locale.forLanguageTag("en"), "domain", "key_2", "value_en_2");
        catalog.put(Locale.forLanguageTag("en"), "domain", "key_1", "value_en_3");
        catalog.put(Locale.forLanguageTag("de"), "domain", "key_1", "value_de_1");
        catalog.put(Locale.forLanguageTag("de"), "domain", "key_2", "value_de_2");
        catalog.put(Locale.forLanguageTag("de"), "domain", "key_1", "value_de_3");

        var finder = new CatalogFinder(
                new CatalogCacheAdapter(cacheManager.getCache(CatalogCache.CACHE_NAME)),
                Locale.forLanguageTag("en"),
                "domain"
        );

        assertEquals("value_en_1", finder.find(Locale.forLanguageTag("en"), "domain.key_1"));
        assertEquals("value_en_1", finder.find(Locale.forLanguageTag("en"), "key_1"));
        assertEquals("value_en_1", finder.find(Locale.forLanguageTag("en-UK"), "domain.key_1"));
        assertEquals("value_en_1", finder.find(Locale.forLanguageTag("en-UK"), "key_1"));
        assertEquals("value_en_1", finder.find(Locale.forLanguageTag("jp"), "key_1"));
    }

}
