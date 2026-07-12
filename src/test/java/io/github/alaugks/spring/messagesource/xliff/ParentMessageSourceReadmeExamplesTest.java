// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.catalog.resources.LocationPattern;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the examples documented in docs/README-Parent-MessageSource.md. Each test mirrors a
 * README section and its parent-chain configuration against the translations_readme_parent/*
 * (XLIFF) and messages_readme_parent/messages (ResourceBundle) resources.
 */
class ParentMessageSourceReadmeExamplesTest {

	private static final Locale DE = Locale.forLanguageTag("de");
	private static final Locale EN = Locale.forLanguageTag("en");

	private static MessageSource xliff_first_config() {
		ResourceBundleMessageSource parent = new ResourceBundleMessageSource();
		parent.setBasename("messages_readme_parent/messages");
		parent.setDefaultEncoding(StandardCharsets.UTF_8.name());
		parent.setFallbackToSystemLocale(false);

		return XliffResourceMessageSource
			.builder(EN, new LocationPattern("translations_readme_parent/*"))
			.parentMessageSource(parent)
			.build();
	}

	private static MessageSource bundle_first_config() {
		MessageSource parent = XliffResourceMessageSource
			.builder(EN, new LocationPattern("translations_readme_parent/*"))
			.build();

		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("messages_readme_parent/messages");
		messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
		messageSource.setFallbackToSystemLocale(false);
		messageSource.setParentMessageSource(parent);

		return messageSource;
	}

	@ParameterizedTest
	@MethodSource("provider_xliff_first")
	void test_xliff_first(String code, Locale locale, String expected) {
		assertThat(xliff_first_config().getMessage(code, null, locale)).isEqualTo(expected);
	}

	@ParameterizedTest
	@MethodSource("provider_bundle_first")
	void test_bundle_first(String code, Locale locale, String expected) {
		assertThat(bundle_first_config().getMessage(code, null, locale)).isEqualTo(expected);
	}

	@Test
	void test_xliff_first_unknown_code_throws() {
		MessageSource messageSource = xliff_first_config();
		assertThatThrownBy(() -> messageSource.getMessage("unknown_code", null, DE))
			.isInstanceOf(NoSuchMessageException.class);
	}

	@Test
	void test_bundle_first_unknown_code_throws() {
		MessageSource messageSource = bundle_first_config();
		assertThatThrownBy(() -> messageSource.getMessage("unknown_code", null, DE))
			.isInstanceOf(NoSuchMessageException.class);
	}

	static Stream<Arguments> provider_xliff_first() {
		return Stream.of(
			Arguments.of("only_xliff", DE, "Nur in XLIFF"),
			Arguments.of("only_xliff", EN, "Only in XLIFF"),
			Arguments.of("only_bundle", DE, "Nur im ResourceBundle"),
			Arguments.of("only_bundle", EN, "Only in ResourceBundle"),
			Arguments.of("shared", DE, "Geteilt aus XLIFF"),
			Arguments.of("shared", EN, "Shared from XLIFF")
		);
	}

	static Stream<Arguments> provider_bundle_first() {
		return Stream.of(
			Arguments.of("only_bundle", DE, "Nur im ResourceBundle"),
			Arguments.of("only_bundle", EN, "Only in ResourceBundle"),
			Arguments.of("only_xliff", DE, "Nur in XLIFF"),
			Arguments.of("only_xliff", EN, "Only in XLIFF"),
			Arguments.of("shared", DE, "Geteilt aus ResourceBundle"),
			Arguments.of("shared", EN, "Shared from ResourceBundle")
		);
	}
}
