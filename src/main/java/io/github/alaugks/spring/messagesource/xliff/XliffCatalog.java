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

import io.github.alaugks.spring.messagesource.catalog.catalog.AbstractCatalog;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import io.github.alaugks.spring.messagesource.catalog.records.TranslationFile;
import io.github.alaugks.spring.messagesource.xliff.exception.SaxErrorHandler;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException.FatalError;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceVersionSupportException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XliffCatalog extends AbstractCatalog {

	private static final List<XliffVersion> SUPPORTED_VERSIONS = List.of(
			new XliffVersion12(),
			new XliffVersion2x()
	);

	private final List<XliffIdentifier> identifiers;

	private final List<TranslationFile> translationFiles;

	private List<TransUnitInterface> cachedTransUnits;

	public XliffCatalog(
			List<TranslationFile> translationFiles,
			List<XliffIdentifier> identifiers
	) {
		this.translationFiles = translationFiles;
		this.identifiers = identifiers == null ? List.of() : identifiers;
	}

	@Override
	public List<TransUnitInterface> getTransUnits() {
		if (this.cachedTransUnits != null) {
			return this.cachedTransUnits;
		}
		try {
			this.cachedTransUnits = this.parseXliffDocuments(this.translationFiles);
			return this.cachedTransUnits;
		}
		catch (ParserConfigurationException | IOException e) {
			throw new FatalError(e);
		}
	}

	private List<TransUnitInterface> parseXliffDocuments(List<TranslationFile> xliffFiles)
		throws ParserConfigurationException, IOException {

		List<TransUnitInterface> transUnits = new ArrayList<>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

		for (TranslationFile xliffFile : xliffFiles) {
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			documentBuilder.setErrorHandler(new SaxErrorHandler());
			Document document;
			try {
				document = documentBuilder.parse(new ByteArrayInputStream(xliffFile.content()));
			}
			catch (SAXException e) {
				throw new XliffMessageSourceRuntimeException(e);
			}

			Element root = document.getDocumentElement();
			XliffDocument xliffDocument = new XliffDocument(root);

			var version = xliffDocument.getXliffVersion();

			if (version == null) {
				continue;
			}

			XliffVersion xliffVersion = SUPPORTED_VERSIONS
					.stream()
					.filter(o -> o.support(version))
					.findFirst()
					.orElse(null);

			if (xliffVersion != null) {
				xliffDocument.getTransUnitsMap(
						xliffVersion.getTransUnitName(),
						this.resolveIdentifiers(this.identifiers, xliffVersion).attributes()
				).forEach((code, value) -> transUnits.add(
								new TransUnit(
										xliffFile.locale(),
										code,
										value,
										xliffFile.domain()
								)
						)
				);
			}
			else {
				throw new XliffMessageSourceVersionSupportException(
						String.format("XLIFF version \"%s\" not supported.", version)
				);
			}
		}

		return transUnits;
	}

	private XliffIdentifier resolveIdentifiers(
			List<XliffIdentifier> identifiers,
			XliffVersion xliffVersionObject
	) {
		return identifiers
				.stream()
				.filter(u -> u.getClass() == xliffVersionObject.getDefaultIdentifier().getClass())
				.findFirst()
				.orElse(xliffVersionObject.getDefaultIdentifier());
	}

	public static final class XliffVersion12 implements XliffVersion {

		@Override
		public boolean support(String version) {
			return version.equals("1.2");
		}

		@Override
		public String getTransUnitName() {
			return "trans-unit";
		}

		@Override
		public XliffIdentifier getDefaultIdentifier() {
			return new Xliff12Identifier(List.of());
		}
	}

	public static final class XliffVersion2x implements XliffVersion {

		@Override
		public boolean support(String version) {
			return List.of("2.0", "2.1").contains(version);
		}

		@Override
		public String getTransUnitName() {
			return "segment";
		}

		@Override
		public XliffIdentifier getDefaultIdentifier() {
			return new Xliff2xIdentifier(List.of());
		}
	}

	public interface XliffVersion {

		boolean support(String version);

		String getTransUnitName();

		XliffIdentifier getDefaultIdentifier();
	}

	public record Xliff12Identifier(List<String> attributes) implements XliffIdentifier {

	}

	public record Xliff2xIdentifier(List<String> attributes) implements XliffIdentifier {

	}

	public interface XliffIdentifier {

		List<String> attributes();
	}
}
