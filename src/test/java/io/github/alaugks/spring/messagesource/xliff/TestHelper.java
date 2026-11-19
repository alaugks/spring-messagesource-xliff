/*
 * Copyright André Laugks
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

import io.github.alaugks.spring.messagesource.base.records.TransUnitInterface;
import io.github.alaugks.spring.messagesource.base.resources.ResourceLoaderBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

final class TestHelper {

	private TestHelper() {
	}

	static XliffCatalog getXliffCatalog(List<String> locationPatterns, Locale locale, boolean validateSchema) {
		return new XliffCatalog(ResourceLoaderBuilder.builder(locale, locationPatterns)
			.fileExtensions(List.of("xlf", "xliff"))
			.build()
			.getTranslationFiles(), validateSchema);
	}

	static XliffCatalog getXliffCatalog(List<String> locationPatterns, Locale locale) {
		return getXliffCatalog(locationPatterns, locale, true);
	}

	static String findInTransUnits(List<TransUnitInterface> transUnits, String locale, String code) {
		return transUnits.stream()
			.filter((t) -> t.locale().toString().equals(locale) && t.code().equals(code))
			.findFirst()
			.get()
			.value();
	}

	static Document parseFile(String path, boolean validateSchema) {
		try (InputStream in = TestHelper.class.getClassLoader().getResourceAsStream(path)) {
			return parseDocument(new String(Objects.requireNonNull(in, path).readAllBytes(), StandardCharsets.UTF_8),
					validateSchema);
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	static Document parseDocument(String xml) {
		return parseDocument(xml, false);
	}

	static Document parseDocument(String xml, boolean validateSchema) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			Document document = factory.newDocumentBuilder()
				.parse(new ByteArrayInputStream(xml.strip().getBytes(StandardCharsets.UTF_8)));

			if (validateSchema) {
				new XliffSchemaValidator().validate(document, document.getDocumentElement().getAttribute("version"));

			}

			return document;
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

}
