// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

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

		// unit/@name takes precedence over unit/@id.
		assertEquals("Target A", units.get("unit_name_a"));
		assertFalse(units.containsKey("unit_id_a"));
		// name absent, no <target> => fall back to <source>.
		assertEquals("Source B", units.get("unit_id_b"));
		// No unit/@name => fall back to unit/@id.
		assertEquals("Target C", units.get("unit_name_c"));
		// No unit/@name and no unit/@id => the unit is skipped.
		assertFalse(units.containsKey("Source D"));
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
		assertEquals("value", units.get("element"));
		assertEquals("value", units.get("element-newline"));
		assertEquals("value", units.get("element-with-cdata"));
		assertEquals("value", units.get("element-with-cdata-newline"));
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

		assertEquals("target", units.get("unit-attr-name"));
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

		assertEquals("<span>target</span>", units.get("unit-attr-name"));
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
		assertEquals("Hallo Welt!", units.get("unit-attr-name"));
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

		assertEquals("spaced value", units.get("unit-attr-name"));
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

		assertEquals("   spaced value   ", units.get("unit-attr-name"));
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

		assertEquals("target", units.get("unit-id"));
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

		assertTrue(units.isEmpty());
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

		assertEquals("Hallo Welt!", units.get("name-value"));
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

		assertEquals("Hallo Welt!", units.get("name-value"));
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

		assertEquals("Hallo Welt! Ich bin hier.", units.get("name-value"));
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

		assertEquals("Welt! Hallo", units.get("disclaimer"));
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

		assertEquals("source", units.get("unit-attr-name"));
	}
}
