package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.Test;
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
}
