package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffCatalogBuilder;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class CatalogReaderTest {

    @Test
    void test_catalog() {
            Catalog catalog = new Catalog(Locale.forLanguageTag("en"), "messages");

            XliffCatalogBuilder xliffCatalogBuilder = XliffCatalogBuilder
                .builder(TestUtilities.getResourcesLoader())
                .build();

            CatalogReader catalogReader = new CatalogReader(catalog, xliffCatalogBuilder);
            catalog = catalogReader.loadCatalog();
            assertEquals(3, catalog.getAll().size());
    }
}
