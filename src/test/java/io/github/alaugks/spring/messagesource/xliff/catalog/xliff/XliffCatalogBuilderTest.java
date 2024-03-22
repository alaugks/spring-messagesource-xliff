package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.Catalog;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceVersionSupportException;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class XliffCatalogBuilderTest {

    @Test
    void test_createCatalog() {
        ResourcesLoader resourcesLoader = ResourcesLoader
                .builder()
                .setDefaultLocale(Locale.forLanguageTag("en"))
                .setBasenamePattern("translations/*")
                .build();

        XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder(resourcesLoader);
        CatalogInterface catalog = xliffCatalogBuilder.createCatalog(new Catalog(Locale.forLanguageTag("en"), "messages"));
        assertEquals("Hello EN (messages)", catalog.get(Locale.forLanguageTag("en"), "messages.hello_language"));
    }

    @Test
    void test_createCatalog_versionNotSupported() {
        Catalog catalog = new Catalog(Locale.forLanguageTag("en"), "messages");
        ResourcesLoader resourcesLoader = ResourcesLoader
                .builder()
                .setDefaultLocale(Locale.forLanguageTag("en-GB"))
                .setBasenamePattern("fixtures/*")
                .build();

        XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder(resourcesLoader);
        XliffMessageSourceVersionSupportException exception = assertThrows(
                XliffMessageSourceVersionSupportException.class, () -> {
                xliffCatalogBuilder.createCatalog(catalog);
            }
        );
        assertEquals("XLIFF version \"1.0\" not supported.", exception.getMessage());
    }

    @Test
    @Disabled("Todo: Handling [Fatal Error] :9:7: The element type \"body\" must be terminated by the matching end-tag \"</body>\".")
    void test_createCatalog_parseError() {
        // ErrorHandler errorHandler = new SimpleSaxErrorHandler(new NoOpLog());
        var catalog = new Catalog(Locale.forLanguageTag("en"), "messages");

        ResourcesLoader resourcesLoader = ResourcesLoader
                .builder()
                .setDefaultLocale(Locale.forLanguageTag("en"))
                .setBasenamePattern("translations_broken/*")
                .build();

        XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder(resourcesLoader);

        Throwable exception = assertThrows(
            Throwable.class, () -> {
                xliffCatalogBuilder.createCatalog(catalog);
            }
        );
        //assertTrue(exception.getMessage().indexOf("body") > 0);
        //assertEquals("The element type \"body\" must be terminated by the matching end-tag \"</body>\".", exception.getMessage());
    }

    @Test
    void test_supportedVersions() {
        var xliffCatalogBuilder = new XliffCatalogBuilder(ResourcesLoader.builder().build());
        assertInstanceOf(XliffVersion12.class, xliffCatalogBuilder.getReader("1.2"));
        assertInstanceOf(XliffVersion2.class, xliffCatalogBuilder.getReader("2.0"));
        assertInstanceOf(XliffVersion2.class, xliffCatalogBuilder.getReader("2.1"));
    }

    @Test
    void test_versionNotSupported() {
        var xliffCatalogBuilder = new XliffCatalogBuilder(ResourcesLoader.builder().build());
        assertNull(xliffCatalogBuilder.getReader("1.0"));
    }
}
