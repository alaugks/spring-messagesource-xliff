// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceValidationException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;

class XliffSchemaValidatorTest {

	static Stream<Arguments> supportedDocuments() {
		return Stream.of(
			Arguments.of("1.2", """
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="1.2" xmlns="urn:oasis:names:tc:xliff:document:1.2">
				    <file original="messages" datatype="plaintext" source-language="en" target-language="de">
				        <body>
				            <trans-unit id="unit-id" resname="trans-unit-resname">
				                <source>source</source>
				                <target>target</target>
				            </trans-unit>
				        </body>
				    </file>
				</xliff>
				"""),
			Arguments.of("2.0", """
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="1.2" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
				        <unit id="unit-id" name="unit-attr-name">
				            <segment id="segment-id">
				                <source><![CDATA[<span>source</span>]]></source>
				                <target><![CDATA[<span>target</span>]]></target>
				            </segment>
				        </unit>
				    </file>
				</xliff>
				"""),
			Arguments.of("2.1", """
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.1" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
				        <unit id="unit-id" name="unit-attr-name">
				            <segment id="segment-id">
				                <source><![CDATA[<span>source</span>]]></source>
				                <target><![CDATA[<span>target</span>]]></target>
				            </segment>
				        </unit>
				    </file>
				</xliff>
				""")
		);
	}

	@ParameterizedTest()
	@MethodSource("supportedDocuments")
	void rest_validate_supported_version(String version, String xml) {
		Document document = TestHelper.parseDocument(xml);
		XliffSchemaValidator validator = new XliffSchemaValidator();
		assertDoesNotThrow(() -> validator.validate(document, version));
	}

	@Test
	void rest_validate_schema_not_supported() {
		String xml = """
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.1" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
			    <file id="f1">
			        <unit id="unit-id" name="unit-attr-name">
			            <segment id="segment-id">
			                <source><![CDATA[<span>source</span>]]></source>
			                <target><![CDATA[<span>target</span>]]></target>
			            </segment>
			        </unit>
			    </file>
			</xliff>
			""";

		Document document = TestHelper.parseDocument(xml);
		XliffSchemaValidator validator = new XliffSchemaValidator();
		XliffMessageSourceValidationException exception = assertThrows(
			XliffMessageSourceValidationException.class,
			() -> validator.validate(document, "1.0")
		);
		assertEquals("No schema available for version '1.0'", exception.getMessage());
	}
}
