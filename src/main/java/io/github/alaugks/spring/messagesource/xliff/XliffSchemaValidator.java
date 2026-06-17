// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceValidationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Validates XLIFF documents against the bundled OASIS XSD schemas.
 *
 * <p>XLIFF 1.2 is validated against {@code xliff-core-1.2-transitional.xsd},
 * XLIFF 2.0/2.1 against {@code xliff-core-2.0.xsd} (2.1 documents reuse the
 * 2.0 core namespace and schema), and XLIFF 2.2 against
 * {@code xliff_core_2.2.xsd} together with the {@code metadata.xsd} module it
 * imports. All schemas, including the {@code xml.xsd} imported by the core
 * schemas, are read only from the package; external access is disabled so
 * nothing is ever fetched from outside the classpath.
 *
 * <p>The Plural, Gender, and Select (PGS) module attributes are an extension of
 * this library, not part of the OASIS core schema, which declares no wildcard
 * for module attributes on every element. They are therefore stripped from a
 * copy of the document before XSD validation so the core schema stays the
 * unmodified OASIS schema; the original document keeps its PGS attributes for
 * the reader.
 */
final class XliffSchemaValidator {

	private static final String SCHEMA_PATH = "schema/";

	/** Namespace of the XLIFF 2.2 Plural, Gender, and Select (PGS) Module. */
	private static final String PGS_NS = "urn:oasis:names:tc:xliff:pgs:1.0";

	private static final Map<String, List<String>> SCHEMA_BY_VERSION = Map.of(
			"1.2", List.of("xliff-core-1.2-transitional.xsd"),
			"2.0", List.of("xliff-core-2.0.xsd"),
			"2.1", List.of("xliff-core-2.0.xsd"),
			"2.2", List.of("metadata.xsd", "xliff_core_2.2.xsd")
	);

	private final Map<String, Schema> schemaCache = new ConcurrentHashMap<>();

	/**
	 * Validates the given XLIFF document against the schema for its version.
	 *
	 * @param document the parsed XLIFF document.
	 * @param version  the resolved XLIFF version (e.g. {@code "1.2"}).
	 * @throws XliffMessageSourceValidationException if no schema is bundled for
	 *                                               the version, or if the
	 *                                               document violates the schema.
	 */
	void validate(Document document, String version) {
		List<String> schemaResources = SCHEMA_BY_VERSION.get(version);
		if (schemaResources == null) {
			throw new XliffMessageSourceValidationException(
					String.format("No schema available for version \"%s\"", version)
			);
		}

		Schema schema = this.schemaCache.computeIfAbsent(version, key -> this.loadSchema(schemaResources));

		CollectingErrorHandler errorHandler = new CollectingErrorHandler();
		Validator validator = schema.newValidator();
		validator.setErrorHandler(errorHandler);
		try {
			validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			validator.validate(new DOMSource(withoutPgsAttributes(document)));
		}
		catch (SAXException | IOException e) {
			throw new XliffMessageSourceValidationException(e.getMessage());
		}

		if (!errorHandler.errors.isEmpty()) {
			throw new XliffMessageSourceValidationException(
					String.join("", errorHandler.errors)
			);
		}
	}

	/**
	 * Returns a deep copy of the document with every PGS module attribute removed, so the document
	 * validates against the unmodified OASIS core schema. The original document is left untouched and
	 * keeps its PGS attributes for the reader. Documents without PGS attributes are returned as is.
	 */
	private static Document withoutPgsAttributes(Document document) {
		Document copy = (Document) document.cloneNode(true);
		NodeList elements = copy.getElementsByTagName("*");
		for (int i = 0; i < elements.getLength(); i++) {
			Element element = (Element) elements.item(i);
			NamedNodeMap attributes = element.getAttributes();
			List<Attr> pgsAttributes = new ArrayList<>();
			for (int j = 0; j < attributes.getLength(); j++) {
				Attr attribute = (Attr) attributes.item(j);
				if (PGS_NS.equals(attribute.getNamespaceURI())) {
					pgsAttributes.add(attribute);
				}
			}
			for (Attr pgsAttribute : pgsAttributes) {
				element.removeAttributeNode(pgsAttribute);
			}
		}
		return copy;
	}

	/** Loads and compiles all bundled schemas for the version (xml.xsd plus the core and any module it imports), with external access disabled. */
	private Schema loadSchema(List<String> schemaResources) {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			// Disable any external access and supply every schema from the package, so
			// imports are resolved only against the bundled sources (xml.xsd plus any
			// module the core imports) and nothing is fetched from outside the package.
			factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

			List<Source> sources = new ArrayList<>();
			sources.add(readSchema("xml.xsd"));
			for (String schemaResource : schemaResources) {
				sources.add(readSchema(schemaResource));
			}
			return factory.newSchema(sources.toArray(new Source[0]));
		}
		catch (SAXException e) {
			throw new XliffMessageSourceValidationException(
					String.format("Unable to load XLIFF schema %s: %s", schemaResources, e.getMessage())
			);
		}
	}

	/** Reads a bundled schema fully into memory so that no open stream outlives this method. */
	private static Source readSchema(String name) {
		try (InputStream in = openSchema(name)) {
			return new StreamSource(new ByteArrayInputStream(in.readAllBytes()));
		}
		catch (IOException e) {
			throw new XliffMessageSourceValidationException(
					String.format("Unable to read bundled schema \"%s\": %s", name, e.getMessage())
			);
		}
	}

	/**
	 * Opens a bundled schema resource from the package (classpath) by file name.
	 */
	private static InputStream openSchema(String name) {
		InputStream in = XliffSchemaValidator.class.getResourceAsStream(SCHEMA_PATH + name);
		if (in == null) {
			throw new XliffMessageSourceValidationException(
					String.format("Bundled schema resource \"%s%s\" not found.", SCHEMA_PATH, name)
			);
		}
		return in;
	}

	/**
	 * Collects schema violations into human-readable strings instead of
	 * throwing on the first one.
	 */
	private static final class CollectingErrorHandler implements ErrorHandler {

		private final List<String> errors = new ArrayList<>();

		/**
		 * Records a schema warning.
		 *
		 * @param exception the violation reported by the parser.
		 */
		@Override
		public void warning(SAXParseException exception) {
			this.errors.add(format("WARNING", exception));
		}

		/**
		 * Records a schema error.
		 *
		 * @param exception the violation reported by the parser.
		 */
		@Override
		public void error(SAXParseException exception) {
			this.errors.add(format("ERROR", exception));
		}

		/**
		 * Records a fatal schema error.
		 *
		 * @param exception the violation reported by the parser.
		 */
		@Override
		public void fatalError(SAXParseException exception) {
			this.errors.add(format("ERROR", exception));
		}

		/**
		 * Formats a single schema violation as
		 * [LEVEL] message (line L, column C).
		 */
		private static String format(String level, SAXParseException exception) {
			return String.format(
					"[%s] %s (line %d, column %d)%n",
					level,
					exception.getMessage(),
					exception.getLineNumber(),
					exception.getColumnNumber()
			);
		}
	}
}
