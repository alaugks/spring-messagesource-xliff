package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.XliffTranslationMessageSource;
import io.github.alaugks.spring.messagesource.xliff.catalog.XliffVersion2.Identifier;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xml.sax.SAXException;

class XliffDocumentTest {

    @Test
    void test_getXliffVersion() throws ParserConfigurationException, IOException, SAXException {
        var xliffDocument = new XliffDocument(TestUtilities.getDocument("fixtures/xliff21.xliff"));

        assertTrue(xliffDocument.isXliffDocument());
        assertEquals("2.1", xliffDocument.getXliffVersion());
    }

    @Test
    void test_fail() throws ParserConfigurationException, IOException, SAXException {
        var xliffDocument = new XliffDocument(TestUtilities.getDocument("fixtures/xliff-fail.xliff"));

        assertFalse(xliffDocument.isXliffDocument());
        assertNull(xliffDocument.getXliffVersion());
    }

    @Test
    void test_getElementValue_getCharacterDataFromElement_TextNode()
        throws ParserConfigurationException, IOException, SAXException {
        Map<Object, Object> transUnits = new HashMap<>();

        var xliffDocument = new XliffDocument(TestUtilities.getDocument("fixtures/xliff-value-test.xliff"));
        xliffDocument.getTransUnits("segment", List.of("id")).forEach(
            transUnit -> transUnits.put(transUnit.code(), transUnit.value())
        );

        assertEquals("value", transUnits.get("element"));
        assertEquals("value", transUnits.get("element-newline"));
        assertEquals("value", transUnits.get("element-with-cdata"));
        assertEquals("value", transUnits.get("element-with-cdata-newline"));
    }

    @ParameterizedTest(name = "{index} => translationUnitIdentifiers={0}, code={1}, expected={2}, value={3}")
    @MethodSource("dataProvider_setTranslationUnitIdentifiersOrdering")
    void test_setTranslationUnitIdentifiersOrdering(
        List<XliffVersionInterface.XliffIdentifierInterface> translationUnitIdentifiers,
        String code, String expected) {
        var messageSource = XliffTranslationMessageSource
            .builder()
            .basenamesPattern(
                List.of(
                    "fixtures/identifierv1.xliff",
                    "fixtures/identifierv2.xliff"
                )
            )
            .defaultLocale(Locale.forLanguageTag("en"))
            .setTransUnitIdentifier(translationUnitIdentifiers)
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
                Arrays.asList(new Identifier(List.of("resname")),
                    new XliffVersion12.Identifier(List.of("resname"))),
                "identifierv1.code-resname-a",
                "Target A"
            ),
            Arguments.of(
                Arrays.asList(new Identifier(List.of("id")), new XliffVersion12.Identifier(List.of("id"))),
                "identifierv1.code-id-a",
                "Target A"
            ),

            Arguments.of(
                Arrays.asList(new Identifier(List.of("resname", "id")),
                    new XliffVersion12.Identifier(List.of("resname", "id"))),
                "identifierv1.code-id-b",
                "Target B"
            ),
            Arguments.of(
                Arrays.asList(new Identifier(List.of("id", "resname")),
                    new XliffVersion12.Identifier(List.of("id", "resname"))),
                "identifierv1.code-resname-c",
                "Target C"
            ),

            Arguments.of(
                Arrays.asList(new Identifier(List.of("resname")),
                    new XliffVersion12.Identifier(List.of("resname"))),
                "identifierv2.code-resname-a",
                "Target A"
            ),
            Arguments.of(
                Arrays.asList(new Identifier(List.of("id")), new XliffVersion12.Identifier(List.of("id"))),
                "identifierv2.code-id-a",
                "Target A"
            ),

            Arguments.of(
                Arrays.asList(new Identifier(List.of("resname", "id")),
                    new XliffVersion12.Identifier(List.of("resname", "id"))),
                "identifierv2.code-id-b",
                "Target B"
            ),
            Arguments.of(
                Arrays.asList(new Identifier(List.of("id", "resname")),
                    new XliffVersion12.Identifier(List.of("id", "resname"))),
                "identifierv2.code-resname-c",
                "Target C"
            )
        );
    }

}
