package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.records.Translation;
import java.util.ArrayList;
import java.util.List;
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

        List<Translation> translations = new ArrayList<>();
        translations.add(new Translation(this.locale, "key", "value_from_file", this.domain));

        // Init CacheCatalog
        var catalogCache = new CacheCatalog(this.cache);
        // Init BaseCatalog
        var catalog = new BaseCatalog(translations, this.locale, this.domain);
        // Set Chain of Responsibility
        catalogCache.nextHandler(catalog);

        // Put translation to baseCatalog
        assertEquals("value_from_file", catalog.get(this.locale, this.domain + ".key"));

        // Is translation in catalogCache (NOT)
        cache = TestUtilities.cacheToArray(this.cache);
        assertNull(cache.get("en|key"));

        // BaseCatalog Hit
        assertEquals("value_from_file", catalogCache.get(locale, "key"));

        // Is translation in catalogCache (YES)
        cache = TestUtilities.cacheToArray(this.cache);
        assertEquals("value_from_file", cache.get("en|key"));

        // CacheCatalog Hit
        // Overwrite cacheItem to test translation is from Cache
        this.cache.put("en|key", "value_from_cache");
        assertEquals("value_from_cache", catalogCache.get(locale, "key"));
    }
}
