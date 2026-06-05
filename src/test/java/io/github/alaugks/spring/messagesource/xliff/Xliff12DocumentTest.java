// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class Xliff12DocumentTest {

	@Test
	void xliff12_keyFromResname_andResnameWinsOverId() {
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

		assertEquals("target", units.get("trans-unit-resname"));
		assertFalse(units.containsKey("unit-id"));
	}

	@Test
	void xliff12_fallbackToId_whenResnameAbsent() {
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

		assertEquals("target", units.get("unit-id"));
	}

	@Test
	void xliff12_valueTrimmed_byDefault() {
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

		assertEquals("spaced value", units.get("trans-unit-resname"));
	}

	@Test
	void xliff12_valueNotTrimmed_whenXmlSpacePreserve() {
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

		assertEquals("   spaced value   ", units.get("trans-unit-resname"));
	}

	@Test
	void xliff12_mrkContentIsIncludedInValue() {
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
		assertEquals("Hallo Welt!", units.get("unit-id"));
	}

	@Test
	void xliff12_skipped_whenNeitherIdNorResname() {
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

		assertTrue(units.isEmpty());
	}
}
