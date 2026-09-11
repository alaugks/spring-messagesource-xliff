// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.base.records.TransFileTargetLocale;
import io.github.alaugks.spring.messagesource.base.records.TransFileTargetLocaleInterface;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceVersionSupportException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceLanguageAttrParserTest {

	private static Resource resource(String xml) {
		return new ByteArrayResource(xml.strip().getBytes(StandardCharsets.UTF_8));
	}

	private static Resource xliff20Resource(String trgLangAttribute) {
		return resource("""
				<?xml version="1.0" encoding="utf-8"?>
				<xliff version="2.0" srcLang="en"%s xmlns="urn:oasis:names:tc:xliff:document:2.0">
				    <file id="f1">
				        <unit id="unit-id">
				            <segment>
				                <source>source</source>
				                <target>target</target>
				            </segment>
				        </unit>
				    </file>
				</xliff>
				""".formatted(trgLangAttribute));
	}

	@Test
	void test_parse_xliff_1_2_returns_filename_from_target_language() {
		TransFileTargetLocaleInterface filename = new ResourceLanguageAttrParser().resolve(
			new ClassPathResource("fixtures/xliff12.xliff")
		);

		assertThat(filename).isEqualTo(new TransFileTargetLocale("de", ""));
	}

	@Test
	void test_parse_xliff_2_x_returns_filename_from_trg_lang() {
		TransFileTargetLocaleInterface filename = new ResourceLanguageAttrParser().resolve(
			new ClassPathResource("fixtures/xliff20.xliff")
		);

		assertThat(filename).isEqualTo(new TransFileTargetLocale("de", ""));
	}

	@Test
	void test_parse_extracts_region_from_language_tag() {
		TransFileTargetLocaleInterface filename = new ResourceLanguageAttrParser().resolve(
				xliff20Resource(" trgLang=\"de-AT\"")
		);

		assertThat(filename).isEqualTo(new TransFileTargetLocale("de", "AT"));
	}

	@Test
	void test_parse_returns_null_when_not_an_xliff_document() {
		TransFileTargetLocaleInterface filename = new ResourceLanguageAttrParser().resolve(
			new ClassPathResource("fixtures/no-xliff.xml")
		);

		assertThat(filename).isNull();
	}

	@Test
	void test_parse_throws_for_unsupported_version() {
		Resource resource = new ClassPathResource("fixtures/xliff10.xliff");

		ResourceLanguageAttrParser r = new ResourceLanguageAttrParser();

		assertThatThrownBy(() -> r.resolve(resource))
				.isInstanceOf(XliffMessageSourceVersionSupportException.class)
				.hasMessage(
						"XLIFF version \"1.0\" not supported. Supported versions: 1.2, 2.0, 2.1 and 2.2"
				);
	}

	@Test
	void test_parse_throws_when_target_language_is_absent() {
		Resource resource = xliff20Resource("");

		ResourceLanguageAttrParser r = new ResourceLanguageAttrParser();

		assertThatThrownBy(() -> r.resolve(resource))
				.isInstanceOf(XliffMessageSourceVersionSupportException.class)
				.hasMessage("Target language not defined in XLIFF file: 2.0");
	}
}
