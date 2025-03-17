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

import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import io.github.alaugks.spring.messagesource.catalog.resources.LocationPattern;
import io.github.alaugks.spring.messagesource.catalog.resources.ResourcesLoader;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.Xliff12Identifier;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.Xliff2xIdentifier;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException.FatalError;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceVersionSupportException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XliffCatalogTest {

	@Test
	void test_getTransUnits() {

		var ressourceLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new LocationPattern(List.of("translations/messages.xliff", "translations/messages_de.xliff")),
				List.of("xlf", "xliff")
		);

		var catalog = new XliffCatalog(
				ressourceLoader.getTranslationFiles(),
				List.of(
						new Xliff12Identifier(List.of("resname", "id")),
						new Xliff2xIdentifier(List.of("id"))
				)
		);
		catalog.build();
		var transUnits = catalog.getTransUnits();

		assertEquals("Postcode", this.findInTransUnits(transUnits, "en", "postcode"));
		assertEquals("Postleitzahl", this.findInTransUnits(transUnits, "de", "postcode"));
	}

	@Test
	void test_parseError() {
		var xliffCatalogBuilder = this.getXliffCatalogBuilder(
				new LocationPattern(List.of("fixtures/parse_error.xliff")),
				Locale.forLanguageTag("en")
		);

		assertThrows(
				XliffMessageSourceSAXParseException.class, xliffCatalogBuilder::build
		);

		assertThrows(
				FatalError.class, xliffCatalogBuilder::build
		);
	}

	@Test
	void test_noXliffDocument() {

		var ressourceLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new LocationPattern(List.of("fixtures/no-xliff.xml")),
				List.of("xml")
		);

		var transUnits = new XliffCatalog(
				ressourceLoader.getTranslationFiles(),
				null
		).getTransUnits();

		assertEquals(List.of(), transUnits);
	}

	@Test
	void test_versionNotSupported() {
		var xliffCatalogBuilder = this.getXliffCatalogBuilder(
				new LocationPattern(List.of("fixtures/xliff10.xliff")),
				Locale.forLanguageTag("en")
		);

		XliffMessageSourceVersionSupportException exception = assertThrows(
				XliffMessageSourceVersionSupportException.class, xliffCatalogBuilder::build
		);
		assertEquals("XLIFF version \"1.0\" not supported.", exception.getMessage());
	}


	@ParameterizedTest
	@MethodSource("dataProvider_loadVersions")
	void test_versionSupported(String ressourcePath, String domain, String expected) {
		var catalog = this.getXliffCatalogBuilder(
				new LocationPattern(List.of(ressourcePath)),
				Locale.forLanguageTag("en")
		);
		catalog.build();

		assertEquals(
				expected,
				this.findInTransUnits(catalog.getTransUnits(), "en", "code-1")
		);
	}

	private static Stream<Arguments> dataProvider_loadVersions() {
		return Stream.of(
				Arguments.of("fixtures/xliff12.xliff", "xliff12", "Postcode (Xliff Version 1.2)"),
				Arguments.of("fixtures/xliff20.xliff", "xliff20", "Postcode (Xliff Version 2.0)"),
				Arguments.of("fixtures/xliff21.xliff", "xliff21", "Postcode (Xliff Version 2.1)")
		);
	}

	private XliffCatalog getXliffCatalogBuilder(LocationPattern files, Locale locale) {

		var ressourceLoader = new ResourcesLoader(locale, files, List.of("xlf", "xliff"));

		return new XliffCatalog(
				ressourceLoader.getTranslationFiles(),
				List.of(
						new Xliff12Identifier(List.of("resname", "id")),
						new Xliff2xIdentifier(List.of("id"))
				)
		);
	}

	private String findInTransUnits(List<TransUnitInterface> transUnits, String locale, String code) {
		return transUnits
				.stream()
				.filter(t -> t.locale().toString().equals(locale) && t.code().equals(code))
				.findFirst()
				.get().value();
	}
}
