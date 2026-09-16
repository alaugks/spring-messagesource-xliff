// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException.FatalError;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XliffDocumentParserTest {

	private static final String VALID_XML = """
		<?xml version="1.0" encoding="utf-8"?>
		<xliff version="1.2" xmlns="urn:oasis:names:tc:xliff:document:1.2">
		</xliff>
		""";

	private static final String MALFORMED_XML = """
		<?xml version="1.0" encoding="utf-8"?>
		<xliff version="1.2" xmlns="urn:oasis:names:tc:xliff:document:1.2">
		""";

	private static final String DOCTYPE_XML = """
		<?xml version="1.0" encoding="utf-8"?>
		<!DOCTYPE xliff SYSTEM "xliff.dtd">
		<xliff version="1.2" xmlns="urn:oasis:names:tc:xliff:document:1.2">
		</xliff>
		""";

	private static Element parse(String xml) throws Exception {
		return XliffDocumentParser.parseRootElement(
			XliffDocumentParser.newDocumentBuilderFactory(),
			xml.strip().getBytes(StandardCharsets.UTF_8)
		);
	}

	@Test
	void test_parse_root_element_returns_root_element_of_document() throws Exception {
		Element root = parse(VALID_XML);

		assertThat(root).isInstanceOf(Element.class);
	}

	@Test
	void test_parse_root_element_throws_fatal_error_for_malformed_xml() {
		assertThatThrownBy(() -> parse(MALFORMED_XML))
			.isInstanceOf(XliffMessageSourceSAXParseException.class)
			.isInstanceOf(FatalError.class);
	}

	@Test
	void test_parse_root_element_throws_fatal_error_for_doctype_declaration() {
		assertThatThrownBy(() -> parse(DOCTYPE_XML))
			.isInstanceOf(XliffMessageSourceSAXParseException.class)
			.isInstanceOf(FatalError.class);
	}
}
