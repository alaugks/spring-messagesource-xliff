// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Renders the content of a {@code <source>}/{@code <target>} element as plain
 * text, reconstructing XLIFF inline elements (prototype).
 *
 * <p>Rules, identical for XLIFF 1.2 and 2.x:
 * <ul>
 *   <li>Text and CDATA are taken verbatim.</li>
 *   <li>{@code <cp hex="..."/>} (2.x) becomes the referenced code point.</li>
 *   <li>{@code <pc/>} (2.x) is wrapped in the original data referenced by
 *       {@code dataRefStart}/{@code dataRefEnd}.</li>
 *   <li>An empty inline element ({@code <ph/>}, {@code <sc/>}, {@code <ec/>},
 *       {@code <x/>}, {@code <bx/>}, {@code <ex/>}, ...) becomes the original
 *       data referenced by {@code dataRef} (2.x), falling back to
 *       {@code equiv} (2.x) / {@code equiv-text} (1.2).</li>
 *   <li>Any other element with content ({@code <g/>}, {@code <mrk/>},
 *       {@code <ph>native</ph>}, ...) is replaced by its rendered content.</li>
 *   <li>{@code <sm/>}/{@code <em/>} annotation markers contribute nothing.</li>
 * </ul>
 */
class XliffInlineRenderer {

	private static final int MAX_DEPTH = 32;

	/**
	 * Renders the element's content as text.
	 *
	 * @param element the source/target element.
	 * @return the rendered, untrimmed text.
	 */
	String render(Element element) {
		StringBuilder out = new StringBuilder();
		this.appendChildren(element, out, 0);
		return out.toString();
	}

	/**
	 * Appends the rendered children of the parent.
	 */
	private void appendChildren(Element parent, StringBuilder out, int depth) {
		if (depth > MAX_DEPTH) {
			return;
		}
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getNodeType()) {
				case Node.TEXT_NODE, Node.CDATA_SECTION_NODE -> out.append(child.getNodeValue());
				case Node.ELEMENT_NODE -> this.appendElement((Element) child, out, depth);
				default -> {
					// Comments and processing instructions carry no text.
				}
			}
		}
	}

	/**
	 * Appends the rendering of a single inline element.
	 */
	private void appendElement(Element element, StringBuilder out, int depth) {
		String name = XliffDocument.elementName(element);
		switch (name) {
			case "sm", "em" -> {
				// Annotation markers contribute nothing.
			}
			case "cp" -> this.appendCodePoint(element, out);
			case "pc" -> {
				out.append(this.originalData(element, "dataRefStart"));
				this.appendChildren(element, out, depth + 1);
				out.append(this.originalData(element, "dataRefEnd"));
			}
			default -> {
				if (element.hasChildNodes()) {
					this.appendChildren(element, out, depth + 1);
				} else {
					out.append(this.placeholder(element));
				}
			}
		}
	}

	private void appendCodePoint(Element cp, StringBuilder out) {
		try {
			int codePoint = Integer.parseInt(cp.getAttribute("hex").trim(), 16);
			if (Character.isValidCodePoint(codePoint)) {
				out.appendCodePoint(codePoint);
			}
		} catch (NumberFormatException e) {
			// Not a hex number; skipped.
		}
	}

	private String placeholder(Element element) {
		String data = this.originalData(element, "dataRef");
		if (!data.isEmpty()) {
			return data;
		}
		String equiv = element.getAttribute("equiv");
		return equiv.isEmpty() ? element.getAttribute("equiv-text") : equiv;
	}

	private String originalData(Element element, String attribute) {
		String ref = element.getAttribute(attribute);
		if (ref.isEmpty()) {
			return "";
		}
		Element unit = this.enclosingUnit(element);
		Element originalData = unit != null ? XliffDocument.firstChildElement(unit, "originalData") : null;
		if (originalData == null) {
			return "";
		}
		for (Node data = originalData.getFirstChild(); data != null; data = data.getNextSibling()) {
			if (data instanceof Element dataElement
				&& "data".equals(XliffDocument.elementName(dataElement))
				&& ref.equals(dataElement.getAttribute("id"))
			) {
				StringBuilder out = new StringBuilder();
				this.appendChildren(dataElement, out, MAX_DEPTH - 1);
				return out.toString();
			}
		}
		return "";
	}
	
	private @Nullable Element enclosingUnit(Element element) {
		Node node = element.getParentNode();
		while (node instanceof Element current) {
			if ("unit".equals(XliffDocument.elementName(current))) {
				return current;
			}
			node = current.getParentNode();
		}
		return null;
	}
}
