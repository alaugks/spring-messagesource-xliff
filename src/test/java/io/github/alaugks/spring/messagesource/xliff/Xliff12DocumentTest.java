// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Xliff12DocumentTest {

	@Test
	void test_extract_key_resolution_and_source_fallback() {
		Map<String, String> units = new Xliff12Document(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="1.2" xmlns="urn:oasis:names:tc:xliff:document:1.2">
				    <file original="global" datatype="plaintext" source-language="en" target-language="de">
				        <body>
				            <trans-unit id="id_a" resname="resname_a_key">
				                <source>Source A</source>
				                <target>Target A</target>
				            </trans-unit>
				            <trans-unit id="id_b">
				                <source>Source B</source>
				            </trans-unit>
				            <trans-unit resname="resname_only">
				                <source>Source D</source>
				                <target>Target D</target>
				            </trans-unit>
				            <trans-unit>
				                <source>Source E</source>
				                <target>Target E</target>
				            </trans-unit>
				        </body>
				    </file>
				</xliff>
				""")).getUnits();

		assertThat(units)
			// resname takes precedence over id.
			.containsEntry("resname_a_key", "Target A")
			.doesNotContainKey("id_a")
			// No <target> => fall back to <source>.
			.containsEntry("id_b", "Source B")
			// No id => fall back to resname.
			.containsEntry("resname_only", "Target D")
			// No resname and no id => the unit is skipped.
			.doesNotContainKey("Source E");
	}

	@Test
	void test_key_from_resname_and_resname_wins_over_id() {
		Map<String, String> units = new Xliff12Document(TestHelper.parseDocument("""
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
				""", true)).getUnits();

		assertThat(units)
			.containsEntry("trans-unit-resname", "target")
			.doesNotContainKey("unit-id");
	}

	@Test
	void test_fallback_to_id_when_resname_absent() {
		Map<String, String> units = new Xliff12Document(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="1.2" xmlns="urn:oasis:names:tc:xliff:document:1.2">
				    <file original="messages" datatype="plaintext" source-language="en" target-language="de">
				        <body>
				            <trans-unit id="unit-id">
				                <source>source</source>
				                <target>target</target>
				            </trans-unit>
				        </body>
				    </file>
				</xliff>
				""", true)).getUnits();

		assertThat(units).containsEntry("unit-id", "target");
	}

	@Test
	void test_value_trimmed_by_default() {
		Map<String, String> units = new Xliff12Document(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="1.2" xmlns="urn:oasis:names:tc:xliff:document:1.2">
				    <file original="messages" datatype="plaintext" source-language="en" target-language="de">
				        <body>
				            <trans-unit id="unit-id" resname="trans-unit-resname">
				                <source>source</source>
				                <target>   spaced value   </target>
				            </trans-unit>
				        </body>
				    </file>
				</xliff>
				""", true)).getUnits();

		assertThat(units).containsEntry("trans-unit-resname", "spaced value");
	}

	@Test
	void test_value_not_trimmed_when_xml_space_preserve() {
		Map<String, String> units = new Xliff12Document(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="1.2" xmlns="urn:oasis:names:tc:xliff:document:1.2">
				    <file original="messages" datatype="plaintext" source-language="en" target-language="de">
				        <body>
				            <trans-unit id="unit-id" resname="trans-unit-resname">
				                <source>source</source>
				                <target xml:space="preserve">   spaced value   </target>
				            </trans-unit>
				        </body>
				    </file>
				</xliff>
				""", true)).getUnits();

		assertThat(units).containsEntry("trans-unit-resname", "   spaced value   ");
	}

	@Test
	void test_mrk_content_is_included_in_value() {
		Map<String, String> units = new Xliff12Document(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="1.2" xmlns="urn:oasis:names:tc:xliff:document:1.2">
				    <file original="messages" datatype="plaintext" source-language="en" target-language="de">
				        <body>
				            <trans-unit id="unit-id">
				                <source>Hello world!</source>
				                <target>Hallo <mrk mtype="term">Welt</mrk>!</target>
				            </trans-unit>
				        </body>
				    </file>
				</xliff>
				""", true)).getUnits();

		// <mrk> is not processed, but its text content is part of the value.
		assertThat(units).containsEntry("unit-id", "Hallo Welt!");
	}

	@Test
	void test_skipped_when_neither_id_nor_resname() {
		Map<String, String> units = new Xliff12Document(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="1.2" xmlns="urn:oasis:names:tc:xliff:document:1.2">
				    <file original="messages" datatype="plaintext" source-language="en" target-language="de">
				        <body>
				            <trans-unit>
				                <source>source</source>
				                <target>target</target>
				            </trans-unit>
				        </body>
				    </file>
				</xliff>
				""")).getUnits();

		assertThat(units).isEmpty();
	}
}
