// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XliffInlineRendererTest {

	@Nested
	class Xliff12 {

		private final Map<String, String> units = new Xliff12Document(
			TestHelper.parseFile("fixtures/inline_xliff12.xliff", true)
		).getUnits();

		@Test
		void test_x_uses_equiv_text() {
			assertThat(units).containsEntry("x_equiv_text", "Hallo {0}!");
		}

		@Test
		void test_x_without_equiv_text_contributes_nothing() {
			assertThat(units).containsEntry("x_without_equiv_text", "Hallo !");
		}

		@Test
		void test_g_keeps_wrapped_text() {
			assertThat(units).containsEntry("g_keeps_text", "Ein Text");
		}

		@Test
		void test_ph_keeps_native_content() {
			assertThat(units).containsEntry("ph_native_content", "Hallo {0}");
		}

		@Test
		void test_bpt_and_ept_keep_native_content() {
			assertThat(units).containsEntry("bpt_ept_native_content", "Ein <b>Text</b>");
		}

		@Test
		void test_bx_and_ex_use_equiv_text() {
			assertThat(units).containsEntry("bx_ex_equiv_text", "Ein <b>Text</b>");
		}

		@Test
		void test_mrk_keeps_wrapped_text() {
			assertThat(units).containsEntry("mrk_keeps_text", "Ein Begriff");
		}

		@Test
		void test_cdata_stays_verbatim() {
			assertThat(units).containsEntry("cdata_verbatim", "<b>Fett</b>");
		}
	}

	@Nested
	class Xliff2x {

		private final Map<String, String> units = new Xliff2xDocument(
			TestHelper.parseFile("fixtures/inline_xliff20.xliff", true)
		).getUnits();

		@Test
		void test_ph_is_replaced_by_original_data() {
			assertThat(units).containsEntry("ph_data_ref", "Hallo {0}!");
		}

		@Test
		void test_ph_with_unknown_data_ref_contributes_nothing() {
			assertThat(units).containsEntry("ph_unknown_data_ref", "Hallo !");
		}

		@Test
		void test_ph_without_original_data_falls_back_to_equiv() {
			assertThat(units).containsEntry("ph_equiv", "Zeile\nEnde");
		}

		@Test
		void test_pc_is_wrapped_in_original_data() {
			assertThat(units).containsEntry("pc_data_ref", "Klicke <a href=\"/x\">hier</a>");
		}

		@Test
		void test_pc_without_original_data_falls_back_to_equiv() {
			assertThat(units).containsEntry("pc_equiv", "Klicke <a>hier</a>");
		}

		@Test
		void test_sc_and_ec_use_original_data() {
			assertThat(units).containsEntry("sc_ec_data_ref", "[Text]");
		}

		@Test
		void test_cp_becomes_code_point() {
			assertThat(units).containsEntry("cp_code_point", "A\u0007B");
		}

		@Test
		void test_annotation_markers_contribute_nothing_and_mrk_keeps_text() {
			assertThat(units).containsEntry("annotation_markers", "A B C");
		}

		@Test
		void test_cdata_stays_verbatim() {
			assertThat(units).containsEntry("cdata_verbatim", "<b>Fett</b>");
		}

		@Test
		void test_cdata_stays_verbatim_xxx() {
			assertThat(units).containsEntry("html-without-cdata", "Click <a href=\"https://example.com\" class=\"btn\">here</a> to view the profile of %s.");
		}

		@Test
		void test_cdata_stays_verbatim_xxx_cdata() {
			assertThat(units).containsEntry("html-with-cdata", "Click <a href=\"https://example.com\" class=\"btn\">here</a> to view the profile of %s.");
		}
	}
}
