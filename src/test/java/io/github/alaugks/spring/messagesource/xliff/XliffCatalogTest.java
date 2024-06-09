package io.github.alaugks.spring.messagesource.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.Xliff12Identifier;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.Xliff2xIdentifier;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.XliffIdentifierInterface;
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

class XliffCatalogTest {

    @Test
    void test_getTransUnits() {
        var transUnits = new XliffCatalog(
            new HashSet<>(List.of("translations/messages.xliff", "translations/messages_de.xliff")),
            List.of("xlf", "xliff"),
            Locale.forLanguageTag("en"),
            List.of(
                new Xliff12Identifier(List.of("resname", "id")),
                new Xliff2xIdentifier(List.of("id"))
            )
        ).getTransUnits();

        assertEquals("Hello World (messages / en)", this.findInTransUnits(transUnits, "en", "hello_world"));
        assertEquals("Hallo Welt (messages / de)", this.findInTransUnits(transUnits, "de", "hello_world"));
    }

    @Test
    void test_parseError() {
        var xliffCatalogBuilder = this.getXliffCatalogBuilder(
            new HashSet<>(List.of("fixtures/parse_error.xliff")),
            Locale.forLanguageTag("en"),
            "message"
        );

        assertThrows(
            XliffMessageSourceSAXParseException.class, xliffCatalogBuilder::getTransUnits
        );

        assertThrows(
            FatalError.class, xliffCatalogBuilder::getTransUnits
        );
    }

    @Test
    void test_noXliffDocument() {
        var transUnits = new XliffCatalog(
            new HashSet<>(List.of("fixtures/no-xliff.xml")),
            List.of("xml"),
            Locale.forLanguageTag("en"),
            null
        ).getTransUnits();

        assertEquals(List.of(), transUnits);
    }

    @Test
    void test_versionNotSupported() {
        var xliffCatalogBuilder = this.getXliffCatalogBuilder(
            new HashSet<>(List.of("fixtures/xliff10.xliff")),
            Locale.forLanguageTag("en"),
            "message"
        );

        XliffMessageSourceVersionSupportException exception = assertThrows(
            XliffMessageSourceVersionSupportException.class, xliffCatalogBuilder::getTransUnits
        );
        assertEquals("XLIFF version \"1.0\" not supported.", exception.getMessage());
    }


    @ParameterizedTest
    @MethodSource("dataProvider_loadVersions")
    void test_versionSupported(String ressourcePath, String domain, String expected) {
        var transUnits = this.getXliffCatalogBuilder(
            new HashSet<>(List.of(ressourcePath)),
            Locale.forLanguageTag("en"),
            domain
        ).getTransUnits();

        assertEquals(
            expected,
            this.findInTransUnits(transUnits, "en", "code-1")
        );
    }

    private static Stream<Arguments> dataProvider_loadVersions() {
        return Stream.of(
            Arguments.of("fixtures/xliff12.xliff", "xliff12", "Hello World (Xliff Version 1.2)"),
            Arguments.of("fixtures/xliff20.xliff", "xliff20", "Hello World (Xliff Version 2.0)"),
            Arguments.of("fixtures/xliff21.xliff", "xliff21", "Hello World (Xliff Version 2.1)")
        );
    }

    private XliffCatalog getXliffCatalogBuilder(Set<String> files, Locale locale, String domain) {
        return new XliffCatalog(
            files,
            List.of("xlf", "xliff"),
            locale,
            List.of(
                new Xliff12Identifier(List.of("resname", "id")),
                new Xliff2xIdentifier(List.of("id"))
            )
        );
    }

    @ParameterizedTest()
    @MethodSource("dataProvider_setTranslationUnitIdentifiersOrdering")
    void test_setTranslationUnitIdentifiersOrdering(
        List<XliffIdentifierInterface> translationUnitIdentifiers,
        String code,
        String expected
    ) {
        var messageSource = XliffTranslationMessageSource
            .builder(
                Locale.forLanguageTag("en"),
                List.of(
                    "fixtures/identifierv1.xliff",
                    "fixtures/identifierv2.xliff"
                )
            )
            .identifier(translationUnitIdentifiers)
            .build();

        String message = messageSource.getMessage(
            code,
            null,
            Locale.forLanguageTag("en")
        );
        assertEquals(expected, message);
    }

    private static Stream<Arguments> dataProvider_setTranslationUnitIdentifiersOrdering() {
        return Stream.of(
            Arguments.of(
                List.of(
                    new Xliff12Identifier(List.of("resname"))
                ),
                "identifierv1.code-resname-a",
                "Target A"
            ),
            Arguments.of(
                List.of(
                    new Xliff12Identifier(List.of("id"))
                ),
                "identifierv1.code-id-a",
                "Target A"
            ),

            Arguments.of(
                List.of(
                    new Xliff12Identifier(List.of("resname", "id"))
                ),
                "identifierv1.code-id-b",
                "Target B"
            ),
            Arguments.of(
                List.of(
                    new Xliff12Identifier(List.of("id", "resname"))
                ),
                "identifierv1.code-resname-c",
                "Target C"
            ),

            Arguments.of(
                List.of(
                    new Xliff2xIdentifier(List.of("resname"))
                ),
                "identifierv2.code-resname-a",
                "Target A"
            ),
            Arguments.of(
                List.of(
                    new Xliff2xIdentifier(List.of("id"))
                ),
                "identifierv2.code-id-a",
                "Target A"
            ),

            Arguments.of(
                List.of(
                    new Xliff2xIdentifier(List.of("resname", "id"))
                ),
                "identifierv2.code-id-b",
                "Target B"
            ),
            Arguments.of(
                List.of(
                    new Xliff2xIdentifier(List.of("id", "resname"))
                ),
                "identifierv2.code-resname-c",
                "Target C"
            )
        );
    }

    private String findInTransUnits(List<TransUnit> transUnits, String locale, String code) {
        return transUnits
            .stream()
            .filter(t -> t.locale().toString().equals(locale) && t.code().equals(code))
            .findFirst()
            .get().value();
    }
}
