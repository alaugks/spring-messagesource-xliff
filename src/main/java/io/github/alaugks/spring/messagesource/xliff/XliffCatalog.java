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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import io.github.alaugks.spring.messagesource.catalog.catalog.AbstractCatalog;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import io.github.alaugks.spring.messagesource.catalog.records.TranslationFile;
import io.github.alaugks.spring.messagesource.xliff.exception.SaxErrorHandler;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException.FatalError;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceVersionSupportException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XliffCatalog extends AbstractCatalog {

	private final List<TransUnitInterface> transUnits;

	private final List<XliffIdentifierInterface> identifiers;

	private final List<TranslationFile> translationFiles;

	List<XliffVersionInterface> supportedVersions = List.of(
			new XliffVersion12(),
			new XliffVersion2x()
	);

	public XliffCatalog(
			List<TranslationFile> translationFiles,
			List<XliffIdentifierInterface> identifiers
	) {
		this.translationFiles = translationFiles;
		this.transUnits = new ArrayList<>();
		this.identifiers = identifiers == null ? List.of() : identifiers;
	}

	@Override
	public List<TransUnitInterface> getTransUnits() {
		return this.transUnits;
	}

	@Override
	public void build() {
		try {
			this.parseXliffDocuments(this.translationFiles);
		}
		catch (ParserConfigurationException | IOException e) {
			throw new FatalError(e);
		}
	}

	private void parseXliffDocuments(List<TranslationFile> xliffFiles)
			throws ParserConfigurationException, IOException {

		for (TranslationFile xliffFile : xliffFiles) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			documentBuilder.setErrorHandler(new SaxErrorHandler());
			Document document;
			try {
				document = documentBuilder.parse(xliffFile.inputStream());
			}
			catch (SAXException e) {
				throw new XliffMessageSourceRuntimeException(e);
			}

			Element root = document.getDocumentElement();
			XliffDocument xliffDocument = new XliffDocument(root);

			var version = xliffDocument.getXliffVersion();

			XliffVersionInterface xliffVersion = this.supportedVersions
					.stream()
					.filter(o -> o.support(version))
					.findFirst()
					.orElse(null);

			if (xliffVersion != null) {
				xliffDocument.getTransUnitsMap(
						xliffVersion.getTransUnitName(),
						this.resolveIdentifiers(this.identifiers, xliffVersion).list()
				).forEach((code, value) -> this.transUnits.add(
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
	}

	public XliffIdentifierInterface resolveIdentifiers(
			List<XliffIdentifierInterface> identifiers,
			XliffVersionInterface xliffVersionObject
	) {
		return identifiers
				.stream()
				.filter(u -> u.getClass() == xliffVersionObject.getDefaultIdentifier().getClass())
				.findFirst()
				.orElse(xliffVersionObject.getDefaultIdentifier());
	}

	public static final class XliffVersion12 implements XliffVersionInterface {

		@Override
		public boolean support(String version) {
			return version.equals("1.2");
		}

		@Override
		public String getTransUnitName() {
			return "trans-unit";
		}

		@Override
		public XliffIdentifierInterface getDefaultIdentifier() {
			return new Xliff12Identifier(List.of());
		}
	}

	public static final class XliffVersion2x implements XliffVersionInterface {

		@Override
		public boolean support(String version) {
			return List.of("2.0", "2.1").contains(version);
		}

		@Override
		public String getTransUnitName() {
			return "segment";
		}

		@Override
		public XliffIdentifierInterface getDefaultIdentifier() {
			return new Xliff2xIdentifier(List.of());
		}
	}

	public interface XliffVersionInterface {

		boolean support(String version);

		String getTransUnitName();

		XliffIdentifierInterface getDefaultIdentifier();
	}

	record Xliff12Identifier(List<String> list) implements XliffIdentifierInterface {

	}

	record Xliff2xIdentifier(List<String> list) implements XliffIdentifierInterface {

	}

	public interface XliffIdentifierInterface {

		List<String> list();
	}
}
