package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException.FatalError;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceVersionSupportException;
import io.github.alaugks.spring.messagesource.xliff.records.TranslationFile;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class XliffCatalogBuilderTest {

    @Test
    void test_getBaseCatalog() {

        var files = new ArrayList<TranslationFile>();
        files.add(
            new TranslationFile(
                "messages",
                Locale.forLanguageTag("en"),
                getClass().getClassLoader().getResourceAsStream("translations/messages.xliff")
            )
        );

        files.add(
            new TranslationFile(
                "messages",
                Locale.forLanguageTag("de"),
                getClass().getClassLoader().getResourceAsStream("translations/messages_de.xliff")
            )
        );

        var catalog = new XliffCatalogBuilder(
            files,
            "messages",
            Locale.forLanguageTag("en")
        ).getBaseCatalog();

        assertEquals("Hello World (messages / en)", catalog.get(Locale.forLanguageTag("en"), "messages.hello_world"));
        assertEquals("Hallo Welt (messages / de)", catalog.get(Locale.forLanguageTag("de"), "messages.hello_world"));
    }

    @Test
    void test_parseError() {
        var xliffCatalogBuilder = this.getXliffCatalogBuilder(
            "fixtures/parse_error.xliff",
            Locale.forLanguageTag("en"),
            "message"
        );

        assertThrows(
            XliffMessageSourceSAXParseException.class, xliffCatalogBuilder::getBaseCatalog
        );

        assertThrows(
            FatalError.class, xliffCatalogBuilder::getBaseCatalog
        );
    }

    @Test
    void test_versionNotSupported() {
        var xliffCatalogBuilder = this.getXliffCatalogBuilder(
            "fixtures/xliff10.xliff",
            Locale.forLanguageTag("en"),
            "message"
        );

        XliffMessageSourceVersionSupportException exception = assertThrows(
            XliffMessageSourceVersionSupportException.class, xliffCatalogBuilder::getBaseCatalog
        );
        assertEquals("XLIFF version \"1.0\" not supported.", exception.getMessage());
    }


    @ParameterizedTest
    @MethodSource("dataProvider_loadVersions")
    void test_versionSupported(String ressourcePath, String expected) {
        var baseCatalog = this.getXliffCatalogBuilder(
            ressourcePath,
            Locale.forLanguageTag("en"),
            "domain"
        );

        assertEquals(
            expected,
            baseCatalog.getBaseCatalog().get(Locale.forLanguageTag("en"), "domain.code-1")
        );
    }

    private static Stream<Arguments> dataProvider_loadVersions() {
        return Stream.of(
            Arguments.of("fixtures/xliff12.xliff", "Hello World (Xliff Version 1.2)"),
            Arguments.of("fixtures/xliff20.xliff", "Hello World (Xliff Version 2.0)"),
            Arguments.of("fixtures/xliff21.xliff", "Hello World (Xliff Version 2.1)")
        );
    }

    private XliffCatalogBuilder getXliffCatalogBuilder(String ressource, Locale locale, String domain) {
        var files = new ArrayList<TranslationFile>();
        files.add(
            new TranslationFile(
                domain,
                locale,
                getClass().getClassLoader().getResourceAsStream(ressource)
            )
        );

        return new XliffCatalogBuilder(
            files,
            domain,
            locale
        );
    }

}
