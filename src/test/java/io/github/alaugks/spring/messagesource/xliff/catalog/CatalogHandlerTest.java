package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.records.Translation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        String key = this.domain + ".key";
        String localeKey = "en|" + key;
        List<Translation> translations = new ArrayList<>();
        translations.add(new Translation(this.locale, "key", "value_from_file", this.domain));
        var catalog = new BaseCatalog(translations, this.locale, this.domain).build();

        var cache = TestUtilities.getCache();

        // Is translation in cache?
        var cacheBuffer = TestUtilities.cacheToArray(cache);
        assertNull(cacheBuffer.get(localeKey));

        var catalogHandler = new CatalogHandler(
            catalog,
            cache
        );

        // Cache is build
        cacheBuffer = TestUtilities.cacheToArray(cache);
        assertEquals("value_from_file", cacheBuffer.get(localeKey));

        // Hit
        assertEquals("value_from_file", catalogHandler.get(this.locale, key));

        // CacheCatalog Hit
        // Overwrite cacheItem to test translation is from Cache
        cache.put(localeKey, "value_from_cache");
        assertEquals("value_from_cache", catalogHandler.get(locale, key));
    }

}
