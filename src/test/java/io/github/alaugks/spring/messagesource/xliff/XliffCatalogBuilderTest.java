package io.github.alaugks.spring.messagesource.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException.FatalError;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceVersionSupportException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class XliffCatalogBuilderTest {

    @Test
    void test_getBaseCatalog() {
        var catalog = new XliffCatalogBuilder(
            new HashSet<>(List.of("translations/messages.xliff", "translations/messages_de.xliff")),
            List.of("xlf", "xliff"),
            "messages",
            Locale.forLanguageTag("en")
        ).getBaseCatalog();
        catalog.build();

        assertEquals("Hello World (messages / en)", catalog.get(Locale.forLanguageTag("en"), "messages.hello_world"));
        assertEquals("Hallo Welt (messages / de)", catalog.get(Locale.forLanguageTag("de"), "messages.hello_world"));
    }

    @Test
    void test_parseError() {
        var xliffCatalogBuilder = this.getXliffCatalogBuilder(
            new HashSet<>(List.of("fixtures/parse_error.xliff")),
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
            new HashSet<>(List.of("fixtures/xliff10.xliff")),
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
    void test_versionSupported(String ressourcePath, String domain, String expected) {
        var xliffCatalogBuilder = this.getXliffCatalogBuilder(
            new HashSet<>(List.of(ressourcePath)),
            Locale.forLanguageTag("en"),
            domain
        );
        var catalog = xliffCatalogBuilder.getBaseCatalog();
        catalog.build();

        assertEquals(
            expected,
            catalog.get(Locale.forLanguageTag("en"), domain + ".code-1")
        );
    }

    private static Stream<Arguments> dataProvider_loadVersions() {
        return Stream.of(
            Arguments.of("fixtures/xliff12.xliff", "xliff12", "Hello World (Xliff Version 1.2)"),
            Arguments.of("fixtures/xliff20.xliff", "xliff20", "Hello World (Xliff Version 2.0)"),
            Arguments.of("fixtures/xliff21.xliff", "xliff21", "Hello World (Xliff Version 2.1)")
        );
    }

    private XliffCatalogBuilder getXliffCatalogBuilder(Set<String> files, Locale locale, String domain) {
        return new XliffCatalogBuilder(
            files,
            List.of("xlf", "xliff"),
            domain,
            locale
        );
    }
}
