package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import java.util.HashMap;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S5778")
class CacheCatalogTest {

    private CacheCatalog cacheCatalog;

    @BeforeEach
    void beforeEach() {

        var cache = TestUtilities.getCache();
        cache.put("en|messages.m_en_1", "value_m_en_1");
        cache.put("en|messages.m_en_2", "value_m_en_2");
        cache.put("en|domain.d_en_1", "value_d_en_1");
        cache.put("de|messages.m_de_1", "value_m_de_1");
        cache.put("de|messages.m_de_2", "value_m_de_2");
        cache.put("de|domain.d_de_1", "value_d_de_1");

        this.cacheCatalog = new CacheCatalog(cache);
    }

    @Test
    void test_getAll() {
        var en = Locale.forLanguageTag("en");
        var de = Locale.forLanguageTag("de");

        var all = this.cacheCatalog.getAll();
        var transEn = all.get(en.toString());
        var transDe = all.get(de.toString());

        assertEquals("value_m_en_1", transEn.get("messages.m_en_1"));
        assertEquals("value_m_en_2", transEn.get("messages.m_en_2"));
        assertEquals("value_d_en_1", transEn.get("domain.d_en_1"));
        assertEquals("value_m_de_1", transDe.get("messages.m_de_1"));
        assertEquals("value_m_de_2", transDe.get("messages.m_de_2"));
        assertEquals("value_d_de_1", transDe.get("domain.d_de_1"));
    }

    @Test
    void test_get() {
        assertEquals("value_m_en_1", cacheCatalog.get(Locale.forLanguageTag("en"), "messages.m_en_1"));
    }

    @Test
    void test_get_netExists() {
        assertNull(cacheCatalog.get(Locale.forLanguageTag("en"), "messages.not_exists"));
    }

    @Test
    void test_get_paramValuesEmpty() {
        assertNull(cacheCatalog.get(Locale.forLanguageTag("en"), ""));
        assertNull(cacheCatalog.get(Locale.forLanguageTag(""), "messages.m_en_1"));
    }

    @Test
    void test_cache_isNull() {
        this.cacheCatalog = new CacheCatalog(null);
        assertInstanceOf(HashMap.class, this.cacheCatalog.getAll());
    }
}
