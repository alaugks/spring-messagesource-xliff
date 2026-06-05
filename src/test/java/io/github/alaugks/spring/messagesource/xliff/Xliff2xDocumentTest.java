// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class Xliff2xDocumentTest {

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
	void test_unitSkipped_whenNoUnitIdNorName() {
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
						<unit id="1" name="disclaimer">
							<segment>
								<source>segment_a_source</source>
								<target>segment_a_target</target>
							</segment>
							<ignorable>
								<source> </source>
							</ignorable>
							<segment>
								<source>segment_b_source</source>
								<target>segment_b_target</target>
							</segment>
						</unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertEquals("segment_a_target segment_b_target", units.get("disclaimer"));
	}

	@Test
	void test_segmentIdNeverUsed_unitSkipped_whenNoUnitIdNorName_Optional() {
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
