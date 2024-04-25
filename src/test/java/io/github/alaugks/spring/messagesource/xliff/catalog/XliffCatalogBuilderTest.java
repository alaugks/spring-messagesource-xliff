package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseFatalErrorException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceVersionSupportException;
import io.github.alaugks.spring.messagesource.xliff.records.TranslationFile;
import java.util.ArrayList;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class XliffCatalogBuilderTest {

    @Test
    void test_getBaseCatalog() {
        CatalogInterface catalog = TestUtilities.getTestBaseCatalog();
        assertEquals("Hello World (messages / en)", catalog.get(Locale.forLanguageTag("en"), "messages.hello_world"));
    }

    @Test
    void test_getBaseCatalog_versionNotSupported() {
        var files = new ArrayList<TranslationFile>();
        files.add(
            new TranslationFile(
                "message",
                Locale.forLanguageTag("en"),
                getClass().getClassLoader().getResourceAsStream("fixtures/xliff10.xliff")
            )
        );

        XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder(
            files,
            "message",
            Locale.forLanguageTag("en"),
            null
        );

        XliffMessageSourceVersionSupportException exception = assertThrows(
            XliffMessageSourceVersionSupportException.class, xliffCatalogBuilder::getBaseCatalog
        );
        assertEquals("XLIFF version \"1.0\" not supported.", exception.getMessage());
    }

    @Test
    void test_getBaseCatalog_parseError() {
        var files = new ArrayList<TranslationFile>();
        files.add(
            new TranslationFile(
                "message",
                Locale.forLanguageTag("en"),
                getClass().getClassLoader().getResourceAsStream("fixtures/parse_error.xliff")
            )
        );

        XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder(
            files,
            "message",
            Locale.forLanguageTag("en"),
            null
        );

        assertThrows(
            XliffMessageSourceSAXParseException.class, xliffCatalogBuilder::getBaseCatalog
        );

        assertThrows(
            XliffMessageSourceSAXParseFatalErrorException.class, xliffCatalogBuilder::getBaseCatalog
        );
    }
}
