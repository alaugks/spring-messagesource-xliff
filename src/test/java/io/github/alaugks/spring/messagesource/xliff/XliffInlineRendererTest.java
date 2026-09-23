// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Element;

import static org.assertj.core.api.Assertions.assertThat;

class XliffInlineRendererTest {

	@Nested
	class Xliff12 {

		@ParameterizedTest(name = "{0}")
		@MethodSource("cases")
		void test_render_inline_elements(String name, String transUnit, String expected) {
			var units = new Xliff12Document(TestHelper.parseDocument(wrap(transUnit), true)).getUnits();
			assertThat(units).containsEntry("code", expected);
		}

		private static String wrap(String transUnit) {
			return """
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="1.2" xmlns="urn:oasis:names:tc:xliff:document:1.2">
				    <file source-language="en" target-language="de" datatype="plaintext" original="messages">
				        <body>
				""" + transUnit + """
				        </body>
				    </file>
				</xliff>
				""";
		}

		static Stream<Arguments> cases() {
			return Stream.of(
				Arguments.of(
					"x_uses_equiv_text",
					"""
					<trans-unit id="x_equiv_text" resname="code">
						<source>Hello <x id="1" equiv-text="{0}"/>!</source>
						<target>Hallo <x id="1" equiv-text="{0}"/>!</target>
					</trans-unit>
					""",
					"Hallo {0}!"
				),
				Arguments.of(
					"x_without_equiv_text_contributes_nothing",
					"""
					<trans-unit id="x_without_equiv_text" resname="code">
						<source>Hello <x id="1"/>!</source>
						<target>Hallo <x id="1"/>!</target>
					</trans-unit>
					""",
					"Hallo !"
				),
				Arguments.of(
					"g_keeps_wrapped_text",
					"""
					<trans-unit id="g_keeps_text" resname="code">
						<source>A <g id="1" ctype="bold">text</g></source>
						<target>Ein <g id="1" ctype="bold">Text</g></target>
					</trans-unit>
					""",
					"Ein Text"
				),
				Arguments.of(
					"ph_keeps_native_content",
					"""
					<trans-unit id="ph_native_content" resname="code">
						<source>Hello <ph id="1">{0}</ph></source>
						<target>Hallo <ph id="1">{0}</ph></target>
					</trans-unit>
					""",
					"Hallo {0}"
				),
				Arguments.of(
					"bpt_and_ept_keep_native_content",
					"""
					<trans-unit id="bpt_ept_native_content" resname="code">
						<source>A <bpt id="1">&lt;b&gt;</bpt>text<ept id="1">&lt;/b&gt;</ept></source>
						<target>Ein <bpt id="1">&lt;b&gt;</bpt>Text<ept id="1">&lt;/b&gt;</ept></target>
					</trans-unit>
					""",
					"Ein <b>Text</b>"
				),
				Arguments.of(
					"bx_and_ex_use_equiv_text",
					"""
					<trans-unit id="bx_ex_equiv_text" resname="code">
						<source>A <bx id="1" equiv-text="&lt;b&gt;"/>text<ex id="1" equiv-text="&lt;/b&gt;"/></source>
						<target>Ein <bx id="1" equiv-text="&lt;b&gt;"/>Text<ex id="1" equiv-text="&lt;/b&gt;"/></target>
					</trans-unit>
					""",
					"Ein <b>Text</b>"
				),
				Arguments.of(
					"mrk_keeps_wrapped_text",
					"""
					<trans-unit id="mrk_keeps_text" resname="code">
						<source>A <mrk mtype="term">term</mrk></source>
						<target>Ein <mrk mtype="term">Begriff</mrk></target>
					</trans-unit>
					""",
					"Ein Begriff"
				),
				Arguments.of(
					"cdata_stays_verbatim",
					"""
					<trans-unit id="cdata_verbatim" resname="code">
						<source><![CDATA[<b>Bold</b>]]></source>
						<target><![CDATA[<b>Fett</b>]]></target>
					</trans-unit>
					""",
					"<b>Fett</b>"
				)
			);
		}
	}

	@Nested
	class Xliff2x {

		@ParameterizedTest(name = "{0}")
		@MethodSource("cases")
		void test_render_inline_elements(String name, String unit, String expected) {
			var units = new Xliff2xDocument(TestHelper.parseDocument(wrap(unit), true)).getUnits();
			assertThat(units).containsEntry("code", expected);
		}

		private static String wrap(String unit) {
			return """
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.0" srcLang="en" trgLang="de" xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
				""" + unit + """
				    </file>
				</xliff>
				""";
		}

		static Stream<Arguments> cases() {
			return Stream.of(
				Arguments.of(
					"ph_is_replaced_by_original_data",
					"""
					<unit id="ph_data_ref" name="code">
						<originalData>
							<data id="d1">{0}</data>
						</originalData>
						<segment>
							<source>Hello <ph id="1" dataRef="d1"/>!</source>
							<target>Hallo <ph id="1" dataRef="d1"/>!</target>
						</segment>
					</unit>
					""",
					"Hallo {0}!"
				),
				Arguments.of(
					"ph_with_unknown_data_ref_contributes_nothing",
					"""
					<unit id="ph_unknown_data_ref" name="code">
						<segment>
							<source>Hello <ph id="1" dataRef="missing"/>!</source>
							<target>Hallo <ph id="1" dataRef="missing"/>!</target>
						</segment>
					</unit>
					""",
					"Hallo !"
				),
				Arguments.of(
					"ph_without_original_data_falls_back_to_equiv",
					"""
					<unit id="ph_equiv" name="code">
						<segment>
							<source>Line<ph id="1" equiv="&#10;"/>end</source>
							<target>Zeile<ph id="1" equiv="&#10;"/>Ende</target>
						</segment>
					</unit>
					""",
					"Zeile\nEnde"
				),
				Arguments.of(
					"pc_is_wrapped_in_original_data",
					"""
					<unit id="pc_data_ref" name="code">
						<originalData>
							<data id="d1">&lt;a href="/x"&gt;</data>
							<data id="d2">&lt;/a&gt;</data>
						</originalData>
						<segment>
							<source>Click <pc id="1" dataRefStart="d1" dataRefEnd="d2">here</pc></source>
							<target>Klicke <pc id="1" dataRefStart="d1" dataRefEnd="d2">hier</pc></target>
						</segment>
					</unit>
					""",
					"Klicke <a href=\"/x\">hier</a>"
				),
				Arguments.of(
					"pc_without_original_data_falls_back_to_equiv",
					"""
					<unit id="pc_equiv" name="code">
						<segment>
							<source>Click <pc id="1" equivStart="&lt;a&gt;" equivEnd="&lt;/a&gt;">here</pc></source>
							<target>Klicke <pc id="1" equivStart="&lt;a&gt;" equivEnd="&lt;/a&gt;">hier</pc></target>
						</segment>
					</unit>
					""",
					"Klicke <a>hier</a>"
				),
				Arguments.of(
					"sc_and_ec_use_original_data",
					"""
					<unit id="sc_ec_data_ref" name="code">
						<originalData>
							<data id="d1">[</data>
							<data id="d2">]</data>
						</originalData>
						<segment>
							<source><sc id="1" dataRef="d1"/>Text<ec startRef="1" dataRef="d2"/></source>
							<target><sc id="1" dataRef="d1"/>Text<ec startRef="1" dataRef="d2"/></target>
						</segment>
					</unit>
					""",
					"[Text]"
				),
				Arguments.of(
					"cp_becomes_code_point",
					"""
					<unit id="cp_code_point" name="code">
						<segment>
							<source>A<cp hex="0007"/>B</source>
							<target>A<cp hex="0007"/>B</target>
						</segment>
					</unit>
					""",
					"A\u0007B"
				),
				Arguments.of(
					"annotation_markers_contribute_nothing_and_mrk_keeps_text",
					"""
					<unit id="annotation_markers" name="code">
						<segment>
							<source>A <sm id="m1"/>B<em startRef="m1"/> <mrk id="m2" type="term">C</mrk></source>
							<target>A <sm id="m1"/>B<em startRef="m1"/> <mrk id="m2" type="term">C</mrk></target>
						</segment>
					</unit>
					""",
					"A B C"
				),
				Arguments.of(
					"cdata_stays_verbatim",
					"""
					<unit id="cdata_verbatim" name="code">
						<segment>
							<source><![CDATA[<b>Bold</b>]]></source>
							<target><![CDATA[<b>Fett</b>]]></target>
						</segment>
					</unit>
					""",
					"<b>Fett</b>"
				),
				Arguments.of(
					"cdata_stays_verbatim_html_without_cdata",
					"""
					<unit id="html-without-cdata" name="code">
						<originalData>
							<data id="d1">&lt;a href="https://example.com" class="btn"&gt;</data>
							<data id="d2">&lt;/a&gt;</data>
							<data id="d3">%s</data>
						</originalData>
						<segment>
							<source>Klicken Sie <pc id="1" dataRefStart="d1" dataRefEnd="d2">hier</pc>, um das Profil von <ph id="2" dataRef="d3"/> aufzurufen.</source>
							<target>Click <pc id="1" dataRefStart="d1" dataRefEnd="d2">here</pc> to view the profile of <ph id="2" dataRef="d3"/>.</target>
						</segment>
					</unit>
					""",
					"Click <a href=\"https://example.com\" class=\"btn\">here</a> to view the profile of %s."
				),
				Arguments.of(
					"cdata_stays_verbatim_html_with_cdata",
					"""
					<unit id="html-with-cdata" name="code">
						<originalData>
							<data id="d1"><![CDATA[<a href="https://example.com" class="btn">]]></data>
							<data id="d2"><![CDATA[</a>]]></data>
							<data id="d3"><![CDATA[%s]]></data>
						</originalData>
						<segment>
							<source>Klicken Sie <pc id="1" dataRefStart="d1" dataRefEnd="d2">hier</pc>, um das Profil von <ph id="2" dataRef="d3"/> aufzurufen.</source>
							<target>Click <pc id="1" dataRefStart="d1" dataRefEnd="d2">here</pc> to view the profile of <ph id="2" dataRef="d3"/>.</target>
						</segment>
					</unit>
					""",
					"Click <a href=\"https://example.com\" class=\"btn\">here</a> to view the profile of %s."
				)
			);
		}
	}

	@Nested
	class MaxDepth {

		@Test
		void test_content_beyond_max_depth_is_dropped_without_stack_overflow() {
			Element source = TestHelper.parseDocument(
				"<source>SHALLOW" + "<g>".repeat(16) + "DEEP" + "</g>".repeat(16) + "</source>"
			).getDocumentElement();

			String rendered = new XliffInlineRenderer().render(source);

			assertThat(rendered)
				.contains("SHALLOW")
				.doesNotContain("DEEP");
		}
	}
}
