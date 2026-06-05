// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

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
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XliffCatalog extends AbstractCatalog {

	private final List<TranslationFile> translationFiles;

	private final boolean validateSchema;

	private final XliffSchemaValidator schemaValidator = new XliffSchemaValidator();

	/**
	 * Creates a new catalog that lazily parses the given XLIFF translation
	 * files.
	 *
	 * @param translationFiles XLIFF files to parse on first access.
	 * @param validateSchema   whether each document is validated against its
	 *                         OASIS XSD schema before its units are extracted.
	 */
	public XliffCatalog(List<TranslationFile> translationFiles, boolean validateSchema) {
		this.translationFiles = translationFiles;
		this.validateSchema = validateSchema;
	}

	/**
	 * Returns the translation units parsed from all configured XLIFF files.
	 * <p>The result is parsed on the first invocation and cached for subsequent
	 * calls.
	 *
	 * @return list of all translation units across the configured files; never
	 *         {@code null}.
	 * @throws FatalError                                if the XML parser cannot
	 *                                                   be configured or a file
	 *                                                   cannot be read.
	 * @throws XliffMessageSourceRuntimeException        if SAX-level parsing
	 *                                                   fails for one of the
	 *                                                   files.
	 * @throws XliffMessageSourceVersionSupportException if a file declares an
	 *                                                   unsupported XLIFF
	 *                                                   version.
	 */
	@Override
	public List<TransUnitInterface> getTransUnits() {
		try {
			return this.parseXliffDocuments(this.translationFiles);
		}
		catch (ParserConfigurationException | IOException e) {
			throw new FatalError(e);
		}
	}

	/**
	 * Parses the XLIFF files into a flat list of translation units, using a
	 * namespace-aware, XXE-hardened parser.
	 */
	private List<TransUnitInterface> parseXliffDocuments(List<TranslationFile> xliffFiles)
		throws ParserConfigurationException, IOException {

		List<TransUnitInterface> transUnits = new ArrayList<>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
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

			String version = XliffDocument.readVersion(root);
			if (version == null) {
				continue;
			}

			if (this.validateSchema) {
				this.schemaValidator.validate(document, version);
			}

			Map<String, String> units = switch (version) {
				case "1.2" -> new Xliff12Document(root).getUnits();
				case "2.0", "2.1" -> new Xliff2xDocument(root).getUnits();
				default -> throw new XliffMessageSourceVersionSupportException(
						String.format(
								"XLIFF version \"%s\" not supported. Supported versions: 1.2, 2.0 and 2.1",
								version
						)
				);
			};

			units.forEach((code, value) -> transUnits.add(
					new TransUnit(
							xliffFile.locale(),
							code,
							value,
							xliffFile.domain()
					)
			));
		}

		return transUnits;
	}
}
