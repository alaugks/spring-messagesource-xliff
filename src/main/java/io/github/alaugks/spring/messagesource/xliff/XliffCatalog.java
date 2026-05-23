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
import java.util.stream.Collectors;
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

	/**
	 * Creates a new catalog that lazily parses the given XLIFF translation
	 * files.
	 *
	 * @param translationFiles XLIFF files to parse on first access.
	 * @param identifiers      optional list of identifier strategies that
	 *                         override the per-version defaults; may be
	 *                         {@code null}, in which case the defaults are used.
	 */
	public XliffCatalog(
			List<TranslationFile> translationFiles,
			List<XliffIdentifier> identifiers
	) {
		this.translationFiles = translationFiles;
		this.identifiers = identifiers == null ? List.of() : identifiers;
	}

	/**
	 * Returns the translation units parsed from all configured XLIFF files.
	 * <p>The result is parsed on the first invocation and cached for subsequent
	 * calls.
	 *
	 * @return list of all translation units across the configured files; never
	 *         {@code null}.
	 * @throws FatalError                              if the XML parser cannot
	 *                                                 be configured or a file
	 *                                                 cannot be read.
	 * @throws XliffMessageSourceRuntimeException      if SAX-level parsing
	 *                                                 fails for one of the
	 *                                                 files.
	 * @throws XliffMessageSourceVersionSupportException if a file declares an
	 *                                                 unsupported XLIFF
	 *                                                 version.
	 */
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

	/**
	 * Parses the given XLIFF files into a flat list of translation units.
	 * <p>The parser is hardened against XXE attacks by enabling
	 * {@link XMLConstants#FEATURE_SECURE_PROCESSING} and disabling DOCTYPE
	 * declarations.
	 *
	 * @param xliffFiles the XLIFF files to parse.
	 * @return list of translation units extracted from the files.
	 * @throws ParserConfigurationException             if the underlying parser
	 *                                                  cannot be configured.
	 * @throws IOException                              if a file cannot be
	 *                                                  read.
	 * @throws XliffMessageSourceRuntimeException       if a file is not valid
	 *                                                  XML.
	 * @throws XliffMessageSourceVersionSupportException if a file declares an
	 *                                                  unsupported XLIFF
	 *                                                  version.
	 */
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
						String.format(
							"XLIFF version \"%s\" not supported. Supported versions: 1.2, 2.0 and 2.1",
							version
						)
				);
			}
		}

		return transUnits;
	}

	/**
	 * Picks the user-supplied {@link XliffIdentifier} matching the given XLIFF
	 * version, falling back to the version's default if none was configured.
	 *
	 * @param identifiers         user-supplied identifier strategies.
	 * @param xliffVersionObject  the XLIFF version whose identifier strategy
	 *                            is being resolved.
	 * @return the matching identifier, or the version default if none is
	 *         configured.
	 */
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

		/**
		 * Reports whether this implementation supports XLIFF version
		 * {@code 1.2}.
		 *
		 * @param version the version string declared by the document.
		 * @return {@code true} if {@code version} equals {@code "1.2"}.
		 */
		@Override
		public boolean support(String version) {
			return version.equals("1.2");
		}

		/**
		 * Returns the trans-unit element name used by XLIFF 1.2.
		 *
		 * @return the literal {@code "trans-unit"}.
		 */
		@Override
		public String getTransUnitName() {
			return "trans-unit";
		}

		/**
		 * Returns the default identifier strategy for XLIFF 1.2.
		 *
		 * @return an empty {@link Xliff12Identifier}.
		 */
		@Override
		public XliffIdentifier getDefaultIdentifier() {
			return new Xliff12Identifier(List.of());
		}
	}

	public static final class XliffVersion2x implements XliffVersion {

		/**
		 * Reports whether this implementation supports the given XLIFF 2.x
		 * version.
		 *
		 * @param version the version string declared by the document.
		 * @return {@code true} if {@code version} is {@code "2.0"} or
		 *         {@code "2.1"}.
		 */
		@Override
		public boolean support(String version) {
			return List.of("2.0", "2.1").contains(version);
		}

		/**
		 * Returns the trans-unit element name used by XLIFF 2.x.
		 *
		 * @return the literal {@code "segment"}.
		 */
		@Override
		public String getTransUnitName() {
			return "segment";
		}

		/**
		 * Returns the default identifier strategy for XLIFF 2.x.
		 *
		 * @return an empty {@link Xliff2xIdentifier}.
		 */
		@Override
		public XliffIdentifier getDefaultIdentifier() {
			return new Xliff2xIdentifier(List.of());
		}
	}

	public interface XliffVersion {

		/**
		 * Reports whether this implementation supports the given XLIFF version
		 * string declared by a document.
		 *
		 * @param version the version string (e.g. {@code "1.2"}, {@code "2.0"}).
		 * @return {@code true} if the version is handled by this implementation.
		 */
		boolean support(String version);

		/**
		 * Returns the name of the XML element that carries a translation unit
		 * for this XLIFF version.
		 *
		 * @return the trans-unit element name.
		 */
		String getTransUnitName();

		/**
		 * Returns the default identifier strategy for this XLIFF version,
		 * used when the user did not configure a custom one.
		 *
		 * @return the default identifier instance.
		 */
		XliffIdentifier getDefaultIdentifier();
	}

	public record Xliff12Identifier(List<String> attributes) implements XliffIdentifier {

	}

	public record Xliff2xIdentifier(List<String> attributes) implements XliffIdentifier {

	}

	public interface XliffIdentifier {

		/**
		 * Returns the ordered list of attribute names probed on each
		 * trans-unit element to resolve its identifier.
		 *
		 * @return list of attribute names; never {@code null}.
		 */
		List<String> attributes();
	}
}
