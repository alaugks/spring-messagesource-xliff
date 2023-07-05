package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.Catalog;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceVersionSupportException;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XliffCatalogBuilderTest {

    @Test
    void test_createCatalog() {
        ResourcesLoader resourcesLoader = new ResourcesLoader();
        resourcesLoader.setBasenamePattern("translations/*");
        resourcesLoader.setDefaultLocale(Locale.forLanguageTag("en"));
        XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder();
        CatalogInterface catalog = xliffCatalogBuilder.createCatalog(resourcesLoader, new Catalog());
        assertEquals("Hello EN (messages)", catalog.get(Locale.forLanguageTag("en"), "messages.hello_language"));
    }

    @Test
    void test_createCatalog_versionNotSupported() {
        Catalog catalog = new Catalog();
        ResourcesLoader resourcesLoader = new ResourcesLoader();
        resourcesLoader.setBasenamePattern("fixtures/*");
        resourcesLoader.setDefaultLocale(Locale.forLanguageTag("en-GB"));
        XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder();

        XliffMessageSourceVersionSupportException exception = assertThrows(
                XliffMessageSourceVersionSupportException.class, () -> {
                xliffCatalogBuilder.createCatalog(resourcesLoader, catalog);
            }
        );
        assertEquals("XLIFF version \"1.0\" not supported.", exception.getMessage());
    }

    @Test
    @Disabled("Todo: Handling [Fatal Error] :9:7: The element type \"body\" must be terminated by the matching end-tag \"</body>\".")
    void test_createCatalog_parseError() {
        // ErrorHandler errorHandler = new SimpleSaxErrorHandler(new NoOpLog());
        var catalog = new Catalog();
        ResourcesLoader resourcesLoader = new ResourcesLoader();
        resourcesLoader.setBasenamePattern("translations_broken/*");
        resourcesLoader.setDefaultLocale(Locale.forLanguageTag("en"));
        XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder();

        Throwable exception = assertThrows(
            Throwable.class, () -> {
                xliffCatalogBuilder.createCatalog(resourcesLoader, catalog);
            }
        );
        //assertTrue(exception.getMessage().indexOf("body") > 0);
        //assertEquals("The element type \"body\" must be terminated by the matching end-tag \"</body>\".", exception.getMessage());
    }
}
