package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.records.Translation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

class CatalogHandlerTest {

    private Locale locale;
    private String domain;

    @BeforeEach
    void beforeEach() {
        this.domain = "messages";
        this.locale = Locale.forLanguageTag("en");
    }

    @Test
    void test_getAll() {
        var catalogHandler = new CatalogHandler(
            TestUtilities.getTestBaseCatalog(),
            null
        );
        assertEquals(3, catalogHandler.getAll().size());
    }

    @Test
    void test_get() {
        var catalogHandler = new CatalogHandler(
            TestUtilities.getTestBaseCatalog(),
            null
        );
        assertEquals("Hello World (messages / en)", catalogHandler.get(this.locale, "hello_world"));
        assertEquals("Hello World (messages / en)", catalogHandler.get(this.locale, "messages.hello_world"));
    }

    @Test
    void test_handling_catalog_and_caching() {
        // Init BaseCatalog
        List<Translation> translations = new ArrayList<>();
        translations.add(new Translation(this.locale, "key", "value_from_file", this.domain));
        var catalog = new BaseCatalog(translations, this.locale, this.domain);

        var cache = new ConcurrentMapCacheManager("test-cache").getCache("test-cache");

        var catalogHandler = new CatalogHandler(
            catalog,
            cache
        );

        // Put translation to baseCatalog
        assertEquals("value_from_file", catalogHandler.get(this.locale, this.domain + ".key"));

        // BaseCatalog Hit
        assertEquals("value_from_file", catalogHandler.get(locale, "key"));

        var cacheBuffer = TestUtilities.cacheToArray(cache);
        assertEquals("value_from_file", cacheBuffer.get("en|key"));

        // CacheCatalog Hit
        // Overwrite cacheItem to test translation is from Cache
        cache.put("en|key", "value_from_cache");
        assertEquals("value_from_cache", catalogHandler.get(locale, "key"));
    }

}
