package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class XliffReaderTest {

    @Test
    void test_getElementValue_getCharacterDataFromElement_TextNode() throws ParserConfigurationException, IOException, SAXException {
        Map<Object, Object> transUnits = new HashMap<>();

        Document document = TestUtilities.getDocument("fixtures/xliff-value-test.xliff");
        var xliffDocument = new XliffDocument(document);
        xliffDocument.getTransUnits("segment", List.of("id")).forEach(
                transUnit -> transUnits.put(transUnit.getCode(), transUnit.getTargetValue())
        );

        assertEquals("value", transUnits.get("element"));
        assertEquals("value", transUnits.get("element-newline"));
        assertEquals("value", transUnits.get("element-with-cdata"));
        assertEquals("value", transUnits.get("element-with-cdata-newline"));
    }


    @ParameterizedTest(name = "{index} => elementName={0}, translationUnitIdentifiers={1}, expected={2}")
    @MethodSource("getCodeProvider")
    void test_getCode(
            String expected,
            ArrayList<String> translationUnitIdentifiers,
            String code
    ) throws ParserConfigurationException, IOException, SAXException {
        Map<Object, Object> transUnits = new HashMap<>();
        Document document = TestUtilities.getDocument("fixtures/xliff-code-test.xliff");

        var xliffDocument = new XliffDocument(document);
        xliffDocument.getTransUnits("segment", translationUnitIdentifiers).forEach(
                transUnit -> transUnits.put(transUnit.getCode(), transUnit.getTargetValue())
        );

        assertEquals(expected, transUnits.get(code));
    }

    private static Stream<Arguments> getCodeProvider() {
        return Stream.of(
            Arguments.of(
                    "trans-unit-a",
                    new ArrayList<>(Arrays.asList("resname", "id")),
                    "resname-a"
            ),
            Arguments.of(
                    "trans-unit-a",
                    new ArrayList<>(Arrays.asList("id", "resname")),
                    "id-a"
            ),
            Arguments.of(
                    "trans-unit-b",
                    new ArrayList<>(Arrays.asList("resname", "id")),
                    "id-b"
            ),
            Arguments.of(
                    "trans-unit-b",
                    new ArrayList<>(Arrays.asList("id", "resname")),
                    "id-b"
            )
        );
    }
}
