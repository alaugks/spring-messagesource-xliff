package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class XliffParserUtilityTest {
    @Test
    void test_createCode() {
        assertEquals("my-resname", XliffParserUtility.getCode(null, "my-resname", "my-target-value"));
        assertEquals("my-resname", XliffParserUtility.getCode("my-id", "my-resname", "my-target-value"));
        assertEquals("my-id", XliffParserUtility.getCode("my-id", null, "my-target-value"));
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

    private static Element getRootElement() {
        try {
            InputStream fileStream = XliffParserUtilityTest
                    .class
                    .getClassLoader()
                    .getResourceAsStream("fixtures/xliff-parser-utility-test.xliff");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(fileStream);
            return document.getDocumentElement();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            return null;
        }
    }

}
