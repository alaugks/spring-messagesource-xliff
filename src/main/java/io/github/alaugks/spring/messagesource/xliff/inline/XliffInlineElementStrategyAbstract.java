// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff.inline;

import io.github.alaugks.spring.messagesource.xliff.XliffElementSupport;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Base for the per-element rendering strategies used by
 * {@link io.github.alaugks.spring.messagesource.xliff.XliffInlineRenderer}.
 *
 * <p>Provides the behavior shared by every strategy: the dispatch loop that
 * renders an element's children ({@link #appendChildren}), and resolving
 * original data referenced by a {@code dataRef}/{@code dataRefStart}/
 * {@code dataRefEnd} attribute via the enclosing {@code <unit>}'s
 * {@code <originalData>} ({@link #originalData}).
 */
public abstract class XliffInlineElementStrategyAbstract implements XliffInlineElementStrategyInterface {

	private static final int MAX_DEPTH = 32;

	/**
	 * Appends the rendered children of the parent, dispatching each element
	 * child to its {@link XliffInlineElementStrategyAbstract} via
	 * {@link XliffInlineElementStrategyFactory}. Called by subclasses to
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
					XliffInlineElementStrategyFactory.strategyFor(childElement).append(childElement, out, depth);
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
	 * @return the referenced data's rendered content, or {@code ""} if none.
	 */
	protected final String originalData(Element element, String attribute) {
		String ref = element.getAttribute(attribute);
		if (ref.isEmpty()) {
			return "";
		}
		Element unit = this.enclosingUnit(element);
		Element originalData = unit != null ? XliffElementSupport.firstChildElement(unit, "originalData") : null;
		if (originalData == null) {
			return "";
		}
		for (Node data = originalData.getFirstChild(); data != null; data = data.getNextSibling()) {
			if (data instanceof Element dataElement
				&& "data".equals(XliffElementSupport.elementName(dataElement))
				&& ref.equals(dataElement.getAttribute("id"))
			) {
				StringBuilder out = new StringBuilder();
				this.appendChildren(dataElement, out, MAX_DEPTH - 1);
				return out.toString();
			}
		}
		return "";
	}

	// Finds the closest ancestor <unit> element, or null outside of one.
	private @Nullable Element enclosingUnit(Element element) {
		Node node = element.getParentNode();
		while (node instanceof Element current) {
			if ("unit".equals(XliffElementSupport.elementName(current))) {
				return current;
			}
			node = current.getParentNode();
		}
		return null;
	}
}
