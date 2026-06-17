// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.alaugks.spring.messagesource.catalog.resources.LocationPattern;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceValidationException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.NoSuchMessageException;

class XliffResourceMessageSourceTest {

	@ParameterizedTest
	@MethodSource("provider_message")
	void test_get_message(String code, Object[] args, Locale locale, String expected) {
		var messageSource = XliffResourceMessageSource
			.builder(Locale.forLanguageTag("en"), new LocationPattern("translations/*"))
			.validateSchema(true)
			.build();

		assertThat(messageSource.getMessage(code, args, locale)).isEqualTo(expected);
	}

	static Stream<Arguments> provider_message() {
		return Stream.of(
			Arguments.of("postcode", null, Locale.forLanguageTag("en"), "Postcode"),
			Arguments.of("postcode", null, Locale.forLanguageTag("en-US"), "Zip code"),
			Arguments.of("postcode", null, Locale.forLanguageTag("de"), "Postleitzahl"),
			Arguments.of("format_plural", new Object[]{1000L}, Locale.forLanguageTag("en"), "There are 1,000 files."),
			Arguments.of("format_plural", new Object[]{1000L}, Locale.forLanguageTag("en-US"), "There are 1,000 files."),
			Arguments.of("format_plural", new Object[]{1000L}, Locale.forLanguageTag("de"), "Es gibt 1.000 Dateien."),
			Arguments.of("payment.expiry_date", null, Locale.forLanguageTag("en"), "Expiry date"),
			Arguments.of("payment.expiry_date", null, Locale.forLanguageTag("en-US"), "Expiration date"),
			Arguments.of("payment.expiry_date", null, Locale.forLanguageTag("de"), "Ablaufdatum")
		);
	}

	@ParameterizedTest
	@MethodSource({"provider_message", "provider_message_icu4j"})
	void test_get_message_enabled_icu4j(String code, Object[] args, Locale locale, String expected) {
		var messageSource = XliffResourceMessageSource
			.builder(Locale.forLanguageTag("en"), new LocationPattern("translations/*"))
			.enableICU4j()
			.validateSchema(true)
			.build();

		assertThat(messageSource.getMessage(code, args, locale)).isEqualTo(expected);
	}

	static Stream<Arguments> provider_message_icu4j() {
		return Stream.of(
			Arguments.of("plural.file_deleted", new Object[]{Map.of("count", 2)}, Locale.forLanguageTag("en"), "You deleted 2 files."),
			Arguments.of("plural.file_deleted", new Object[]{Map.of("count", 2)}, Locale.forLanguageTag("en-US"), "You deleted 2 files."),
			Arguments.of("plural.file_deleted", new Object[]{Map.of("count", 2)}, Locale.forLanguageTag("de"), "Sie haben 2 Dateien gelöscht.")
		);
	}

	@ParameterizedTest
	@MethodSource("provider_builder_with_location_pattern_multiple_folder")
	void test_builder_with_location_pattern_multiple_folder(String code, Locale locale, String expected) {
		var messageSource = XliffResourceMessageSource
				.builder(
						Locale.forLanguageTag("en"),
						new LocationPattern(
								List.of(
										"translations_en/*",
										"translations_de/*"
								)
						)
				)
				.validateSchema(true)
				.build();

		assertThat(messageSource.getMessage(code, null, locale)).isEqualTo(expected);
	}

	static Stream<Arguments> provider_builder_with_location_pattern_multiple_folder() {
		return Stream.of(
				Arguments.of("messages.postcode", Locale.forLanguageTag("en"), "Postcode"),
				Arguments.of("payment.expiry_date", Locale.forLanguageTag("en"), "Expiry date"),
				Arguments.of("messages.postcode", Locale.forLanguageTag("de"), "Postleitzahl"),
				Arguments.of("payment.expiry_date", Locale.forLanguageTag("de"), "Ablaufdatum")
		);
	}

	@Test
	void test_default_domain() {
		var messageSource = XliffResourceMessageSource
				.builder(Locale.forLanguageTag("en"), new LocationPattern("translations/*"))
				.defaultDomain("payment")
				.validateSchema(true)
				.build();

		assertThat(messageSource.getMessage(
				"expiry_date",
				null,
				Locale.forLanguageTag("en-US")
		)).isEqualTo("Expiration date");
	}

	@Test
	void test_file_extensions() {
		var messageSource = XliffResourceMessageSource
				.builder(Locale.forLanguageTag("en"), new LocationPattern("translations/*"))
				.fileExtensions(List.of("xlf"))
				.validateSchema(true)
				.build();

		var locale = Locale.forLanguageTag("en");
		assertThatThrownBy(() -> messageSource.getMessage(
				"postcode",
				null,
				locale
		)).isInstanceOf(NoSuchMessageException.class);
	}

	@Test
	void test_validate_schema_enabled_throws() {
		var builder = XliffResourceMessageSource
				.builder(Locale.forLanguageTag("en"), new LocationPattern("fixtures/schemainvalid.xliff"))
				.validateSchema(true);

		assertThatThrownBy(builder::build).isInstanceOf(XliffMessageSourceValidationException.class);
	}

	@Test
	void test_validate_schema_disabled_by_default() {
		var messageSource = XliffResourceMessageSource
				.builder(Locale.forLanguageTag("en"), new LocationPattern("fixtures/schemainvalid.xliff"))
				.defaultDomain("schemainvalid")
				.build();

		assertThat(messageSource.getMessage(
				"novalid",
				null,
				Locale.forLanguageTag("en")
		)).isEqualTo("Target");
	}
}
