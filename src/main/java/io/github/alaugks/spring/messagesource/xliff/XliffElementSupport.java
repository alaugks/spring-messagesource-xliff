// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * DOM helpers shared across the library's XLIFF parsing and inline
 * rendering, both of which need to identify elements by local name.
 */
public final class XliffElementSupport {

	private XliffElementSupport() {
	}

	/**
	 * Returns the node's local name, falling back to its node name when no
	 * namespace-aware local name is available.
	 *
	 * @param node the node to name.
	 * @return the local name, or the node name as fallback.
	 */
	public static String elementName(Node node) {
		String localName = node.getLocalName();
		return localName != null ? localName : node.getNodeName();
	}

	/**
	 * Returns the first direct child element with the given local name.
	 *
	 * @param parent    the element whose children are searched.
	 * @param localName the local name to match.
	 * @return the first matching child element, or {@code null} if none.
	 */
	public static @Nullable Element firstChildElement(Element parent, String localName) {
		Node child = parent.getFirstChild();
		while (child != null) {
			if (child.getNodeType() == Node.ELEMENT_NODE && localName.equals(elementName(child))) {
				return (Element) child;
			}
			child = child.getNextSibling();
		}
		return null;
	}
}
