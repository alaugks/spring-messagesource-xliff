package io.github.alaugks.spring.messagesource.xliff.catalog.finder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.alaugks.spring.messagesource.xliff.catalog.Catalog;
import java.util.Locale;
import org.junit.jupiter.api.Test;


class CatalogFileAdapterTest {

    @Test
    void test_find() {
        Catalog catalog = new Catalog(Locale.forLanguageTag("en"), "messages");
        catalog.put(Locale.forLanguageTag("en"), "domain", "key_1", "value_en_1");
        catalog.put(Locale.forLanguageTag("en"), "domain", "key_2", "value_en_2");
        catalog.put(Locale.forLanguageTag("en"), "domain", "key_1", "value_en_3");

        var adapter = new CatalogFileAdapter(catalog.getAll());

        assertEquals("value_en_1", adapter.find(Locale.forLanguageTag("en"), "domain.key_1"));
        assertNull(adapter.find(Locale.forLanguageTag("en"), "domain.not_exists"));
    }
}
