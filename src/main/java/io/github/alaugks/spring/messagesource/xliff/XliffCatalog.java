// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.base.records.TransFileInterface;
import io.github.alaugks.spring.messagesource.base.records.TransUnit;
import io.github.alaugks.spring.messagesource.base.records.TransUnitInterface;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException.FatalError;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceVersionSupportException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * The {@code XliffCatalog} class provides support for managing and parsing
 * a collection of XLIFF translation files. It extracts translation units
 * while optionally validating files against their associated XLIFF schemas.
 * The process is optimized for lazy evaluation, parsing the documents
 * and extracting their units only when requested.
 */
class XliffCatalog {

	private final List<TransFileInterface> translationFiles;

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
	public XliffCatalog(List<TransFileInterface> translationFiles, boolean validateSchema) {
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
	private List<TransUnitInterface> parseXliffDocuments(List<TransFileInterface> xliffFiles)
		throws ParserConfigurationException, IOException {

		List<TransUnitInterface> transUnits = new ArrayList<>();

		DocumentBuilderFactory factory = XliffDocumentParser.newDocumentBuilderFactory();

		for (TransFileInterface xliffFile : xliffFiles) {
			Element root;
			try {
				root = XliffDocumentParser.parseRootElement(factory, Objects.requireNonNull(xliffFile.content()));
			}
			catch (SAXException e) {
				throw new XliffMessageSourceRuntimeException(e);
			}

			String version = XliffDocument.readVersion(root);
			if (version == null) {
				continue;
			}

			if (this.validateSchema) {
				this.schemaValidator.validate(root.getOwnerDocument(), version);
			}

			Map<String, String> units = switch (version) {
				case "1.2" -> new Xliff12Document(root).getUnits();
				case "2.0", "2.1", "2.2" -> new Xliff2xDocument(root).getUnits();
				default -> throw new XliffMessageSourceVersionSupportException(
						String.format(
								"XLIFF version \"%s\" not supported. Supported versions: 1.2, 2.0, 2.1 and 2.2",
								version
						)
				);
			};

			units.forEach((code, value) -> transUnits.add(
					new TransUnit(
							xliffFile.locale(),
							code,
							value
					)
			));
		}

		return transUnits;
	}
}
