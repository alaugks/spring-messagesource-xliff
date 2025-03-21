/*
 * Copyright 2023-2025 André Laugks <alaugks@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

		assertEquals(new HashMap<>(), xliffDocument.getTransUnitsMap("segment", List.of("id")));
		assertNull(xliffDocument.getXliffVersion());
	}

	@Test
	void test_getElementValue_getCharacterDataFromElement_TextNode()
			throws ParserConfigurationException, IOException, SAXException {

		var xliffDocument = new XliffDocument(this.getDocument("fixtures/xliff-value-test.xliff"));
		Map<Object, Object> transUnits = new HashMap<>(xliffDocument.getTransUnitsMap("segment", List.of("id")));

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
