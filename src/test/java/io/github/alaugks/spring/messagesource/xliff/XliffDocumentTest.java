// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import static org.assertj.core.api.Assertions.assertThat;

class XliffDocumentTest {

	@Test
	void test_get_xliff_version_from_version_attribute() {
		var xliffDocument = new Xliff2xDocument(TestHelper.parseDocument("""
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
		var xliffDocument = new Xliff2xDocument(TestHelper.parseDocument("""
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
}
