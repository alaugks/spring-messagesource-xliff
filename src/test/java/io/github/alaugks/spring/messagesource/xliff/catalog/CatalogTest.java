package io.github.alaugks.spring.messagesource.xliff.catalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class CatalogTest {

    static Catalog catalog = new Catalog();

    @BeforeEach
    void BeforeEach() {
        // Domain foo
        catalog.put(Locale.forLanguageTag("en"), "foo", "key_1", "value_en_1");
        catalog.put(Locale.forLanguageTag("en"), "foo", "key_2", "value_en_2");
        catalog.put(Locale.forLanguageTag("en"), "foo", "key_1", "value_en_3"); // Check overwrite
        // Domain bar
        catalog.put(Locale.forLanguageTag("en"), "bar", "key_1", "value_en_1");
        catalog.put(Locale.forLanguageTag("en"), "bar", "key_2", "value_en_2");
        catalog.put(Locale.forLanguageTag("en"), "bar", "key_1", "value_en_3"); // Check overwrite
        // Domain foo
        catalog.put(Locale.forLanguageTag("en-US"), "foo", "key_1", "value_en_us_1");
        catalog.put(Locale.forLanguageTag("en_US"), "foo", "key_2", "value_en_us_2");
    }

    @Test
    void test_en() {
        // Domain foo
        Locale locale = Locale.forLanguageTag("en");
        assertEquals("value_en_1", catalog.get(locale, "foo.key_1"));
        // Domain bar
        assertEquals("value_en_1", catalog.get(locale, "bar.key_1"));
        // Domain foo
        assertEquals("value_en_2", catalog.get(locale, "foo.key_2"));
        // Domain bar
        assertEquals("value_en_2", catalog.get(locale, "bar.key_2"));

        // Domain bar
        assertNull(catalog.get(locale, "bar.key_3"));
        // Domain foo
        assertNull(catalog.get(locale, "foo.key_3"));
    }

    @Test
    void test_localeExists() {
        assertTrue(catalog.localeExists(Locale.forLanguageTag("en")));
        assertTrue(catalog.localeExists(Locale.forLanguageTag("en-US")));
        assertFalse(catalog.localeExists(Locale.forLanguageTag("en_US")));
        assertFalse(catalog.localeExists(Locale.forLanguageTag("jp")));
    }

    @Test
    void test_enUk_withDash() {
        Locale locale = Locale.forLanguageTag("en-US");
        // Domain foo
        assertEquals("value_en_us_1", catalog.get(locale, "foo.key_1"));
    }

    @Test
    void test_enUk_withUnderscore() {
        Locale locale = Locale.forLanguageTag("en_US");
        // Domain foo
        assertNull(catalog.get(locale, "foo.key_2"));
    }

    @Test
    void test_hasTranslation_true() {
        Locale locale = Locale.forLanguageTag("en");
        assertTrue(catalog.has(locale, "foo.key_1"));
    }

    @Test
    void test_hasTranslation_false() {
        Locale locale = Locale.forLanguageTag("en");
        assertFalse(catalog.has(locale, "key_3"));
    }
}
