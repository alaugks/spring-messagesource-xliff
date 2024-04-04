package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.alaugks.spring.messagesource.xliff.catalog.Catalog;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogBuilder;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseFatalErrorException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceVersionSupportException;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoader;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class CatalogBuilderTest {

    @Test
    void test_createCatalog() {
        ResourcesLoader resourcesLoader = ResourcesLoader
                .builder()
                .defaultLocale(Locale.forLanguageTag("en"))
                .basenamePattern("translations/*")
                .build();

        CatalogBuilder catalogBuilder = CatalogBuilder
                .builder(resourcesLoader)
                .build();

        CatalogInterface catalog = catalogBuilder.createCatalog(new Catalog(Locale.forLanguageTag("en"), "messages"));
        assertEquals("Hello World (messages / en)", catalog.get(Locale.forLanguageTag("en"), "messages.hello_world"));
    }

    @Test
    void test_createCatalog_versionNotSupported() {
        Catalog catalog = new Catalog(Locale.forLanguageTag("en"), "messages");
        ResourcesLoader resourcesLoader = ResourcesLoader
                .builder()
                .defaultLocale(Locale.forLanguageTag("en-GB"))
                .basenamePattern("fixtures/*")
                .build();

        CatalogBuilder catalogBuilder = CatalogBuilder
                .builder(resourcesLoader)
                .build();

        XliffMessageSourceVersionSupportException exception = assertThrows(
                XliffMessageSourceVersionSupportException.class, () -> {
                    catalogBuilder.createCatalog(catalog);
                }
        );
        assertEquals("XLIFF version \"1.0\" not supported.", exception.getMessage());
    }

    @Test
    void test_createCatalog_parseError() {
        var catalog = new Catalog(Locale.forLanguageTag("en"), "messages");

        ResourcesLoader resourcesLoader = ResourcesLoader
                .builder()
                .defaultLocale(Locale.forLanguageTag("en"))
                .basenamePattern("translations_broken/*")
                .build();

        CatalogBuilder catalogBuilder = CatalogBuilder
                .builder(resourcesLoader)
                .build();

        assertThrows(
                XliffMessageSourceSAXParseException.class, () -> catalogBuilder.createCatalog(catalog)
        );

        assertThrows(
                XliffMessageSourceSAXParseFatalErrorException.class, () -> catalogBuilder.createCatalog(catalog)
        );
    }

    @Test
    void test_supportedVersions() {
        var xliffCatalog = CatalogBuilder
                .builder(
                        ResourcesLoader
                                .builder()
                                .build()
                ).build();

        assertInstanceOf(XliffVersion12.class, xliffCatalog.getReader("1.2"));
        assertInstanceOf(XliffVersion2.class, xliffCatalog.getReader("2.0"));
        assertInstanceOf(XliffVersion2.class, xliffCatalog.getReader("2.1"));
    }

    @Test
    void test_versionNotSupported() {
        var xliffCatalog = CatalogBuilder
                .builder(
                        ResourcesLoader
                                .builder()
                                .build()
                )
                .build();
        assertNull(xliffCatalog.getReader("1.0"));
    }
}
