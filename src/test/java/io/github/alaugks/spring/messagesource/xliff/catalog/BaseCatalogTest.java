package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.alaugks.spring.messagesource.xliff.records.Translation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BaseCatalogTest {

    static BaseCatalog baseCatalog;

    @BeforeEach
    void BeforeEach() {

        List<Translation> translations = new ArrayList<>();

        // Domain foo
        translations.add(new Translation(Locale.forLanguageTag("en"), "key_1", "value_en_1", "foo"));
        translations.add(new Translation(Locale.forLanguageTag("en"), "key_2", "value_en_2", "foo"));
        translations.add(new Translation(Locale.forLanguageTag("en"), "key_1", "value_en_3", "foo")); // Check overwrite
        // Domain bar
        translations.add(new Translation(Locale.forLanguageTag("en"), "key_1", "value_en_1", "bar"));
        translations.add(new Translation(Locale.forLanguageTag("en"), "key_2", "value_en_2", "bar"));
        translations.add(new Translation(Locale.forLanguageTag("en"), "key_1", "value_en_3", "bar")); // Check overwrite
        // Domain foo
        translations.add(new Translation(Locale.forLanguageTag("en-US"), "key_1", "value_en_us_1", "foo"));
        translations.add(new Translation(Locale.forLanguageTag("en_US"), "key_2", "value_en_us_2", "foo"));

        baseCatalog = new BaseCatalog(translations, Locale.forLanguageTag("en"), "foo").build();
    }

    @Test
    void test_fallback() {
        // Domain foo
        Locale locale = Locale.forLanguageTag("en");
        assertEquals("value_en_1", baseCatalog.get(locale, "foo.key_1"));
        assertEquals("value_en_1", baseCatalog.get(locale, "key_1"));
    }

    @Test
    void test_en() {
        // Domain foo
        Locale locale = Locale.forLanguageTag("en");
        assertEquals("value_en_1", baseCatalog.get(locale, "foo.key_1"));
        // Domain bar
        assertEquals("value_en_1", baseCatalog.get(locale, "bar.key_1"));
        // Domain foo
        assertEquals("value_en_2", baseCatalog.get(locale, "foo.key_2"));
        // Domain bar
        assertEquals("value_en_2", baseCatalog.get(locale, "bar.key_2"));

        // Domain bar
        assertNull(baseCatalog.get(locale, "bar.key_3"));
        // Domain foo
        assertNull(baseCatalog.get(locale, "foo.key_3"));
    }

    @Test
    void test_enUk_withDash() {
        Locale locale = Locale.forLanguageTag("en-US");
        // Domain foo
        assertEquals("value_en_us_1", baseCatalog.get(locale, "foo.key_1"));
    }

}
