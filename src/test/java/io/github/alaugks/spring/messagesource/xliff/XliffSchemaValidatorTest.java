// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceValidationException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;

class XliffSchemaValidatorTest {

	@ParameterizedTest()
	@MethodSource("provider_supported_documents")
	void test_validate_supported_version(String version, String xml) {
		Document document = TestHelper.parseDocument(xml);
		XliffSchemaValidator validator = new XliffSchemaValidator();
		assertThatCode(() -> validator.validate(document, version)).doesNotThrowAnyException();
	}

	static Stream<Arguments> provider_supported_documents() {
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
				"""),
			Arguments.of("2.2", """
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.2" srcLang="en" trgLang="de"
				       xmlns="urn:oasis:names:tc:xliff:document:2.2">
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

	@Test
	void test_validate_2_2_with_pgs_module_is_valid() {
		String xml = """
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.2" srcLang="en" trgLang="de"
			    xmlns="urn:oasis:names:tc:xliff:document:2.2"
			    xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
			    <file id="f1">
			        <unit id="tu1" name="file_deleted" pgs:switch="plural:count">
			            <segment pgs:case="0">
			                <source>You deleted no plural.</source>
			                <target>Sie haben keine Dateien gelöscht.</target>
			            </segment>
			            <segment pgs:case="other">
			                <source>You deleted <ph id="1" disp="count"/> plural.</source>
			                <target>Sie haben <ph id="1" disp="count"/> Dateien gelöscht.</target>
			            </segment>
			        </unit>
			    </file>
			</xliff>
			""";

		Document document = TestHelper.parseDocument(xml);
		XliffSchemaValidator validator = new XliffSchemaValidator();
		assertThatCode(() -> validator.validate(document, "2.2")).doesNotThrowAnyException();
	}

	@Test
	void test_validate_2_2_rejects_unknown_core_attribute_on_segment() {
		String xml = """
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.2" srcLang="en" trgLang="de"
			    xmlns="urn:oasis:names:tc:xliff:document:2.2">
			    <file id="f1">
			        <unit id="tu1" name="file_deleted">
			            <segment case="0">
			                <source>You deleted no plural.</source>
			                <target>Sie haben keine Dateien gelöscht.</target>
			            </segment>
			        </unit>
			    </file>
			</xliff>
			""";

		Document document = TestHelper.parseDocument(xml);
		XliffSchemaValidator validator = new XliffSchemaValidator();
		assertThatThrownBy(() -> validator.validate(document, "2.2"))
			.isInstanceOf(XliffMessageSourceValidationException.class);
	}

	@Test
	void test_validate_schema_not_supported() {
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
		assertThatThrownBy(() -> validator.validate(document, "1.0"))
			.isInstanceOf(XliffMessageSourceValidationException.class)
			.hasMessage("No schema available for version \"1.0\"");
	}
}
