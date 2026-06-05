// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceValidationException;
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
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Validates XLIFF documents against the bundled OASIS XSD schemas.
 *
 * <p>XLIFF 1.2 is validated against {@code xliff-core-1.2-transitional.xsd},
 * and XLIFF 2.0/2.1 against {@code xliff-core-2.0.xsd} (2.1 documents reuse the
 * 2.0 core namespace and schema). All schemas, including the {@code xml.xsd}
 * imported by the core schemas, are read only from the package; external access
 * is disabled so nothing is ever fetched from outside the classpath.
 */
final class XliffSchemaValidator {

	private static final String SCHEMA_PATH = "schema/";

	private static final Map<String, String> SCHEMA_BY_VERSION = Map.of(
			"1.2", "xliff-core-1.2-transitional.xsd",
			"2.0", "xliff-core-2.0.xsd",
			"2.1", "xliff-core-2.0.xsd"
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
		String schemaResource = SCHEMA_BY_VERSION.get(version);
		if (schemaResource == null) {
			throw new XliffMessageSourceValidationException("No schema available for version '" + version + "'");
		}

		Schema schema = this.schemaCache.computeIfAbsent(schemaResource, this::loadSchema);

		CollectingErrorHandler errorHandler = new CollectingErrorHandler();
		Validator validator = schema.newValidator();
		validator.setErrorHandler(errorHandler);
		try {
			validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			validator.validate(new DOMSource(document));
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
	 * Loads and compiles the bundled core schema together with the bundled
	 * xml.xsd, with external access disabled.
	 */
	private Schema loadSchema(String schemaResource) {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try (InputStream xmlSchema = openSchema("xml.xsd"); InputStream coreSchema = openSchema(schemaResource)) {
			// Disable any external access and supply the bundled xml.xsd together
			// with the core schema, so the schemas are read only from the package.
			factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			return factory.newSchema(new Source[] {
					new StreamSource(xmlSchema),
					new StreamSource(coreSchema)
			});
		}
		catch (SAXException | IOException e) {
			throw new XliffMessageSourceValidationException(
					String.format("Unable to load XLIFF schema \"%s\": %s", schemaResource, e.getMessage())
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
