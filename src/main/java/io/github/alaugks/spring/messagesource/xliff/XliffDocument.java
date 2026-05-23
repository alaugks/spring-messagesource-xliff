// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XliffDocument {

	private final Element root;

	/**
	 * Creates a new wrapper around the root {@code <xliff>} element of an
	 * already parsed XLIFF document.
	 *
	 * @param root the root element of the XLIFF document.
	 */
	public XliffDocument(Element root) {
		this.root = root;
	}

	/**
	 * Creates a new wrapper by extracting the root element from the given
	 * parsed XLIFF {@link Document}.
	 *
	 * @param document the parsed XLIFF document.
	 */
	public XliffDocument(Document document) {
		this.root = document.getDocumentElement();
	}

	/**
	 * Returns a mapping of translation identifiers to their target text for the
	 * given trans-unit element name.
	 * <p>The identifier of each entry is resolved by inspecting the attributes
	 * named in {@code transUnitIdentifiers} in order; the first non-empty value
	 * is used as the map key. Entries whose target text cannot be resolved
	 * (no {@code <target>} element or no text content) are skipped.
	 *
	 * @param transUnitName        the element name carrying the translation
	 *                             (e.g. {@code trans-unit} for XLIFF 1.2 or
	 *                             {@code segment} for XLIFF 2.x).
	 * @param transUnitIdentifiers ordered list of attribute names to probe for
	 *                             the translation identifier.
	 * @return map of identifier to translated target text; empty if the
	 *         document is not a recognised XLIFF document.
	 */
	public Map<String, String> getTransUnitsMap(String transUnitName, List<String> transUnitIdentifiers) {
		Map<String, String> transUnitMap = new HashMap<>();

		if (this.isXliffDocument()) {
			NodeList nodeList = this.root.getElementsByTagName(transUnitName);

			for (int item = 0; item < nodeList.getLength(); item++) {
				Element node = (Element) nodeList.item(item);
				String targetText = this.getTargetText(node);
				if (targetText == null) {
					continue;
				}
				transUnitIdentifiers.stream()
						.map(attributeName -> this.getAttributeValue(
								node.getAttributes().getNamedItem(attributeName)
						))
						.filter(code -> (code != null && !code.isEmpty()))
						.findFirst()
						.ifPresent(code -> transUnitMap.put(code, targetText));
			}
		}

		return transUnitMap;
	}

	/**
	 * Returns the target text for the given trans-unit element.
	 * <p>The first {@code <target>} descendant returned by
	 * {@link Element#getElementsByTagName(String)} is consulted, and its first
	 * child is passed to {@link #getCharacterDataFromElement(Node)}.
	 *
	 * @param node the trans-unit element.
	 * @return the trimmed target text, or {@code null} if no {@code <target>}
	 *         is present or its first child is missing.
	 */
	private String getTargetText(Element node) {
		NodeList targets = node.getElementsByTagName("target");
		if (targets.getLength() == 0) {
			return null;
		}
		Node firstChild = targets.item(0).getFirstChild();
		if (firstChild == null) {
			return null;
		}
		return this.getCharacterDataFromElement(firstChild);
	}

	/**
	 * Returns the value of the {@code version} attribute on the root element.
	 *
	 * @return the XLIFF version (e.g. {@code "1.2"}, {@code "2.0"}, {@code "2.1"}),
	 *         or {@code null} if the document is not an XLIFF document.
	 */
	public String getXliffVersion() {
		if (this.isXliffDocument()) {
			return this.getAttributeValue(
					root.getAttributes().getNamedItem("version")
			);
		}

		return null;
	}

	/**
	 * Tests whether the wrapped element is an XLIFF document root.
	 *
	 * @return {@code true} if the root element is named {@code xliff}.
	 */
	private boolean isXliffDocument() {
		// Simple test: Filter if root element <xliff>
		return root.getNodeName().equals("xliff");
	}

	/**
	 * Extracts trimmed character data from the given DOM node.
	 * <p>If the node has a next sibling, the trimmed
	 * {@link Node#getTextContent() text content} of that sibling is returned.
	 * Otherwise the trimmed {@link Node#getNodeValue() node value} of
	 * {@code child} itself is returned.
	 *
	 * @param child the DOM node to read from.
	 * @return the trimmed character data.
	 */
	private String getCharacterDataFromElement(Node child) {
		if (child.getNextSibling() != null) {
			return child.getNextSibling().getTextContent().trim();
		}
		return child.getNodeValue().trim();
	}

	/**
	 * Returns the value of the given attribute node.
	 *
	 * @param node the attribute node, may be {@code null}.
	 * @return the attribute value, or {@code null} if the node is {@code null}.
	 */
	private String getAttributeValue(Node node) {
		if (node != null) {
			return node.getNodeValue();
		}
		return null;
	}
}
