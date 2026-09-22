// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.inline.XliffInlineElementStrategyAbstract;
import org.w3c.dom.Element;

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
 * <p>Extends {@link XliffInlineElementStrategyAbstract} purely to reuse its
 * dispatch loop for the top-level {@code <source>}/{@code <target>} element,
 * which is rendered exactly like any inline element's content.
 */
public final class XliffInlineRenderer extends XliffInlineElementStrategyAbstract {

	/**
	 * Renders the element's content as text.
	 *
	 * @param element the source/target element.
	 * @return the rendered, untrimmed text.
	 */
	String render(Element element) {
		StringBuilder out = new StringBuilder();
		this.append(element, out, 0);
		return out.toString();
	}

	@Override
	public void append(Element element, StringBuilder out, int depth) {
		this.appendChildren(element, out, depth);
	}
}
