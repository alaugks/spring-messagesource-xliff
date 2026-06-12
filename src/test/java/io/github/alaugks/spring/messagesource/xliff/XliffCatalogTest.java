// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.alaugks.spring.messagesource.catalog.resources.LocationPattern;
import io.github.alaugks.spring.messagesource.catalog.resources.ResourcesLoader;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException.FatalError;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceValidationException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceVersionSupportException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class XliffCatalogTest {

	@Test
	void test_get_trans_units() {

		var ressourceLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new LocationPattern(List.of("translations/messages.xliff", "translations/messages_de.xliff")),
				List.of("xlf", "xliff")
		);

		var catalog = new XliffCatalog(ressourceLoader.getTranslationFiles(), true);
		var transUnits = catalog.getTransUnits();

		assertThat(TestHelper.findInTransUnits(transUnits, "en", "postcode")).isEqualTo("Postcode");
		assertThat(TestHelper.findInTransUnits(transUnits, "de", "postcode")).isEqualTo("Postleitzahl");
	}

	@Test
	void test_parse_error() {
		var xliffCatalog = TestHelper.getXliffCatalog(
				new LocationPattern(List.of("fixtures/parse_error.xliff")),
				Locale.forLanguageTag("en")
		);

		assertThatThrownBy(xliffCatalog::getTransUnits).isInstanceOf(XliffMessageSourceSAXParseException.class);
		assertThatThrownBy(xliffCatalog::getTransUnits).isInstanceOf(FatalError.class);
	}

	@Test
	void test_no_xliff_document() {

		var ressourceLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new LocationPattern(List.of("fixtures/no-xliff.xml")),
				List.of("xml")
		);

		var transUnits = new XliffCatalog(
				ressourceLoader.getTranslationFiles(),
				true
		).getTransUnits();

		assertThat(transUnits).isEqualTo(List.of());
	}

	@Test
	void test_version_not_supported() {
		var xliffCatalog = TestHelper.getXliffCatalog(
				new LocationPattern(List.of("fixtures/xliff10.xliff")),
				Locale.forLanguageTag("en"),
				false
		);

		assertThatThrownBy(
				xliffCatalog::getTransUnits
		)
		.isInstanceOf(XliffMessageSourceVersionSupportException.class)
		.hasMessage(
				"XLIFF version \"1.0\" not supported. Supported versions: 1.2, 2.0, 2.1 and 2.2"
		);
	}

	@ParameterizedTest
	@MethodSource("dataProvider_loadVersions")
	void test_version_supported(String ressourcePath, String expected) {
		var catalog = TestHelper.getXliffCatalog(
				new LocationPattern(List.of(ressourcePath)),
				Locale.forLanguageTag("en")
		);

		assertThat(TestHelper.findInTransUnits(catalog.getTransUnits(), "en", "code-1")).isEqualTo(expected);
	}

	private static Stream<Arguments> dataProvider_loadVersions() {
		return Stream.of(
				Arguments.of("fixtures/xliff12.xliff", "Postcode (Xliff Version 1.2)"),
				Arguments.of("fixtures/xliff20.xliff", "Postcode (Xliff Version 2.0)"),
				Arguments.of("fixtures/xliff21.xliff", "Postcode (Xliff Version 2.1)")
		);
	}

	@Test
	void test_schema_validation_invalid_throws() {
		var xliffCatalog = TestHelper.getXliffCatalog(
				new LocationPattern(List.of("fixtures/schemainvalid.xliff")),
				Locale.forLanguageTag("en")
		);

		assertThatThrownBy(xliffCatalog::getTransUnits).isInstanceOf(XliffMessageSourceValidationException.class);
	}

	@Test
	void test_schema_validation_disabled_skips_validation() {
		var ressourceLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new LocationPattern(List.of("fixtures/schemainvalid.xliff")),
				List.of("xlf", "xliff")
		);

		var transUnits = new XliffCatalog(ressourceLoader.getTranslationFiles(), false).getTransUnits();

		assertThat(TestHelper.findInTransUnits(transUnits, "en", "novalid")).isEqualTo("Target");
	}

	@ParameterizedTest
	@MethodSource("dataProvider_standardCompliantFixtures")
	void test_fixtures_are_schema_valid(String resourcePath) {
		var catalog = TestHelper.getXliffCatalog(
				new LocationPattern(List.of(resourcePath)),
				Locale.forLanguageTag("en")
		);

		assertThatCode(catalog::getTransUnits).doesNotThrowAnyException();
	}

	private static Stream<Arguments> dataProvider_standardCompliantFixtures() {
		return Stream.of(
				Arguments.of("fixtures/xliff12.xliff"),
				Arguments.of("fixtures/xliff20.xliff"),
				Arguments.of("fixtures/xliff21.xliff"),
				Arguments.of("translations/messages.xliff"),
				Arguments.of("translations/messages_de.xliff"),
				Arguments.of("translations/messages_en_US.xliff"),
				Arguments.of("translations/payment.xlf"),
				Arguments.of("translations/payment_de.xlf"),
				Arguments.of("translations_en/messages.xliff"),
				Arguments.of("translations_en/payment.xlf"),
				Arguments.of("translations_de/messages_de.xliff"),
				Arguments.of("translations_de/payment_de.xlf"),
				Arguments.of("translations_en_US/messages_en_US.xliff")
		);
	}

}
