package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S5778")
class CatalogCacheTest {
    private CatalogCache catalogCache;
    private Locale locale;

    @BeforeEach
    void beforeEach() {
        this.catalogCache = new CatalogCache(
                Locale.forLanguageTag("en"),
                "messages",
            TestUtilities.getCache()
        );
        this.locale = Locale.forLanguageTag("en");
    }

    @Test
    void test_getAll() {
        var en = Locale.forLanguageTag("en");
        var de = Locale.forLanguageTag("de");

        this.catalogCache.put(en, "messages", "m_en_1", "value_m_en_1");
        this.catalogCache.put(en, "messages", "m_en_2", "value_m_en_2");
        this.catalogCache.put(en, "domain", "d_en_1", "value_d_en_1");
        this.catalogCache.put(de, "messages", "m_de_1", "value_m_de_1");
        this.catalogCache.put(de, "messages", "m_de_2", "value_m_de_2");
        this.catalogCache.put(de, "domain", "d_de_1", "value_d_de_1");

        var all = this.catalogCache.getAll();
        var transEn = all.get(en.toString());
        var transDe = all.get(de.toString());

        assertAll(
                () -> assertEquals("value_m_en_1", transEn.get("messages.m_en_1")),
                () -> assertEquals("value_m_en_2", transEn.get("messages.m_en_2")),
                () -> assertEquals("value_d_en_1", transEn.get("domain.d_en_1")),
                () -> assertEquals("value_m_de_1", transDe.get("messages.m_de_1")),
                () -> assertEquals("value_m_de_2", transDe.get("messages.m_de_2")),
                () -> assertEquals("value_d_de_1", transDe.get("domain.d_de_1"))
        );
    }

    @Test
    void test_put_get_withDomain() {
        this.catalogCache.put(this.locale, "domain", "code", "value");
        assertEquals("value", this.catalogCache.get(this.locale, "domain.code"));
    }

    @Test
    void test_put_get() {
        Locale locale = Locale.forLanguageTag("en");
        this.catalogCache.put(this.locale, "code", "value");
        assertEquals("value", this.catalogCache.get(this.locale, "code"));
    }

    @Test
    void test_get_onNull() {
        this.catalogCache.put(this.locale, "domain", "code", "value");
        assertNull(this.catalogCache.get(this.locale, "domain.foo"));
    }

    @Test
    void test_get_onNull_localeEmpty() {
        this.catalogCache.put(this.locale, "domain", "code", "value");
        assertNull(this.catalogCache.get(Locale.forLanguageTag(""), "domain.foo"));
    }

    @Test
    void test_put_not_overwrite() {
        this.catalogCache.put(this.locale, "domain", "code", "value_1");
        this.catalogCache.put(this.locale, "domain", "code", "value_2");
        this.catalogCache.put(this.locale, "domain", "code", "value_3");
        assertEquals("value_1", this.catalogCache.get(this.locale, "domain.code"));
    }
}
