// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceValidationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Validates XLIFF documents against the bundled OASIS schemas.
 *
 * <p>Every version is validated through one engine, Jing
 * ({@link ValidationDriver}). XLIFF 1.2 is validated against
 * {@code xliff-core-1.2-transitional.xsd} and XLIFF 2.0/2.1 against
 * {@code xliff-core-2.0.xsd} (2.1 documents reuse the 2.0 core namespace and
 * schema), both as plain XSD.
 *
 * <p>XLIFF 2.2 is validated via NVDL (ISO/IEC 19757-4) using
 * {@code xliff_2.2_validation.nvdl}: NVDL dispatches the Plural, Gender, and
 * Select (PGS) module attributes away from the core schema before XSD
 * validation, which is necessary because the OASIS core schema does not declare
 * a wildcard for module attributes on every element. The core schemas therefore
 * stay the unmodified OASIS schemas. All schemas, including the {@code xml.xsd}
 * imported by every core schema, are read only from the package; nothing is ever
 * fetched from outside the classpath.
 */
final class XliffSchemaValidator {

	private static final String SCHEMA_PATH = "schema/";

	private static final Map<String, String> SCHEMA_BY_VERSION = Map.of(
			"1.2", "xliff-core-1.2-transitional.xsd",
			"2.0", "xliff-core-2.0.xsd",
			"2.1", "xliff-core-2.0.xsd",
			"2.2", "xliff_2.2_validation.nvdl"
	);

	/**
	 * Validates the given XLIFF document against the schema for its version with Jing.
	 *
	 * <p>The bundled schema resource is either a core XSD (1.2/2.0/2.1) or, for 2.2, the NVDL that
	 * dispatches the PGS module attributes away from the core XSD; the {@link AutoSchemaReader} picks
	 * the schema language from the resource. Schema references resolve relative to the resource's
	 * classpath location; the {@code xml.xsd} that every core schema imports is served from the package
	 * via the entity resolver, so nothing is fetched from outside the classpath.
	 *
	 * @param document the parsed XLIFF document.
	 * @param version  the resolved XLIFF version (e.g. {@code "1.2"}).
	 * @throws XliffMessageSourceValidationException if no schema is bundled for
	 *                                               the version, or if the
	 *                                               document violates the schema.
	 */
	public void validate(Document document, String version) {
		String schemaPath = SCHEMA_BY_VERSION.get(version);
		if (schemaPath == null) {
			throw new XliffMessageSourceValidationException(
				String.format("No schema available for version \"%s\"", version)
			);
		}
		String schemaResource = SCHEMA_PATH + schemaPath;

		CollectingErrorHandler errorHandler = new CollectingErrorHandler();

		EntityResolver entityResolver = (publicId, systemId) -> {
			if (systemId != null && systemId.endsWith("xml.xsd")) {
				InputSource source = new InputSource(openXmlSchema());
				source.setSystemId(systemId); // <<----------------------------------------------------- ??????
				return source;
			}
			return null;
		};

		PropertyMapBuilder properties = new PropertyMapBuilder();
		properties.put(ValidateProperty.ERROR_HANDLER, errorHandler);
		properties.put(ValidateProperty.ENTITY_RESOLVER, entityResolver);

		ValidationDriver driver = new ValidationDriver(properties.toPropertyMap(), new AutoSchemaReader());
		try {
			URL schema = XliffSchemaValidator.class.getResource(schemaResource);
			if (schema == null) {
				throw new XliffMessageSourceValidationException(
						String.format("Bundled schema resource \"%s\" not found.", schemaResource)
				);
			}
			if (!driver.loadSchema(new InputSource(schema.toExternalForm()))) {
				throw new XliffMessageSourceValidationException(
						errorHandler.errors.isEmpty()
								? String.format("Unable to compile schema \"%s\"", schemaResource)
								: String.join("", errorHandler.errors)
				);
			}
			driver.validate(new InputSource(new ByteArrayInputStream(serialize(document))));
		}
		catch (SAXException | IOException e) {
			throw new XliffMessageSourceValidationException(e.getMessage());
		}

		if (!errorHandler.errors.isEmpty()) {
			throw new XliffMessageSourceValidationException(String.join("", errorHandler.errors));
		}
	}

	/**
	 * Serializes the parsed document back to bytes, since the Jing validator consumes a byte stream.
	 */
	private static byte[] serialize(Document document) {
		try {
			// Force the JDK's built-in factory: the jing dependency puts a standalone Xalan/Xerces
			// on the classpath whose TransformerFactory does not recognize the JAXP 1.5 accessExternal*
			// attributes set below.
			TransformerFactory factory = TransformerFactory.newInstance(
					"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
					XliffSchemaValidator.class.getClassLoader()
			);
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			Transformer transformer = factory.newTransformer();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			transformer.transform(new DOMSource(document), new StreamResult(out));
			return out.toByteArray();
		}
		catch (TransformerException e) {
			throw new XliffMessageSourceValidationException(e.getMessage());
		}
	}

	/**
	 * Opens the bundled W3C {@code xml.xsd} from the package (classpath); every core schema imports it.
	 */
	private static InputStream openXmlSchema() {
		String resource = SCHEMA_PATH + "xml.xsd";
		InputStream in = XliffSchemaValidator.class.getResourceAsStream(resource);
		if (in == null) {
			throw new XliffMessageSourceValidationException(
					String.format("Bundled schema resource \"%s\" not found.", resource)
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
		 * Ignores a schema warning: warnings (e.g. Jing's XSD hint about an odd
		 * schemaLocation URI count) are not schema violations and must not fail
		 * validation.
		 *
		 * @param exception the warning reported by the parser.
		 */
		@Override
		public void warning(SAXParseException exception) {
			// Not a violation; intentionally not collected.
		}

		/**
		 * Records a schema error.
		 *
		 * @param exception the violation reported by the parser.
		 */
		@Override
		public void error(SAXParseException exception) {
			this.errors.add(format(exception));
		}

		/**
		 * Records a fatal schema error.
		 *
		 * @param exception the violation reported by the parser.
		 */
		@Override
		public void fatalError(SAXParseException exception) {
			this.errors.add(format(exception));
		}

		/**
		 * Formats a single schema violation as
		 * [LEVEL] message (line L, column C).
		 */
		private static String format(SAXParseException exception) {
			return String.format(
					"[%s] %s (line %d, column %d)%n",
					"ERROR",
					exception.getMessage(),
					exception.getLineNumber(),
					exception.getColumnNumber()
			);
		}
	}
}
