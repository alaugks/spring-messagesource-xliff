// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.alaugks.spring.messagesource.catalog.resources.LocationPattern;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceValidationException;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.context.NoSuchMessageException;

class XliffResourceMessageSourceTest {

	@Test
	void test_get_message() {
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

		assertEquals("There are 1,000 files.", messageSource.getMessage(
			"format_choice",
			new Object[]{1000L},
			Locale.forLanguageTag("en")
		));

		assertEquals("There are 1,000 files.", messageSource.getMessage(
			"format_choice",
			new Object[]{1000L},
			Locale.forLanguageTag("en-US")
		));

		assertEquals("Es gibt 1.000 Dateien.", messageSource.getMessage(
			"format_choice",
			new Object[]{1000L},
			Locale.forLanguageTag("de")
		));

		assertEquals(
			"Expiry date",
			messageSource.getMessage("payment.expiry_date", null, Locale.forLanguageTag("en"))
		);
		assertEquals(
			"Expiration date",
			messageSource.getMessage("payment.expiry_date", null, Locale.forLanguageTag("en-US"))
		);
		assertEquals(
			"Ablaufdatum",
			messageSource.getMessage("payment.expiry_date", null, Locale.forLanguageTag("de"))
		);
	}

	@Test
	void test_builder_with_location_patterns() {
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
	void test_default_domain() {
		var messageSource = XliffResourceMessageSource
				.builder(Locale.forLanguageTag("en"), new LocationPattern("translations/*"))
				.defaultDomain("payment")
				.build();

		assertEquals("Expiration date", messageSource.getMessage(
				"expiry_date",
				null,
				Locale.forLanguageTag("en-US")
		));
	}

	@Test
	void test_file_extensions() {
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
	void test_validate_schema_enabled_throws() {
		var builder = XliffResourceMessageSource
				.builder(Locale.forLanguageTag("en"), new LocationPattern("fixtures/schemainvalid.xliff"))
				.validateSchema(true);

		assertThrows(XliffMessageSourceValidationException.class, builder::build);
	}

	@Test
	void test_validate_schema_disabled_by_default() {
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
