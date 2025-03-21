/*
 * Copyright 2023-2025 André Laugks <alaugks@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.alaugks.spring.messagesource.xliff;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.Xliff12Identifier;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.Xliff2xIdentifier;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.XliffIdentifierInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.context.NoSuchMessageException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XliffResourceMessageSourceTest {

	@Test
	void test_getMessage_code_args_locale() {
		var messageSource = XliffResourceMessageSource
				.builder(Locale.forLanguageTag("en"), "translations/*")
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
	}

	@Test
	void test_builder_withLocationPatterns() {
		var messageSource = XliffResourceMessageSource
				.builder(
						Locale.forLanguageTag("en"),
						List.of(
								"translations_en/*",
								"translations_de/*"
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
				.builder(Locale.forLanguageTag("en"), "translations/*")
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
				.builder(Locale.forLanguageTag("en"), "translations/*")
				.fileExtensions(List.of("xlf"))
				.build();

		var locale = Locale.forLanguageTag("en");
		assertThrows(NoSuchMessageException.class, () -> messageSource.getMessage(
				"postcode",
				null,
				locale
		));
	}

	@ParameterizedTest()
	@MethodSource("dataProvider_setTranslationUnitIdentifiersOrdering")
	void test_identifier(
			List<XliffIdentifierInterface> translationUnitIdentifiers,
			String code,
			String expected
	) {
		var messageSource = XliffResourceMessageSource
				.builder(
						Locale.forLanguageTag("en"),
						List.of(
								"fixtures/identifierxliff12.xliff",
								"fixtures/identifierxliff2x.xliff"
						)
				)
				.identifier(translationUnitIdentifiers)
				.build();

		String message = messageSource.getMessage(
				code,
				null,
				Locale.forLanguageTag("en")
		);
		assertEquals(expected, message);
	}

	private static Stream<Arguments> dataProvider_setTranslationUnitIdentifiersOrdering() {
		return Stream.of(
				Arguments.of(
						List.of(
								new Xliff12Identifier(List.of("resname"))
						),
						"identifierxliff12.code-resname-a",
						"Target A"
				),
				Arguments.of(
						List.of(
								new Xliff12Identifier(List.of("id"))
						),
						"identifierxliff12.code-id-a",
						"Target A"
				),

				Arguments.of(
						List.of(
								new Xliff12Identifier(List.of("resname", "id"))
						),
						"identifierxliff12.code-id-b",
						"Target B"
				),
				Arguments.of(
						List.of(
								new Xliff12Identifier(List.of("id", "resname"))
						),
						"identifierxliff12.code-resname-c",
						"Target C"
				),

				Arguments.of(
						List.of(
								new Xliff2xIdentifier(List.of("resname"))
						),
						"identifierxliff2x.code-resname-a",
						"Target A"
				),
				Arguments.of(
						List.of(
								new Xliff2xIdentifier(List.of("id"))
						),
						"identifierxliff2x.code-id-a",
						"Target A"
				),

				Arguments.of(
						List.of(
								new Xliff2xIdentifier(List.of("resname", "id"))
						),
						"identifierxliff2x.code-id-b",
						"Target B"
				),
				Arguments.of(
						List.of(
								new Xliff2xIdentifier(List.of("id", "resname"))
						),
						"identifierxliff2x.code-resname-c",
						"Target C"
				)
		);
	}

}
