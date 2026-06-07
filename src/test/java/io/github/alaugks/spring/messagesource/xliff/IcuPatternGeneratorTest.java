// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.alaugks.spring.messagesource.catalog.CatalogMessageSourceBuilder;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IcuPatternGeneratorTest {

	@Test
	void test_plural_to_icu_pattern() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.2" srcLang="en" trgLang="de"
				       xmlns="urn:oasis:names:tc:xliff:document:2.0"
				       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
				    <file id="f1">
				        <unit id="tu1" name="file_deleted" pgs:switch="plural:file_count">
				            <segment pgs:case="0">
				                <source>You deleted no files.</source>
				                <target>Sie haben keine Dateien gelöscht.</target>
				            </segment>
				            <segment pgs:case="1">
				                <source>You deleted one file.</source>
				                <target>Sie haben eine Datei gelöscht.</target>
				            </segment>
				            <segment pgs:case="other">
				                <source>You deleted <ph id="1" disp="file_count"/> files.</source>
				                <target>Sie haben <ph id="1" disp="file_count"/> Dateien gelöscht.</target>
				            </segment>
				        </unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertEquals(
			"{file_count, plural, =0 {Sie haben keine Dateien gelöscht.}"
				+ " =1 {Sie haben eine Datei gelöscht.}"
				+ " other {Sie haben {file_count} Dateien gelöscht.}}",
			units.get("file_deleted")
		);


		List<TransUnitInterface> transUnits = new ArrayList<>();
		transUnits.add(new TransUnit(Locale.GERMAN, "file_deleted", units.get("file_deleted")));

		CatalogMessageSourceBuilder messageSource = CatalogMessageSourceBuilder
			.builder(transUnits, Locale.GERMAN)
			.build();

		assertEquals(
			"Sie haben 5 Dateien gelöscht.",
			messageSource.getMessage("file_deleted", new Object[]{5}, Locale.GERMAN)
		);
	}

	// Arabic (ar) is one of the languages that actually use all six CLDR plural
	// categories; the target text is the category name purely to keep the assertion exact.
	@Test
	void test_plural_all_cldr_keywords_kept_verbatim() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.2" srcLang="en" trgLang="ar"
				       xmlns="urn:oasis:names:tc:xliff:document:2.0"
				       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
				    <file id="f1">
				        <unit id="tu1" name="count" pgs:switch="plural:count">
				            <segment pgs:case="zero"><target>zero</target></segment>
				            <segment pgs:case="one"><target>one</target></segment>
				            <segment pgs:case="two"><target>two</target></segment>
				            <segment pgs:case="few"><target>few</target></segment>
				            <segment pgs:case="many"><target>many</target></segment>
				            <segment pgs:case="other"><target>other</target></segment>
				        </unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertEquals(
			"{count, plural, zero {zero} one {one} two {two}"
				+ " few {few} many {many} other {other}}",
			units.get("count")
		);
	}

	@Test
	void test_plural_numeric_cases_become_exact_matches() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.2" srcLang="en" trgLang="de"
				       xmlns="urn:oasis:names:tc:xliff:document:2.0"
				       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
				    <file id="f1">
				        <unit id="tu1" name="count" pgs:switch="plural:count">
				            <segment pgs:case="0"><target>null</target></segment>
				            <segment pgs:case="2"><target>zwei</target></segment>
				            <segment pgs:case="5"><target>fünf</target></segment>
				            <segment pgs:case="other"><target>viele</target></segment>
				        </unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertEquals(
			"{count, plural, =0 {null} =2 {zwei} =5 {fünf} other {viele}}",
			units.get("count")
		);
	}

	@Test
	void test_segment_without_case_defaults_to_other() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.2" srcLang="en" trgLang="de"
				       xmlns="urn:oasis:names:tc:xliff:document:2.0"
				       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
				    <file id="f1">
				        <unit id="tu1" name="count" pgs:switch="plural:count">
				            <segment pgs:case="one"><target>eine</target></segment>
				            <segment><target>andere</target></segment>
				        </unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertEquals(
			"{count, plural, one {eine} other {andere}}",
			units.get("count")
		);
	}

	@Test
	void test_gender_to_icu_select_pattern() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.2" srcLang="en" trgLang="de"
				       xmlns="urn:oasis:names:tc:xliff:document:2.0"
				       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
				    <file id="f1">
				        <unit id="tu1" name="greeting" pgs:switch="gender:recipient_gender">
				            <segment pgs:case="feminine">
				                <source>How is she?</source>
				                <target>Wie geht's ihr?</target>
				            </segment>
				            <segment pgs:case="masculine">
				                <source>How is he?</source>
				                <target>Wie geht's ihm?</target>
				            </segment>
				            <segment pgs:case="other">
				                <source>How are they?</source>
				                <target>Wie geht's ihnen?</target>
				            </segment>
				        </unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertEquals(
			"{recipient_gender, select, feminine {Wie geht''s ihr?}"
				+ " masculine {Wie geht''s ihm?}"
				+ " other {Wie geht''s ihnen?}}",
			units.get("greeting")
		);
	}

	@Test
	void test_nested_switches_gender_then_plural() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.2" srcLang="en" trgLang="de"
				       xmlns="urn:oasis:names:tc:xliff:document:2.0"
				       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
				    <file id="f1">
				        <unit id="tu1" name="invites" pgs:switch="gender:user_gender plural:count">
				            <segment pgs:case="feminine one"><target>Sie hat eine</target></segment>
				            <segment pgs:case="feminine other"><target>Sie hat viele</target></segment>
				            <segment pgs:case="masculine one"><target>Er hat eine</target></segment>
				            <segment pgs:case="masculine other"><target>Er hat viele</target></segment>
				            <segment pgs:case="other"><target>Sie haben viele</target></segment>
				        </unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertEquals(
			"{user_gender, select,"
				+ " feminine {{count, plural, one {Sie hat eine} other {Sie hat viele}}}"
				+ " masculine {{count, plural, one {Er hat eine} other {Er hat viele}}}"
				+ " other {{count, plural, other {Sie haben viele}}}}",
			units.get("invites")
		);
	}

	@Test
	void test_escapes_icu_metacharacters_in_text() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.2" srcLang="en" trgLang="de"
				       xmlns="urn:oasis:names:tc:xliff:document:2.0"
				       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
				    <file id="f1">
				        <unit id="tu1" name="discount" pgs:switch="plural:count">
				            <segment pgs:case="other"><target>50% {Rabatt}</target></segment>
				        </unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertEquals(
			"{count, plural, other {50% '{'Rabatt'}'}}",
			units.get("discount")
		);
	}
}
