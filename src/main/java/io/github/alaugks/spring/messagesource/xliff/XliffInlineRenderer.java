// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.inline.AnnotationMarkerElementStrategy;
import io.github.alaugks.spring.messagesource.xliff.inline.CodePointElementStrategy;
import io.github.alaugks.spring.messagesource.xliff.inline.GenericInlineElementStrategy;
import io.github.alaugks.spring.messagesource.xliff.inline.PairedCodeElementStrategy;
import io.github.alaugks.spring.messagesource.xliff.inline.XliffInlineElementStrategyAbstract;
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
 *
 * <p>Dispatches each inline element to an
 * {@link XliffInlineElementStrategyAbstract}
 * chosen by element name, and serves as those strategies' access point back
 * into this package: {@link #appendChildren} to recurse, and
 * {@link #originalData} to resolve a {@code dataRef}-style attribute, so that
 * {@link XliffDocument} itself stays internal to this package.
 */
public final class XliffInlineRenderer {

	private static final int MAX_DEPTH = 32;

	private static final XliffInlineElementStrategyAbstract ANNOTATION_MARKER = new AnnotationMarkerElementStrategy();
	private static final XliffInlineElementStrategyAbstract CODE_POINT = new CodePointElementStrategy();
	private static final XliffInlineElementStrategyAbstract PAIRED_CODE = new PairedCodeElementStrategy();
	private static final XliffInlineElementStrategyAbstract GENERIC = new GenericInlineElementStrategy();

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
	 * Appends the rendered children of the parent, dispatching each element
	 * child to its {@link XliffInlineElementStrategyAbstract}. Called by strategies to
	 * recurse into an element's own children.
	 *
	 * @param parent the element whose children to render.
	 * @param out the buffer to append to.
	 * @param depth the current recursion depth.
	 */
	public void appendChildren(Element parent, StringBuilder out, int depth) {
		if (depth > MAX_DEPTH) {
			return;
		}
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getNodeType()) {
				case Node.TEXT_NODE, Node.CDATA_SECTION_NODE -> out.append(child.getNodeValue());
				case Node.ELEMENT_NODE -> {
					Element childElement = (Element) child;
					this.strategyFor(childElement).append(childElement, out, depth, this);
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
	public String originalData(Element element, String attribute) {
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

	/**
	 * Finds the closest ancestor <unit> element, or null outside of one.
	 */
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

	/**
	 * Choose the rendering strategy for an inline element by its local name.
	 */
	private XliffInlineElementStrategyAbstract strategyFor(Element element) {
		String name = XliffDocument.elementName(element);
		return switch (name) {
			case "sm", "em" -> ANNOTATION_MARKER;
			case "cp" -> CODE_POINT;
			case "pc" -> PAIRED_CODE;
			default -> GENERIC;
		};
	}
}
