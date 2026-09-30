// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import com.ibm.icu.text.MessageFormat;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IcuPatternGeneratorTest {

	@Test
	void test_plural_to_icu_pattern() {

		String icuPattern = "{count, plural, =0 {Sie haben keine Dateien gelöscht.} =1 {Sie haben eine Datei gelöscht.} other {Sie haben {count} Dateien gelöscht.}}";

		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.2" srcLang="en" trgLang="de"
			       xmlns="urn:oasis:names:tc:xliff:document:2.0"
			       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
			    <file id="f1">
			        <unit id="tu1" name="file_deleted" pgs:switch="plural:count">
			            <segment pgs:case="0">
			                <source>You deleted no plural.</source>
			                <target>Sie haben keine Dateien gelöscht.</target>
			            </segment>
			            <segment pgs:case="1">
			                <source>You deleted one file.</source>
			                <target>Sie haben eine Datei gelöscht.</target>
			            </segment>
			            <segment pgs:case="other">
			                <source>You deleted <ph id="1" disp="count"/> plural.</source>
			                <target>Sie haben <ph id="1" disp="count"/> Dateien gelöscht.</target>
			            </segment>
			        </unit>
			    </file>
			</xliff>
			""")).getUnits();

		assertThat(units).containsEntry("file_deleted", icuPattern);

		MessageFormat messageFormat = new MessageFormat(icuPattern, Locale.forLanguageTag("de"));
		assertThat(messageFormat.format(Map.of("count", 1000))).isEqualTo("Sie haben 1.000 Dateien gelöscht.");
	}

	@Test
	void test_plural_to_icu_pattern_multiple_placeholder() {

		String icuPattern = "{count, plural, =0 {Sie haben keine Dateien gelöscht.} =1 {Sie haben eine Datei in der Kategorie {category} gelöscht.} other {Sie haben {count} Dateien in der Kategorie {category} gelöscht.}}";

		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.2" srcLang="en" trgLang="de"
			       xmlns="urn:oasis:names:tc:xliff:document:2.0"
			       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
			    <file id="f1">
					<unit id="tu1" name="multiple_placeholder" pgs:switch="plural:count">
						<segment pgs:case="0">
							<source>You deleted no files.</source>
							<target>Sie haben keine Dateien gelöscht.</target>
						</segment>
						<segment pgs:case="1">
							<source>You deleted one file.</source>
							<target>Sie haben eine Datei in der Kategorie <ph id="2" disp="category"/> gelöscht.</target>
						</segment>
						<segment pgs:case="other">
							<source>You deleted <ph id="1" disp="count"/> files in category <ph id="2" disp="category"/>.</source>
							<target>Sie haben <ph id="1" disp="count"/> Dateien in der Kategorie <ph id="2" disp="category"/> gelöscht.</target>
						</segment>
					</unit>
			    </file>
			</xliff>
			""")).getUnits();

		assertThat(units).containsEntry("multiple_placeholder", icuPattern);

		MessageFormat messageFormat = new MessageFormat(icuPattern, Locale.forLanguageTag("de"));
		assertThat(messageFormat.format(Map.of("count", 1000, "category", "FooBar"))).isEqualTo(
			"Sie haben 1.000 Dateien in der Kategorie FooBar gelöscht.");
		assertThat(messageFormat.format(Map.of("count", 1, "category", "FooBar"))).isEqualTo(
			"Sie haben eine Datei in der Kategorie FooBar gelöscht.");
	}

	// Arabic (ar) is one of the languages that actually use all six CLDR plural
	// categories; the target text is the category name purely to keep the assertion exact.
	@Test
	void test_plural_all_cldr_keywords_kept_verbatim() {

		String icuPattern = "{count, plural, zero {zero} one {one} two {two} few {few} many {many} other {other}}";

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

		assertThat(units).containsEntry("count", icuPattern);

		// A plural category is selected via a Number (ICU derives the CLDR category
		// from the locale's rules), not via the keyword string. The Arabic (ar)
		// rules map a number n to a category as follows:
		//   zero  -> n = 0
		//   one   -> n = 1
		//   two   -> n = 2
		//   few   -> n % 100 = 3..10   (e.g. 3, 10, 103)
		//   many  -> n % 100 = 11..99  (e.g. 11, 50, 99)
		//   other -> everything else   (e.g. 100, 101, decimals)
		MessageFormat messageFormat = new MessageFormat(icuPattern, Locale.forLanguageTag("ar"));

		assertThat(messageFormat.format(Map.of("count", 0))).isEqualTo("zero");
		assertThat(messageFormat.format(Map.of("count", 1))).isEqualTo("one");
		assertThat(messageFormat.format(Map.of("count", 2))).isEqualTo("two");
		assertThat(messageFormat.format(Map.of("count", 10))).isEqualTo("few");
		assertThat(messageFormat.format(Map.of("count", 50))).isEqualTo("many");
		assertThat(messageFormat.format(Map.of("count", 100))).isEqualTo("other");
	}

	@Test
	void test_plural_numeric_cases_become_exact_matches() {

		String icuPattern = "{count, plural, =0 {null} =2 {zwei} =5 {fünf} other {viele}}";

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

		assertThat(units).containsEntry("count", icuPattern);

		assertThat(
			new MessageFormat(icuPattern).format(Map.of("count", 2))
		).isEqualTo("zwei");
	}

	@Test
	void test_gender_to_icu_select_pattern() {

		String icuPattern = "{recipient_gender, select, feminine {Wie geht es ihr?} masculine {Wie geht es ihm?} other {Wie geht es ihnen?}}";

		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.2" srcLang="en" trgLang="de"
			       xmlns="urn:oasis:names:tc:xliff:document:2.0"
			       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
			    <file id="f1">
			        <unit id="tu1" name="greeting" pgs:switch="gender:recipient_gender">
			            <segment pgs:case="feminine">
			                <source>How is she?</source>
			                <target>Wie geht es ihr?</target>
			            </segment>
			            <segment pgs:case="masculine">
			                <source>How is he?</source>
			                <target>Wie geht es ihm?</target>
			            </segment>
			            <segment pgs:case="other">
			                <source>How are they?</source>
			                <target>Wie geht es ihnen?</target>
			            </segment>
			        </unit>
			    </file>
			</xliff>
			""")).getUnits();

		assertThat(units).containsEntry("greeting", icuPattern);

		assertThat(
			new MessageFormat(icuPattern).format(Map.of("recipient_gender", "feminine"))
		).isEqualTo("Wie geht es ihr?");
	}

	@Test
	void test_escapes_icu_metacharacters_in_text() {

		String icuPattern = "{count, plural, =30 {'#' 30% '{'Rabatt'''}'} other {'#' 50% '{'Rabatt'''}'}}";

		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.2" srcLang="en" trgLang="de"
			       xmlns="urn:oasis:names:tc:xliff:document:2.0"
			       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
			    <file id="f1">
			        <unit id="tu1" name="discount" pgs:switch="plural:count">
						<segment pgs:case="30">
			            	<target># 30% {Rabatt'}</target>
			            </segment>
			            <segment pgs:case="other">
			            	<target># 50% {Rabatt'}</target>
			            </segment>
			        </unit>
			    </file>
			</xliff>
			""")).getUnits();

		assertThat(units).containsEntry("discount", icuPattern);

		assertThat(
			new MessageFormat(icuPattern, Locale.forLanguageTag("ar")).format(Map.of("count", 30))
		).isEqualTo("# 30% {Rabatt'}");
	}

	@Test
	void test_unit_without_segments_yields_no_unit() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.2" srcLang="en" trgLang="de"
			       xmlns="urn:oasis:names:tc:xliff:document:2.0"
			       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
			    <file id="f1">
			        <unit id="tu1" name="count" pgs:switch="plural:count">
			            <notes><note>no segments here</note></notes>
			        </unit>
			    </file>
			</xliff>
			""")).getUnits();

		assertThat(units).doesNotContainKey("count");
	}

	@ParameterizedTest
	@MethodSource("provider_invalid_switch_or_case_structure")
	void test_malformed_switch_or_case_structure_fails_with_domain_error(String switchValue, String segments, String reason) {
		String xml = """
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.2" srcLang="en" trgLang="de"
			       xmlns="urn:oasis:names:tc:xliff:document:2.0"
			       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
			    <file id="f1">
			        <unit id="tu1" name="key" pgs:switch="%s">
			            %s
			        </unit>
			    </file>
			</xliff>
			""".formatted(switchValue, segments);

		Xliff2xDocument xliffDokument = new Xliff2xDocument(TestHelper.parseDocument(xml));

		assertThatThrownBy(xliffDokument::getUnits)
			.isInstanceOf(XliffMessageSourceRuntimeException.class)
			.hasMessageContaining("'key'")
			.hasMessageContaining(reason);
	}

	static Stream<Arguments> provider_invalid_switch_or_case_structure() {
		String other = "<segment pgs:case=\"other\"><target>x</target></segment>";
		return Stream.of(
			Arguments.of("invalid", other, "malformed pgs:switch"),
			Arguments.of("plural:", other, "malformed pgs:switch"),
			Arguments.of(":count", other, "malformed pgs:switch"),
			Arguments.of("plural:count", other + other, "duplicate pgs:case 'other'"),
			Arguments.of("plural:count", "<segment pgs:case=\"one\"><target>x</target></segment><segment pgs:case=\"one\"><target>y</target></segment>" + other, "duplicate pgs:case 'one'"),
			Arguments.of("plural:count", other + "<segment><target>x</target></segment>", "segment without pgs:case")
		);
	}

	@Test
	void test_case_falls_back_to_source_when_target_is_missing() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.2" srcLang="en" trgLang="de"
			       xmlns="urn:oasis:names:tc:xliff:document:2.0"
			       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
			    <file id="f1">
			        <unit id="tu1" name="count" pgs:switch="plural:count">
			            <segment pgs:case="other"><source>fallback</source></segment>
			        </unit>
			    </file>
			</xliff>
			""")).getUnits();

		assertThat(units).containsEntry("count", "{count, plural, other {fallback}}");
	}

	@Test
	void test_case_is_empty_when_source_and_target_are_missing() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.2" srcLang="en" trgLang="de"
			       xmlns="urn:oasis:names:tc:xliff:document:2.0"
			       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
			    <file id="f1">
			        <unit id="tu1" name="count" pgs:switch="plural:count">
			            <segment pgs:case="0"/>
			            <segment pgs:case="other"/>
			        </unit>
			    </file>
			</xliff>
			""")).getUnits();

		assertThat(units).containsEntry("count", "{count, plural, =0 {} other {}}");
	}

	@Test
	void test_comment_inside_target_contributes_nothing() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.2" srcLang="en" trgLang="de"
			       xmlns="urn:oasis:names:tc:xliff:document:2.0"
			       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
			    <file id="f1">
			        <unit id="tu1" name="count" pgs:switch="plural:count">
			            <segment pgs:case="other"><target>Text<!-- comment -->More</target></segment>
			        </unit>
			    </file>
			</xliff>
			""")).getUnits();

		assertThat(units).containsEntry("count", "{count, plural, other {TextMore}}");
	}

	@Test
	void test_non_ph_inline_element_recurses_into_its_text() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.2" srcLang="en" trgLang="de"
			       xmlns="urn:oasis:names:tc:xliff:document:2.0"
			       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
			    <file id="f1">
			        <unit id="tu1" name="count" pgs:switch="plural:count">
			            <segment pgs:case="other"><target>Hello <pc id="1">World</pc>!</target></segment>
			        </unit>
			    </file>
			</xliff>
			""")).getUnits();

		assertThat(units).containsEntry("count", "{count, plural, other {Hello World!}}");
	}

	@Test
	void test_placeholder_without_disp_attribute_contributes_nothing() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.2" srcLang="en" trgLang="de"
			       xmlns="urn:oasis:names:tc:xliff:document:2.0"
			       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
			    <file id="f1">
			        <unit id="tu1" name="count" pgs:switch="plural:count">
			            <segment pgs:case="other"><target>Before<ph id="1"/>After</target></segment>
			        </unit>
			    </file>
			</xliff>
			""")).getUnits();

		assertThat(units).containsEntry("count", "{count, plural, other {BeforeAfter}}");
	}

	@Test
	void test_escapes_adjacent_metacharacters_and_leaves_pipe_unquoted() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.2" srcLang="en" trgLang="de"
			       xmlns="urn:oasis:names:tc:xliff:document:2.0"
			       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
			    <file id="f1">
			        <unit id="tu1" name="count" pgs:switch="plural:count">
			            <segment pgs:case="other"><target>Special {}| chars</target></segment>
			        </unit>
			    </file>
			</xliff>
			""")).getUnits();

		assertThat(units).containsEntry("count", "{count, plural, other {Special '{}'| chars}}");
	}

	@ParameterizedTest
	@MethodSource("provider_invalid_pgs_structure")
	void test_invalid_structure_fails_with_domain_error(String switchValue, String caseValue, String disp, String reason) {
		String xml = """
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.2" srcLang="en" trgLang="de"
			       xmlns="urn:oasis:names:tc:xliff:document:2.0"
			       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
			    <file id="f1">
			        <unit id="tu1" name="key" pgs:switch="%s">
			            <segment pgs:case="%s"><target>Text <ph id="1" disp="%s"/></target></segment>
			        </unit>
			    </file>
			</xliff>
			""".formatted(switchValue, caseValue, disp);

		Xliff2xDocument xliffDokument = new Xliff2xDocument(TestHelper.parseDocument(xml));

		assertThatThrownBy(xliffDokument::getUnits)
			.isInstanceOf(XliffMessageSourceRuntimeException.class)
			.hasMessageContaining("'key'")
			.hasMessageContaining(reason);
	}

	static Stream<Arguments> provider_invalid_pgs_structure() {
		return Stream.of(
			Arguments.of("plural:a b", "other", "x", "pgs:switch variable"),
			Arguments.of("plural:a}", "other", "x", "pgs:switch variable"),
			Arguments.of("foo:count", "other", "x", "unsupported pgs:switch type"),
			Arguments.of("plural:count", "other", "a, b", "<ph disp>"),
			Arguments.of("plural:count", "other", "}", "<ph disp>"),
			Arguments.of("plural:count", "abc", "x", "invalid plural pgs:case"),
			Arguments.of("select:kind", "a b", "x", "select pgs:case"),
			Arguments.of("select:kind", "{", "x", "select pgs:case"),
			Arguments.of("plural:count", "one", "x", "missing mandatory 'other' case")
		);
	}

	@ParameterizedTest
	@MethodSource("provider_literal_text_roundtrip")
	void test_literal_text_survives_icu_roundtrip(String text) {
		String xml = """
			<?xml version="1.0" encoding="utf-8"?>
			<xliff version="2.2" srcLang="en" trgLang="de"
			       xmlns="urn:oasis:names:tc:xliff:document:2.0"
			       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
			    <file id="f1">
			        <unit id="tu1" name="key" pgs:switch="plural:count">
			            <segment pgs:case="other"><target><![CDATA[%s]]></target></segment>
			        </unit>
			    </file>
			</xliff>
			""".formatted(text);

		String pattern = new Xliff2xDocument(TestHelper.parseDocument(xml)).getUnits().get("key");

		assertThat(new MessageFormat(pattern, Locale.ENGLISH).format(Map.of("count", 5))).isEqualTo(text);
	}

	static Stream<Arguments> provider_literal_text_roundtrip() {
		return Stream.of(
			Arguments.of("Hello {world}"),
			Arguments.of("It's nice"),
			Arguments.of("foo # bar"),
			Arguments.of("foo | bar"),
			Arguments.of("{{{{}}}}"),
			Arguments.of("''"),
			Arguments.of("'{'"),
			Arguments.of("a'{b}'c")
		);
	}
}
