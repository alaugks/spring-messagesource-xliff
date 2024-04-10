package io.github.alaugks.spring.messagesource.xliff.catalog.finder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogCache;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

@SuppressWarnings("java:S5961")
class CatalogFinderTest {

    @Test
    void test_emptyLocale() {
        Cache cache = TestUtilities.getCache();
        CatalogCache catalog = new CatalogCache(Locale.forLanguageTag("en"), "messages", cache);

        catalog.put(Locale.forLanguageTag("en"), "domain", "m_key_1", "value_en_1");

        var finder = new CatalogFinder(
            new CatalogCacheAdapter(cache),
            Locale.forLanguageTag("en"),
            "domain"
        );

        assertNull(finder.find(Locale.forLanguageTag(""), "domain.m_key_1"));
    }

    @Test
    void test_find_defaultLanguage() {
        Cache cache = TestUtilities.getCache();
        CatalogCache catalog = new CatalogCache(Locale.forLanguageTag("en"), "messages", cache);

        // messages
        catalog.put(Locale.forLanguageTag("en"), "messages", "m_key_1", "m_value_en_1");
        catalog.put(Locale.forLanguageTag("en-US"), "messages", "m_key_1", "m_value_en_us_1");
        catalog.put(Locale.forLanguageTag("de"), "messages", "m_key_1", "m_value_de_1");
        catalog.put(Locale.forLanguageTag("de-CH"), "messages", "m_key_1", "m_value_de_ch_1");

        // domain
        catalog.put(Locale.forLanguageTag("en"), "domain", "d_key_1", "d_value_en_1");
        catalog.put(Locale.forLanguageTag("en-US"), "domain", "d_key_1", "d_value_en_us_1");
        catalog.put(Locale.forLanguageTag("de"), "domain", "d_key_1", "d_value_de_1");
        catalog.put(Locale.forLanguageTag("de-CH"), "domain", "d_key_1", "d_value_de_ch_1");

        var finder = new CatalogFinder(
            new CatalogCacheAdapter(cache),
            Locale.forLanguageTag("en"),
            "messages"
        );

        // messages
        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("en"), "messages.m_key_1"));
        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("en"), "m_key_1"));

        assertEquals("m_value_en_us_1", finder.find(Locale.forLanguageTag("en-US"), "messages.m_key_1"));
        assertEquals("m_value_en_us_1", finder.find(Locale.forLanguageTag("en-US"), "m_key_1"));

        assertEquals("m_value_de_1", finder.find(Locale.forLanguageTag("de"), "messages.m_key_1"));
        assertEquals("m_value_de_1", finder.find(Locale.forLanguageTag("de"), "m_key_1"));

        assertEquals("m_value_de_ch_1", finder.find(Locale.forLanguageTag("de-CH"), "messages.m_key_1"));
        assertEquals("m_value_de_ch_1", finder.find(Locale.forLanguageTag("de-CH"), "m_key_1"));

        // messages Fallback de-AT -> de
        assertEquals("m_value_de_1", finder.find(Locale.forLanguageTag("de-AT"), "messages.m_key_1"));
        assertEquals("m_value_de_1", finder.find(Locale.forLanguageTag("de-AT"), "m_key_1"));

        // messages Fallback en-GB -> en
        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("en-GB"), "messages.m_key_1"));
        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("en-GB"), "m_key_1"));

        // messages Fallback it -> en
        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("it"), "messages.m_key_1"));
        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("it"), "m_key_1"));

        // messages Fallback it-CH -> en
        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("it-CH"), "messages.m_key_1"));
        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("it-CH"), "m_key_1"));

        // domain
        assertEquals("d_value_en_1", finder.find(Locale.forLanguageTag("en"), "domain.d_key_1"));

        assertEquals("d_value_en_us_1", finder.find(Locale.forLanguageTag("en-US"), "domain.d_key_1"));

        assertEquals("d_value_de_1", finder.find(Locale.forLanguageTag("de"), "domain.d_key_1"));

        assertEquals("d_value_de_ch_1", finder.find(Locale.forLanguageTag("de-CH"), "domain.d_key_1"));

        // domain Fallback de-AT -> de
        assertEquals("d_value_de_1", finder.find(Locale.forLanguageTag("de-AT"), "domain.d_key_1"));

        // domain Fallback en-GB -> en
        assertEquals("d_value_en_1", finder.find(Locale.forLanguageTag("en-GB"), "domain.d_key_1"));

        // domain Fallback it -> en
        assertEquals("d_value_en_1", finder.find(Locale.forLanguageTag("it"), "domain.d_key_1"));

        // domain Fallback it-CH -> en
        assertEquals("d_value_en_1", finder.find(Locale.forLanguageTag("it-CH"), "domain.d_key_1"));

        assertNull(finder.find(Locale.forLanguageTag("en"), "domain.not-exists"));
    }

    @Test
    void test_find_defaultLanguageRegion() {
        Cache cache = TestUtilities.getCache();
        CatalogCache catalog = new CatalogCache(Locale.forLanguageTag("en-GB"), "messages", cache);

        // messages
        catalog.put(Locale.forLanguageTag("en-GB"), "messages", "m_key_1", "m_value_en_1");
        catalog.put(Locale.forLanguageTag("en-US"), "messages", "m_key_1", "m_value_en_us_1");
        catalog.put(Locale.forLanguageTag("de"), "messages", "m_key_1", "m_value_de_1");
        catalog.put(Locale.forLanguageTag("de-CH"), "messages", "m_key_1", "m_value_de_ch_1");

        // domain
        catalog.put(Locale.forLanguageTag("en-GB"), "domain", "d_key_1", "d_value_en_1");
        catalog.put(Locale.forLanguageTag("en-US"), "domain", "d_key_1", "d_value_en_us_1");
        catalog.put(Locale.forLanguageTag("de"), "domain", "d_key_1", "d_value_de_1");
        catalog.put(Locale.forLanguageTag("de-CH"), "domain", "d_key_1", "d_value_de_ch_1");

        var finder = new CatalogFinder(
            new CatalogCacheAdapter(cache),
            Locale.forLanguageTag("en-GB"),
            "messages"
        );

        // messages
        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("en-GB"), "messages.m_key_1"));
        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("en-GB"), "m_key_1"));

        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("en"), "messages.m_key_1"));
        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("en"), "m_key_1"));

        assertEquals("m_value_en_us_1", finder.find(Locale.forLanguageTag("en-US"), "messages.m_key_1"));
        assertEquals("m_value_en_us_1", finder.find(Locale.forLanguageTag("en-US"), "m_key_1"));

        assertEquals("m_value_de_1", finder.find(Locale.forLanguageTag("de"), "messages.m_key_1"));
        assertEquals("m_value_de_1", finder.find(Locale.forLanguageTag("de"), "m_key_1"));

        assertEquals("m_value_de_ch_1", finder.find(Locale.forLanguageTag("de-CH"), "messages.m_key_1"));
        assertEquals("m_value_de_ch_1", finder.find(Locale.forLanguageTag("de-CH"), "m_key_1"));

        // messages Fallback de-AT -> de
        assertEquals("m_value_de_1", finder.find(Locale.forLanguageTag("de-AT"), "messages.m_key_1"));
        assertEquals("m_value_de_1", finder.find(Locale.forLanguageTag("de-AT"), "m_key_1"));

        // messages Fallback en-GB -> en
        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("en-GB"), "messages.m_key_1"));
        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("en-GB"), "m_key_1"));

        // messages Fallback it -> en
        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("it"), "messages.m_key_1"));
        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("it"), "m_key_1"));

        // messages Fallback it-CH -> en
        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("it-CH"), "messages.m_key_1"));
        assertEquals("m_value_en_1", finder.find(Locale.forLanguageTag("it-CH"), "m_key_1"));

        // domain
        assertEquals("d_value_en_1", finder.find(Locale.forLanguageTag("en"), "domain.d_key_1"));

        assertEquals("d_value_en_us_1", finder.find(Locale.forLanguageTag("en-US"), "domain.d_key_1"));

        assertEquals("d_value_de_1", finder.find(Locale.forLanguageTag("de"), "domain.d_key_1"));

        assertEquals("d_value_de_ch_1", finder.find(Locale.forLanguageTag("de-CH"), "domain.d_key_1"));

        // domain Fallback de-AT -> de
        assertEquals("d_value_de_1", finder.find(Locale.forLanguageTag("de-AT"), "domain.d_key_1"));

        // domain Fallback en-GB -> en
        assertEquals("d_value_en_1", finder.find(Locale.forLanguageTag("en-GB"), "domain.d_key_1"));

        // domain Fallback it -> en
        assertEquals("d_value_en_1", finder.find(Locale.forLanguageTag("it"), "domain.d_key_1"));

        // domain Fallback it-CH -> en
        assertEquals("d_value_en_1", finder.find(Locale.forLanguageTag("it-CH"), "domain.d_key_1"));

        assertNull(finder.find(Locale.forLanguageTag("en"), "domain.not-exists"));
    }
}
