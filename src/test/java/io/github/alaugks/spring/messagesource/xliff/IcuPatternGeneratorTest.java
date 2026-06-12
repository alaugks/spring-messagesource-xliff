// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import static org.assertj.core.api.Assertions.assertThat;

import com.ibm.icu.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;

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
				                <source>You deleted no files.</source>
				                <target>Sie haben keine Dateien gelöscht.</target>
				            </segment>
				            <segment pgs:case="1">
				                <source>You deleted one file.</source>
				                <target>Sie haben eine Datei gelöscht.</target>
				            </segment>
				            <segment pgs:case="other">
				                <source>You deleted <ph id="1" disp="count"/> files.</source>
				                <target>Sie haben <ph id="1" disp="count"/> Dateien gelöscht.</target>
				            </segment>
				        </unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertThat(units).containsEntry("file_deleted", icuPattern);

		MessageFormat messageFormat = new MessageFormat(icuPattern, Locale.forLanguageTag("de"));
		assertThat(messageFormat.format(Map.of("count", 1000L))).isEqualTo("Sie haben 1.000 Dateien gelöscht.");
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
	void test_segment_without_case_defaults_to_other() {
		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.2" srcLang="en" trgLang="de"
				       xmlns="urn:oasis:names:tc:xliff:document:2.0"
				       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
				    <file id="f1">
				        <unit id="tu1" name="count" pgs:switch="plural:count">
				            <segment pgs:case="one">
				            	<target>eine</target>
				            </segment>
				            <segment>
				            	<target>andere</target>
							</segment>
				        </unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertThat(units).containsEntry("count", "{count, plural, one {eine} other {andere}}");
	}

	@Test
	void test_gender_to_icu_select_pattern() {

		String icuPattern = "{recipient_gender, select, feminine {Wie geht''s ihr?} masculine {Wie geht''s ihm?} other {Wie geht''s ihnen?}}";

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

		assertThat(units).containsEntry("greeting", icuPattern);

		assertThat(
			new MessageFormat(icuPattern).format(Map.of("recipient_gender", "feminine"))
		).isEqualTo("Wie geht's ihr?");
	}

	@Test
	void test_escapes_icu_metacharacters_in_text() {

		String icuPattern = "{count, plural, =30 {'#' 30% '{'Rabatt'}'} other {'#' 50% '{'Rabatt'}'}}";

		Map<String, String> units = new Xliff2xDocument(TestHelper.parseDocument("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.2" srcLang="en" trgLang="de"
				       xmlns="urn:oasis:names:tc:xliff:document:2.0"
				       xmlns:pgs="urn:oasis:names:tc:xliff:pgs:1.0">
				    <file id="f1">
				        <unit id="tu1" name="discount" pgs:switch="plural:count">
							<segment pgs:case="30">
				            	<target># 30% {Rabatt}</target>
				            </segment>
				            <segment pgs:case="other">
				            	<target># 50% {Rabatt}</target>
				            </segment>
				        </unit>
				    </file>
				</xliff>
				""")).getUnits();

		assertThat(units).containsEntry("discount", icuPattern);

		assertThat(
			new MessageFormat(icuPattern, Locale.forLanguageTag("ar")).format(Map.of("count", 30))
		).isEqualTo("# 30% {Rabatt}");
	}
}
