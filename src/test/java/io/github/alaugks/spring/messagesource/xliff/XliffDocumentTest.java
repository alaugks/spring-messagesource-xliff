// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

class XliffDocumentTest {

	@Test
	void test_getXliffVersion_fromVersionAttribute()
			throws ParserConfigurationException, IOException, SAXException {

		var xliffDocument = new Xliff2xDocument(this.getDocument("fixtures/xliff21.xliff"));

		assertEquals("2.1", xliffDocument.getXliffVersion());
	}

	@Test
	void test_getXliffVersion_nullWhenNoVersionAttribute()
			throws ParserConfigurationException, IOException, SAXException {

		var xliffDocument = new Xliff2xDocument(this.getDocument("fixtures/noversion.xliff"));

		assertNull(xliffDocument.getXliffVersion());
	}

	@Test
	void test_noXliffFile() throws ParserConfigurationException, IOException, SAXException {
		var document = this.getDocument("fixtures/no-xliff.xml");

		assertEquals(Map.of(), new Xliff12Document(document).getUnits());
		assertEquals(Map.of(), new Xliff2xDocument(document).getUnits());
		assertNull(XliffDocument.readVersion(document.getDocumentElement()));
	}

	@Test
	void test_extractXliff1() throws ParserConfigurationException, IOException, SAXException {
		Map<String, String> transUnits = new Xliff12Document(
				this.getDocument("fixtures/extract12.xliff")
		).getUnits();

		// resname takes precedence over id.
		assertEquals("Target A", transUnits.get("resname_a_key"));
		assertFalse(transUnits.containsKey("id_a"));
		// No <target> => fall back to <source>.
		assertEquals("Source B", transUnits.get("id_b"));
		// No id => fall back to resname.
		assertEquals("Target D", transUnits.get("resname_only"));
		// No resname and no id => the unit is skipped.
		assertFalse(transUnits.containsKey("Source E"));
	}

	@Test
	void test_extractXliff2() throws ParserConfigurationException, IOException, SAXException {
		Map<String, String> transUnits = new Xliff2xDocument(
				this.getDocument("fixtures/extract2x.xliff")
		).getUnits();

		// unit/@name takes precedence over unit/@id.
		assertEquals("Target A", transUnits.get("unit_name_a"));
		assertFalse(transUnits.containsKey("unit_id_a"));
		// name absent, no <target> => fall back to <source>.
		assertEquals("Source B", transUnits.get("unit_id_b"));
		// No unit/@name => fall back to unit/@id.
		assertEquals("Target C", transUnits.get("unit_name_c"));
		// No unit/@name and no unit/@id => the unit is skipped.
		assertFalse(transUnits.containsKey("Source D"));
	}

	@Test
	void test_extractXliff2_segmentation()
			throws ParserConfigurationException, IOException, SAXException {

		Map<String, String> transUnits = new Xliff2xDocument(
				this.getDocument("fixtures/multisegment.xliff")
		).getUnits();

		// Segments are reassembled in document order; the 2nd segment has no
		// <target>, so its <source> ("World.") is used as fallback.
		assertEquals("Hallo. World.", transUnits.get("greeting"));
	}

	@Test
	void test_extractValue_textNewlineAndCdata() throws ParserConfigurationException, IOException, SAXException {

		Map<String, String> transUnits = new Xliff2xDocument(
				this.getDocument("fixtures/xliff-value-test.xliff")
		).getUnits();

		assertEquals("value", transUnits.get("element"));
		assertEquals("value", transUnits.get("element-newline"));
		assertEquals("value", transUnits.get("element-with-cdata"));
		assertEquals("value", transUnits.get("element-with-cdata-newline"));
	}

	private Document getDocument(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(inputStream);
	}

	private Document getDocument(String path) throws ParserConfigurationException, SAXException, IOException {
		return this.getDocument(getClass().getClassLoader().getResourceAsStream(path));
	}
}
