package io.github.alaugks.spring.messagesource.xliff;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class XliffDocumentTest {

	@Test
	void test_getXliffVersion() throws ParserConfigurationException, IOException, SAXException {
		var xliffDocument = new XliffDocument(this.getDocument("fixtures/xliff21.xliff"));

		assertEquals("2.1", xliffDocument.getXliffVersion());
	}

	@Test
	void test_noXliffFile() throws ParserConfigurationException, IOException, SAXException {
		var xliffDocument = new XliffDocument(this.getDocument("fixtures/no-xliff.xml"));

		assertEquals(new HashMap<>(), xliffDocument.getTransUnits("segment", List.of("id")));
		assertNull(xliffDocument.getXliffVersion());
	}

	@Test
	void test_getElementValue_getCharacterDataFromElement_TextNode()
			throws ParserConfigurationException, IOException, SAXException {

		var xliffDocument = new XliffDocument(this.getDocument("fixtures/xliff-value-test.xliff"));
		Map<Object, Object> transUnits = new HashMap<>(xliffDocument.getTransUnits("segment", List.of("id")));

		assertEquals("value", transUnits.get("element"));
		assertEquals("value", transUnits.get("element-newline"));
		assertEquals("value", transUnits.get("element-with-cdata"));
		assertEquals("value", transUnits.get("element-with-cdata-newline"));
	}

	private Document getDocument(InputStream inputStream)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(inputStream);
	}

	private Document getDocument(String path) throws ParserConfigurationException, SAXException, IOException {
		return this.getDocument(getClass().getClassLoader().getResourceAsStream(path));
	}
}
