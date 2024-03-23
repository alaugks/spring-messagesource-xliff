package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class XliffDocumentTest {

    @Test
    void test_positiv() throws ParserConfigurationException, IOException, SAXException {
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

}
