package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffCatalogBuilder;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogReaderTest {

    @Test
    void test_catalog() {
            Catalog catalog = new Catalog(Locale.forLanguageTag("en"), "messages");
            XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder(TestUtilities.getResourcesLoader());
            CatalogReader catalogReader = new CatalogReader(catalog, xliffCatalogBuilder);
            catalog = catalogReader.loadCatalog();
            assertEquals(3, catalog.getAll().size());
    }
}
