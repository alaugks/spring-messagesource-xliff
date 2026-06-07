// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ibm.icu.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the ICU patterns produced by {@link IcuPatternGenerator} are
 * valid and resolve correctly when formatted with ICU4J's {@link MessageFormat}
 * (the engine able to handle {@code plural}/{@code select} and named arguments).
 * The pattern strings here are exactly the ones asserted in
 * {@link IcuPatternGeneratorTest}.
 */
class IcuPatternMessageFormatTest {

	private static String render(String pattern, Locale locale, Map<String, Object> args) {
		return new MessageFormat(pattern, locale).format(args);
	}

	@Test
	void test_plural_pattern_resolves() {
		String pattern = "{file_count, plural, =0 {Sie haben keine Dateien gelöscht.}"
			+ " =1 {Sie haben eine Datei gelöscht.}"
			+ " other {Sie haben {file_count} Dateien gelöscht.}}";

		assertEquals(
			"Sie haben keine Dateien gelöscht.",
			render(pattern, Locale.GERMAN, Map.of("file_count", 0))
		);
		assertEquals(
			"Sie haben eine Datei gelöscht.",
			render(pattern, Locale.GERMAN, Map.of("file_count", 1))
		);
		assertEquals(
			"Sie haben 5 Dateien gelöscht.",
			render(pattern, Locale.GERMAN, Map.of("file_count", 5))
		);

	}

	@Test
	void test_select_pattern_resolves_and_unescapes_apostrophe() {
		String pattern = "{recipient_gender, select, feminine {Wie geht''s ihr?}"
			+ " masculine {Wie geht''s ihm?}"
			+ " other {Wie geht''s ihnen?}}";

		assertEquals(
			"Wie geht's ihr?",
			render(pattern, Locale.GERMAN, Map.of("recipient_gender", "feminine"))
		);
		assertEquals(
			"Wie geht's ihm?",
			render(pattern, Locale.GERMAN, Map.of("recipient_gender", "masculine"))
		);
		assertEquals(
			"Wie geht's ihnen?",
			render(pattern, Locale.GERMAN, Map.of("recipient_gender", "unknown"))
		);
	}

	@Test
	void test_nested_pattern_resolves() {
		String pattern = "{user_gender, select,"
			+ " feminine {{count, plural, one {Sie hat eine} other {Sie hat viele}}}"
			+ " masculine {{count, plural, one {Er hat eine} other {Er hat viele}}}"
			+ " other {{count, plural, other {Sie haben viele}}}}";

		assertEquals(
			"Sie hat eine",
			render(pattern, Locale.GERMAN, Map.of("user_gender", "feminine", "count", 1))
		);
		assertEquals(
			"Er hat viele",
			render(pattern, Locale.GERMAN, Map.of("user_gender", "masculine", "count", 3))
		);
		assertEquals(
			"Sie haben viele",
			render(pattern, Locale.GERMAN, Map.of("user_gender", "other", "count", 2))
		);
	}

	@Test
	void test_all_cldr_plural_keywords_resolve_in_arabic() {
		// Arabic uses all six CLDR plural categories, so it actually selects
		// zero/two/few/many (which German/English never reach).
		String pattern = "{count, plural, zero {zero} one {one} two {two}"
			+ " few {few} many {many} other {other}}";
		Locale arabic = Locale.forLanguageTag("ar");

		assertEquals("zero", render(pattern, arabic, Map.of("count", 0)));
		assertEquals("one", render(pattern, arabic, Map.of("count", 1)));
		assertEquals("two", render(pattern, arabic, Map.of("count", 2)));
		assertEquals("few", render(pattern, arabic, Map.of("count", 3)));
		assertEquals("many", render(pattern, arabic, Map.of("count", 11)));
		assertEquals("other", render(pattern, arabic, Map.of("count", 100)));
	}

	@Test
	void test_escaped_metacharacters_resolve_to_literals() {
		String pattern = "{count, plural, other {50% '{'Rabatt'}'}}";

		assertEquals(
			"50% {Rabatt}",
			render(pattern, Locale.GERMAN, Map.of("count", 1))
		);
	}

	// End-to-end: parse the XLIFF, take the pattern *as produced by the generator*
	// (not a hand-written copy) and verify it actually resolves for every Arabic
	// CLDR plural category via ICU MessageFormat.
	@Test
	void test_generated_arabic_pattern_resolves_end_to_end() {
		Locale arabic = Locale.forLanguageTag("ar");
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

		String pattern = units.get("count");

		assertEquals("zero", render(pattern, arabic, Map.of("count", 0)));
		assertEquals("one", render(pattern, arabic, Map.of("count", 1)));
		assertEquals("two", render(pattern, arabic, Map.of("count", 2)));
		assertEquals("few", render(pattern, arabic, Map.of("count", 3)));
		assertEquals("many", render(pattern, arabic, Map.of("count", 11)));
		assertEquals("other", render(pattern, arabic, Map.of("count", 100)));
	}
}
