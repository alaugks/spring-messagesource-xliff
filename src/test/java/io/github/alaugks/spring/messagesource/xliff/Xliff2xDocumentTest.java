// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Xliff2xDocumentTest {

	@Test
	void test_extract_key_resolution_and_source_fallback() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.0" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
				        <unit id="unit_id_a" name="unit_name_a">
				            <segment>
				                <source>Source A</source>
				                <target>Target A</target>
				            </segment>
				        </unit>
				        <unit id="unit_id_b">
				            <segment>
				                <source>Source B</source>
				            </segment>
				        </unit>
				        <unit name="unit_name_c">
				            <segment>
				                <source>Source C</source>
				                <target>Target C</target>
				            </segment>
				        </unit>
				        <unit>
				            <segment>
				                <source>Source D</source>
				                <target>Target D</target>
				            </segment>
				        </unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertThat(units)
			// unit/@name takes precedence over unit/@id.
			.containsEntry("unit_name_a", "Target A")
			.doesNotContainKey("unit_id_a")
			// name absent, no <target> => fall back to <source>.
			.containsEntry("unit_id_b", "Source B")
			// No unit/@name => fall back to unit/@id.
			.containsEntry("unit_name_c", "Target C")
			// No unit/@name and no unit/@id => the unit is skipped.
			.doesNotContainKey("Source D");
	}

	@Test
	void test_value_text_newline_and_cdata() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.0" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
				        <unit id="element">
				            <segment>
				                <source>value</source>
				                <target>value</target>
				            </segment>
				        </unit>
				        <unit id="element-newline">
				            <segment>
				                <source>
				                    value
				                </source>
				                <target>
				                    value
				                </target>
				            </segment>
				        </unit>
				        <unit id="element-with-cdata">
				            <segment>
				                <source><![CDATA[value]]></source>
				                <target><![CDATA[value]]></target>
				            </segment>
				        </unit>
				        <unit id="element-with-cdata-newline">
				            <segment>
				                <source>
				                    <![CDATA[
				                            value
				                        ]]>
				                </source>
				                <target>
				                    <![CDATA[
				                            value
				                        ]]>
				                </target>
				            </segment>
				        </unit>
				    </file>
				</xliff>
				""", true)).getUnits();

		// Surrounding whitespace, newlines and CDATA wrappers are trimmed away.
		assertThat(units)
			.containsEntry("element", "value")
			.containsEntry("element-newline", "value")
			.containsEntry("element-with-cdata", "value")
			.containsEntry("element-with-cdata-newline", "value");
	}

	@Test
	void test_attribute_name() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.0" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
				        <unit id="unit-id" name="unit-attr-name">
				            <segment id="segment-id">
				                <source>source</source>
				                <target>target</target>
				            </segment>
				        </unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertThat(units).containsEntry("unit-attr-name", "target");
	}


	@Test
	void test_attribute_name_html() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.0" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
				        <unit id="unit-id" name="unit-attr-name">
				            <segment id="segment-id">
				                <source><![CDATA[<span>source</span>]]></source>
				                <target><![CDATA[<span>target</span>]]></target>
				            </segment>
				        </unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertThat(units).containsEntry("unit-attr-name", "<span>target</span>");
	}

	@Test
	void test_mrk_content_included_in_value() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.0" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
				        <unit id="unit-id" name="unit-attr-name">
				            <segment id="segment-id">
				                <source>Hello world!</source>
				                <target>Hallo <mrk id="m1" type="term">Welt</mrk>!</target>
				            </segment>
				        </unit>
				    </file>
				</xliff>
				""", true)).getUnits();

		// <mrk> is not processed, but its text content is part of the value.
		assertThat(units).containsEntry("unit-attr-name", "Hallo Welt!");
	}

	@Test
	void test_value_trimmed_by_default() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.0" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
				        <unit id="unit-id" name="unit-attr-name">
				            <segment id="segment-id">
				                <source>source</source>
				                <target>   spaced value   </target>
				            </segment>
				        </unit>
				    </file>
				</xliff>
				""", true)).getUnits();

		assertThat(units).containsEntry("unit-attr-name", "spaced value");
	}

	@Test
	void test_value_not_trimmed_when_xml_space_preserve() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.0" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
				        <unit id="unit-id" name="unit-attr-name">
				            <segment id="segment-id">
				                <source>source</source>
				                <target xml:space="preserve">   spaced value   </target>
				            </segment>
				        </unit>
				    </file>
				</xliff>
				""", true)).getUnits();

		assertThat(units).containsEntry("unit-attr-name", "   spaced value   ");
	}

	@Test
	void test_attribute_fallback_id() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.0" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
				        <unit id="unit-id">
				            <segment id="segment-id">
				                <source>source</source>
				                <target>target</target>
				            </segment>
				        </unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertThat(units).containsEntry("unit-id", "target");
	}

	@Test
	void test_unit_skipped_when_no_unit_id_nor_name() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.0" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
				        <unit>
				            <segment id="segment-id">
				                <source>source</source>
				                <target>target</target>
				            </segment>
				        </unit>
				    </file>
				</xliff>
				""", false)).getUnits();

		assertThat(units).isEmpty();
	}

	@Test
	void test_multiple_segments() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.0" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
						<unit id="1" name="name-value">
							<segment>
								<source>Hello</source>
								<target>Hallo</target>
							</segment>
							<ignorable>
								<source> </source>
							</ignorable>
							<segment>
								<source>World!</source>
								<target>Welt!</target>
							</segment>
						</unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertThat(units).containsEntry("name-value", "Hallo Welt!");
	}

	@Test
	void test_multiple_segments_by_order() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.0" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
						<unit id="1" name="name-value">
							<segment>
								<source>World!</source>
								<target order="2">Welt!</target>
							</segment>
							<ignorable>
								<source> </source>
							</ignorable>
							<segment>
								<source>Hello</source>
								<target order="1">Hallo</target>
							</segment>
						</unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertThat(units).containsEntry("name-value", "Hallo Welt!");
	}

	@Test
	void test_multiple_segments_by_order_and_without_order_attr() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.0" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
						<unit id="1" name="name-value">
							<segment>
								<source>I am here.</source>
								<target>Ich bin hier.</target>
							</segment>
							<ignorable>
								<source> </source>
							</ignorable>
							<segment>
								<source>World!</source>
								<target order="2">Welt!</target>
							</segment>
							<ignorable>
								<source> </source>
							</ignorable>
							<segment>
								<source>Hello</source>
								<target order="1">Hallo</target>
							</segment>
						</unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertThat(units).containsEntry("name-value", "Hallo Welt! Ich bin hier.");
	}

	@Test
	void test_multiple_segments_by_order_same_order_number() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.0" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
						<unit id="1" name="disclaimer">
							<segment>
								<source>World!</source>
								<target order="2">Welt!</target>
							</segment>
							<ignorable>
								<source> </source>
							</ignorable>
							<segment>
								<source>Hello</source>
								<target order="2">Hallo</target>
							</segment>
						</unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertThat(units).containsEntry("disclaimer", "Welt! Hallo");
	}

	@Test
	void test_segment_id_never_used_unit_skipped_when_no_unit_id_nor_name_optional() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.0" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
				        <unit id="unit-id" name="unit-attr-name">
				            <segment id="segment-id">
				                <source>source</source>
				            </segment>
				        </unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertThat(units).containsEntry("unit-attr-name", "source");
	}
}
