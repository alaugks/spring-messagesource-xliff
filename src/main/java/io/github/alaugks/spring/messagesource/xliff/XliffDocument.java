// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import javax.xml.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Abstract base for reading translation units from a parsed XLIFF document.
 *
 * <p>Subclasses implement version-specific extraction; this class provides the
 * shared utilities (key derivation helpers, text extraction, node traversal)
 * and the {@link #readVersion(Element)} factory helper used before the correct
 * subclass can be chosen.
 */
public abstract class XliffDocument {

	protected static final String SOURCE = "source";

	protected static final String TARGET = "target";

	protected final Element root;

	/**
	 * Creates a document reader for the given root element.
	 *
	 * @param root the root element of the parsed XLIFF document.
	 */
	protected XliffDocument(Element root) {
		this.root = root;
	}

	/**
	 * Creates a document reader for the given parsed document.
	 *
	 * @param document the parsed XLIFF document.
	 */
	protected XliffDocument(Document document) {
		this.root = document.getDocumentElement();
	}

	/**
	 * Reads the XLIFF version from the root element without requiring a
	 * concrete subclass instance. Returns {@code null} when the element is not
	 * an XLIFF document or declares no {@code version} attribute.
	 *
	 * @param root the root element of a parsed XML document.
	 * @return the declared XLIFF version string, or {@code null}.
	 */
	public static String readVersion(Element root) {
		String localName = root.getLocalName();
		String name = localName != null ? localName : root.getNodeName();
		if (!"xliff".equals(name)) {
			return null;
		}
		String version = root.getAttribute("version");
		return version.isEmpty() ? null : version;
	}

	/**
	 * Returns the XLIFF version declared on the root element, or {@code null}
	 * when absent or when this is not an XLIFF document.
	 *
	 * @return the declared XLIFF version string, or {@code null}.
	 */
	public String getXliffVersion() {
		return readVersion(this.root);
	}

	/**
	 * Returns whether the root element is an {@code <xliff>} element.
	 *
	 * @return {@code true} if the root is an XLIFF document element.
	 */
	protected boolean isXliffDocument() {
		return "xliff".equals(elementName(this.root));
	}

	/**
	 * Returns the first non-null, non-empty value.
	 *
	 * @param values the candidate values, in priority order.
	 * @return the first non-empty value, or {@code ""} if none qualifies.
	 */
	protected String firstNonEmpty(String... values) {
		for (String value : values) {
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}
		return "";
	}

	/**
	 * Returns the first direct child element with the given local name.
	 *
	 * @param parent    the element whose children are searched.
	 * @param localName the local name to match.
	 * @return the first matching child element, or {@code null} if none.
	 */
	protected static Element firstChildElement(Element parent, String localName) {
		Node child = parent.getFirstChild();
		while (child != null) {
			if (child.getNodeType() == Node.ELEMENT_NODE && localName.equals(elementName(child))) {
				return (Element) child;
			}
			child = child.getNextSibling();
		}
		return null;
	}

	/**
	 * Returns the node's local name, falling back to its node name when no
	 * namespace-aware local name is available.
	 *
	 * @param node the node to name.
	 * @return the local name, or the node name as fallback.
	 */
	protected static String elementName(Node node) {
		String localName = node.getLocalName();
		return localName != null ? localName : node.getNodeName();
	}

	/**
	 * Returns the element's text content as the displayable value: trimmed
	 * unless the element's effective {@code xml:space} is {@code "preserve"}.
	 *
	 * @param element the source/target element; may be {@code null}.
	 * @return the (conditionally trimmed) text content; {@code ""} when
	 *         {@code element} is {@code null}.
	 */
	protected String value(Element element) {
		String raw = this.rawValue(element);
		return element != null && this.isPreserveSpace(element) ? raw : raw.trim();
	}

	/**
	 * Returns the element's raw text content without trimming.
	 *
	 * @param element the element to read; may be {@code null}.
	 * @return the text content, or {@code ""} when {@code element} is
	 *         {@code null}.
	 */
	protected String rawValue(Element element) {
		if (element == null) {
			return "";
		}
		String content = element.getTextContent();
		return content != null ? content : "";
	}

	/**
	 * Returns whether whitespace in the element's content is significant, i.e.
	 * the nearest {@code xml:space} declaration on the element or one of its
	 * ancestors is {@code "preserve"}. When no ancestor declares
	 * {@code xml:space}, whitespace is not preserved (the XML default).
	 *
	 * @param element the element whose effective {@code xml:space} is resolved;
	 *                may be {@code null}.
	 * @return {@code true} if the effective {@code xml:space} is
	 *         {@code "preserve"}.
	 */
	protected boolean isPreserveSpace(Element element) {
		Node node = element;
		while (node instanceof Element current) {
			String space = this.xmlSpace(current);
			if (!space.isEmpty()) {
				return "preserve".equals(space);
			}
			node = current.getParentNode();
		}
		return false;
	}

	/**
	 * Reads the element's xml:space attribute (XML-namespaced, with
	 * prefixed-name fallback).
	 */
	private String xmlSpace(Element element) {
		String space = element.getAttributeNS(XMLConstants.XML_NS_URI, "space");
		return space.isEmpty() ? element.getAttribute("xml:space") : space;
	}
}
