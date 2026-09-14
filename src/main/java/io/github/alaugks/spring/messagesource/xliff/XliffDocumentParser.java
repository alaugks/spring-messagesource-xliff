// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.exception.SaxErrorHandler;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Shared setup for parsing XLIFF content with a namespace-aware,
 * XXE-hardened DOM parser.
 */
class XliffDocumentParser {

	private XliffDocumentParser() {
	}

	/**
	 * Creates a new document builder factory configured for secure,
	 * namespace-aware parsing of XLIFF content.
	 *
	 * @return a namespace-aware factory with secure processing and DTD
	 *         declarations disabled.
	 * @throws ParserConfigurationException if a requested feature is not
	 *                                       supported by the underlying
	 *                                       parser.
	 */
	static DocumentBuilderFactory newDocumentBuilderFactory() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		return factory;
	}

	/**
	 * Parses the given XLIFF content and returns its root element, reporting
	 * SAX warnings, errors, and fatal errors through {@link SaxErrorHandler}.
	 *
	 * @param factory the (securely configured) factory to build the parser
	 *                from.
	 * @param content the XLIFF content to parse.
	 * @return the root element of the parsed document.
	 * @throws ParserConfigurationException if the parser cannot be created
	 *                                       from the given factory.
	 * @throws SAXException                 if the content cannot be parsed
	 *                                       as XML.
	 * @throws IOException                  if the content cannot be read.
	 */
	static Element parseRootElement(DocumentBuilderFactory factory, byte[] content)
		throws ParserConfigurationException, SAXException, IOException {

		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		documentBuilder.setErrorHandler(new SaxErrorHandler());
		Document document = documentBuilder.parse(new ByteArrayInputStream(content));
		return document.getDocumentElement();
	}
}
