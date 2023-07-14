package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class XliffParserUtilityTest {

    @ParameterizedTest(name = "{index} => elementName={0}, translationUnitIdentifiers={1}, expected={2}")
    @MethodSource("getCodeProvider")
    void test_getCode(
            String elementName,
            ArrayList<String> translationUnitIdentifiers,
            String expected
    ) {
        NodeList nodeList = XliffParserUtility.getTranslationUnits(getDocument(), elementName);
        Element node = (Element) nodeList.item(0);
        assertEquals(expected, XliffParserUtility.getCode(node, translationUnitIdentifiers));
    }

    @Test
    void test_getElementValue_getCharacterDataFromElement_TextNode() {
        String value;
        value = XliffParserUtility.getElementValue(getRootElement(), "element");
        assertEquals("value", value);
        value = XliffParserUtility.getElementValue(getRootElement(), "element-newline");
        assertEquals("value", value);
        value = XliffParserUtility.getElementValue(getRootElement(), "element-with-cdata");
        assertEquals("value", value);
        value = XliffParserUtility.getElementValue(getRootElement(), "element-with-cdata-newline");
        assertEquals("value", value);
    }

    @Test
    void test_getElementValue_getCharacterDataFromElement_Node() {
        String value = XliffParserUtility.getElementValue(getRootElement(), "dummy");
        assertNull(value);
    }

    private static Document getDocument() {
        try {
            InputStream fileStream = XliffParserUtilityTest
                    .class
                    .getClassLoader()
                    .getResourceAsStream("fixtures/xliff-parser-utility-test.xliff");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(fileStream);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            return null;
        }
    }

    private static Element getRootElement() {
        return Objects.requireNonNull(getDocument()).getDocumentElement();
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
            ),
            Arguments.of(
                    "trans-unit-c",
                    new ArrayList<>(Arrays.asList("id", "resname")),
                    null
            ),
            Arguments.of(
                    "trans-unit-c",
                    new ArrayList<>(),
                    null
            ),
            Arguments.of(
                    "trans-unit-c",
                    null,
                    null
            )
        );
    }
}
