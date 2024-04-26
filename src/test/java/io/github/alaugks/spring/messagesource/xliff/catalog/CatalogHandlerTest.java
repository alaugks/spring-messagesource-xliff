package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.records.Translation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CatalogHandlerTest {

    private final Locale locale = Locale.forLanguageTag("en");

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
        String domain = "messages";
        String key = domain + ".key";
        String localeKey = "en|" + key;
        List<Translation> translations = new ArrayList<>();
        translations.add(new Translation(this.locale, "key", "value_from_file", domain));
        var catalog = new BaseCatalog(translations, this.locale, domain).build();

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

    @Test
    void test_putFallbackToCache() {

        var xliffCatalogBuildernew = new XliffCatalogBuilder(
            TestUtilities.getResourcesLoader(
                Locale.forLanguageTag("en"),
                "translations_example/*"
            ).getTranslationFiles(),
            "messages",
            Locale.forLanguageTag("en"),
            null
        );

        var cache = TestUtilities.getCache();

        var catalogHandler = new CatalogHandler(
            xliffCatalogBuildernew.getBaseCatalog(),
            cache
        );

        // Check cache before the messages are fetched
        Map<Object, Object> cacheItems = TestUtilities.cacheToArray(cache);
        assertNull(cacheItems.get("en|headline"));
        assertEquals("Headline (en)", cacheItems.get("en|messages.headline"));
        assertNull(cacheItems.get("en-us|headline"));
        assertNull(cacheItems.get("en-us|messages.headline"));
        assertNull(cacheItems.get("jp|headline"));
        assertNull(cacheItems.get("jp|messages.headline"));
        assertEquals("Payment (es)", cacheItems.get("es|payment.headline"));
        assertNull(cacheItems.get("es-cr|payment.text"));

        // Fetch messages
        catalogHandler.get(Locale.forLanguageTag("en"), "headline");
        catalogHandler.get(Locale.forLanguageTag("en"), "messages.headline");
        catalogHandler.get(Locale.forLanguageTag("en-US"), "headline");
        catalogHandler.get(Locale.forLanguageTag("en-US"), "messages.headline");
        catalogHandler.get(Locale.forLanguageTag("jp"), "headline");
        catalogHandler.get(Locale.forLanguageTag("jp"), "messages.headline");
        catalogHandler.get(Locale.forLanguageTag("es"), "payment.headline");
        catalogHandler.get(Locale.forLanguageTag("es-cr"), "payment.text");

        // Check cache after the messages are fetched
        cacheItems = TestUtilities.cacheToArray(cache);
        assertEquals("Headline (en)", cacheItems.get("en|headline"));
        assertEquals("Headline (en)", cacheItems.get("en|messages.headline"));
        assertEquals("Headline (en)", cacheItems.get("en-us|headline"));
        assertEquals("Headline (en)", cacheItems.get("en-us|messages.headline"));
        assertEquals("Headline (en)", cacheItems.get("jp|headline"));
        assertEquals("Headline (en)", cacheItems.get("jp|messages.headline"));
        assertEquals("Payment (es)", cacheItems.get("es|payment.headline"));
        assertEquals("Payment Text (es)", cacheItems.get("es-cr|payment.text"));
    }

}
