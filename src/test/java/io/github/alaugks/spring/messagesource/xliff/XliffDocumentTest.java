// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class XliffDocumentTest {

	@Test
	void test_get_xliff_version_from_version_attribute() {
		Xliff2xDocument xliffDocument = new Xliff2xDocument(TestHelper.parseDocument("""
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.1" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
			    <file id="f1">
			        <unit id="unit-id">
			            <segment>
			                <source>source</source>
			                <target>target</target>
			            </segment>
			        </unit>
			    </file>
			</xliff>
			"""));

		assertThat(xliffDocument.getXliffVersion()).isEqualTo("2.1");
	}

	@Test
	void test_get_xliff_version_null_when_no_version_attribute() {
		Xliff2xDocument xliffDocument = new Xliff2xDocument(TestHelper.parseDocument("""
			<?xml version="1.0" encoding="utf-8"?>
			<xliff srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
			    <file id="f1">
			        <unit id="unit-id">
			            <segment>
			                <source>source</source>
			                <target>target</target>
			            </segment>
			        </unit>
			    </file>
			</xliff>
			"""));

		assertThat(xliffDocument.getXliffVersion()).isNull();
	}

	@Test
	void test_no_xliff_file() {
		Document document = TestHelper.parseDocument("""
			<?xml version="1.0" encoding="utf-8"?>
			<translations></translations>
			""");

		assertThat(new Xliff12Document(document).getUnits()).isEqualTo(Map.of());
		assertThat(new Xliff2xDocument(document).getUnits()).isEqualTo(Map.of());
		assertThat(XliffDocument.readVersion(document.getDocumentElement())).isNull();
	}

	@Test
	void test_read_version_uses_node_name_when_local_name_is_absent() throws Exception {
		Document document = DocumentBuilderFactory.newInstance()
			.newDocumentBuilder()
			.parse(new ByteArrayInputStream(
				"<xliff version=\"1.2\"></xliff>".getBytes(StandardCharsets.UTF_8)
			));

		assertThat(XliffDocument.readVersion(document.getDocumentElement())).isEqualTo("1.2");
	}

	@Test
	void test_element_name_uses_node_name_when_local_name_is_absent() throws Exception {
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element element = document.createElement("segment");

		assertThat(XliffDocument.elementName(element)).isEqualTo("segment");
	}

	@Test
	void test_first_non_empty_skips_null_and_empty_values() {
		Xliff2xDocument xliffDocument = new Xliff2xDocument(TestHelper.parseDocument("""
			<?xml version="1.0" encoding="utf-8"?>
			<xliff xmlns="urn:oasis:names:tc:xliff:document:2.0"></xliff>
			"""));

		assertThat(xliffDocument.firstNonEmpty(null, "", "value")).isEqualTo("value");
	}

	@Test
	void test_value_is_empty_when_element_is_null() {
		Xliff2xDocument xliffDocument = new Xliff2xDocument(TestHelper.parseDocument("""
			<?xml version="1.0" encoding="utf-8"?>
			<xliff xmlns="urn:oasis:names:tc:xliff:document:2.0"></xliff>
			"""));

		assertThat(xliffDocument.value(null)).isEmpty();
	}

	@Test
	void test_raw_value_is_empty_when_text_content_is_null() {
		Xliff2xDocument xliffDocument = new Xliff2xDocument(TestHelper.parseDocument("""
			<?xml version="1.0" encoding="utf-8"?>
			<xliff xmlns="urn:oasis:names:tc:xliff:document:2.0"></xliff>
			"""));
		Element element = mock(Element.class);
		when(element.getTextContent()).thenReturn(null);

		assertThat(xliffDocument.rawValue(element)).isEmpty();
	}
}
