// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.alaugks.spring.messagesource.catalog.resources.LocationPattern;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceValidationException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.context.NoSuchMessageException;

class XliffResourceMessageSourceTest {

	@Test
	void test_getMessage_code_args_locale_with_LocationPattern() {
		var messageSource = XliffResourceMessageSource
			.builder(Locale.forLanguageTag("en"), new LocationPattern("translations/*"))
			.build();

		assertEquals("Postcode", messageSource.getMessage(
			"postcode",
			null,
			Locale.forLanguageTag("en")
		));
	}

	@Test
	void test_getMessage_code_args_locale() {
		var messageSource = XliffResourceMessageSource
				.builder(Locale.forLanguageTag("en"), new LocationPattern("translations/*"))
				.build();

		assertEquals("Postcode", messageSource.getMessage(
				"postcode",
				null,
				Locale.forLanguageTag("en")
		));

		assertEquals("Zip code", messageSource.getMessage(
				"postcode",
				null,
				Locale.forLanguageTag("en-US")
		));

		assertEquals("Postleitzahl", messageSource.getMessage(
				"postcode",
				null,
				Locale.forLanguageTag("de")
		));

		Map<String, Object> args = new HashMap<>();
		args.put("file_count", 3);

		assertEquals("Sie haben 3 Dateien gelöscht.", messageSource.getMessage(
			"downloads.file_deleted",
			new Object[]{args},
			Locale.forLanguageTag("de")
		));
	}

	@Test
	void test_builder_withLocationPatterns() {
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
				.build();

		assertEquals(
				"Postcode",
				messageSource.getMessage("messages.postcode", null, Locale.forLanguageTag("en"))
		);
		assertEquals(
				"Expiry date",
				messageSource.getMessage("payment.expiry_date", null, Locale.forLanguageTag("en"))
		);
		assertEquals(
				"Postleitzahl",
				messageSource.getMessage("messages.postcode", null, Locale.forLanguageTag("de"))
		);
		assertEquals(
				"Ablaufdatum",
				messageSource.getMessage("payment.expiry_date", null, Locale.forLanguageTag("de"))
		);
	}

	@Test
	void test_defaultDomain() {
		var messageSource = XliffResourceMessageSource
				.builder(Locale.forLanguageTag("en"), new LocationPattern("translations/*"))
				.defaultDomain("payment")
				.build();

		assertEquals("Expiry date", messageSource.getMessage(
				"expiry_date",
				null,
				Locale.forLanguageTag("en-US")
		));
	}

	@Test
	void test_fileExtensions() {
		var messageSource = XliffResourceMessageSource
				.builder(Locale.forLanguageTag("en"), new LocationPattern("translations/*"))
				.fileExtensions(List.of("xlf"))
				.build();

		var locale = Locale.forLanguageTag("en");
		assertThrows(NoSuchMessageException.class, () -> messageSource.getMessage(
				"postcode",
				null,
				locale
		));
	}

	@Test
	void test_validateSchema_enabled_throws() {
		var builder = XliffResourceMessageSource
				.builder(Locale.forLanguageTag("en"), new LocationPattern("fixtures/schemainvalid.xliff"))
				.validateSchema(true);

		assertThrows(XliffMessageSourceValidationException.class, builder::build);
	}

	@Test
	void test_validateSchema_disabledByDefault() {
		var messageSource = XliffResourceMessageSource
				.builder(Locale.forLanguageTag("en"), new LocationPattern("fixtures/schemainvalid.xliff"))
				.defaultDomain("schemainvalid")
				.build();

		assertEquals("Target", messageSource.getMessage(
				"novalid",
				null,
				Locale.forLanguageTag("en")
		));
	}
}
