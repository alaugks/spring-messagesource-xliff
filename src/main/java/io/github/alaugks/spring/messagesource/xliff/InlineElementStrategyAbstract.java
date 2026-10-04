// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Base for the per-element rendering strategies used by
 * {@link InlineElementRenderer}.
 *
 * <p>Provides the behavior shared by every strategy: the dispatch loop that
 * renders an element's children ({@link #appendChildren}), and resolving
 * original data referenced by a {@code dataRef}/{@code dataRefStart}/
 * {@code dataRefEnd} attribute via the enclosing {@code <unit>}'s
 * {@code <originalData>} ({@link #originalData}).
 */
abstract class InlineElementStrategyAbstract implements InlineElementStrategyInterface {

	private static final int MAX_DEPTH = 8;

	/**
	 * Appends the rendered children of the parent, dispatching each element
	 * child to its {@link InlineElementStrategyAbstract} via
	 * {@link InlineElementStrategyFactory}. Called by subclasses to
	 * recurse into an element's own children.
	 *
	 * @param parent the element whose children to render.
	 * @param out the buffer to append to.
	 * @param depth the current recursion depth.
	 */
	protected final void appendChildren(Element parent, StringBuilder out, int depth) {
		if (depth > MAX_DEPTH) {
			return;
		}
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getNodeType()) {
				case Node.TEXT_NODE, Node.CDATA_SECTION_NODE -> out.append(child.getNodeValue());
				case Node.ELEMENT_NODE -> {
					Element childElement = (Element) child;
					InlineElementStrategyFactory.strategyFor(childElement).append(out, childElement, depth);
				}
				default -> {
					// Comments and processing instructions carry no text.
				}
			}
		}
	}

	/**
	 * Resolves the original data referenced by {@code attribute} (one of
	 * {@code dataRef}, {@code dataRefStart}, {@code dataRefEnd}) via the
	 * element's enclosing {@code <unit>}'s {@code <originalData>}.
	 *
	 * @param element the element carrying the reference attribute.
	 * @param attribute the attribute holding the {@code <data>} id.
	 * @return the referenced data's rendered content, or {@code ""} if the
	 *         element carries no such attribute.
	 * @throws NullPointerException if the attribute references a {@code <data>}
	 *         that does not exist (invalid document).
	 */
	protected final String originalData(Element element, String attribute) {
		String ref = element.getAttribute(attribute);
		if (ref.isEmpty()) {
			return "";
		}
		Element originalData = Objects.requireNonNull(
			XliffElementSupport.firstChildElement(this.enclosingUnit(element), "originalData")
		);
		StringBuilder out = new StringBuilder();
		Element data = Objects.requireNonNull(this.dataById(originalData, ref));
		this.appendChildren(data, out, MAX_DEPTH - 1);
		return out.toString();
	}

	/**
	 * Finds the <data> child of <originalData> with the given id, or null if there is none.
	 */
	private @Nullable Element dataById(Element originalData, String id) {
		NodeList dataElements = originalData.getElementsByTagNameNS("*", "data");
		for (int i = 0; i < dataElements.getLength(); i++) {
			Element data = (Element) dataElements.item(i);
			if (id.equals(data.getAttribute("id"))) {
				return data;
			}
		}
		return null;
	}

	/**
	 * Finds the closest ancestor <unit> element. A dataRef is only valid inside a <unit>.
	 */
	private Element enclosingUnit(Element element) {
		Element current = (Element) element.getParentNode();
		while (!"unit".equals(XliffElementSupport.elementName(current))) {
			current = (Element) current.getParentNode();
		}
		return current;
	}
}
