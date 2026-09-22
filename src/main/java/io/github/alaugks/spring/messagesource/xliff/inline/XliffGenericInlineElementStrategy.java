// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff.inline;

import io.github.alaugks.spring.messagesource.xliff.XliffInlineRenderer;
import org.w3c.dom.Element;

/**
 * Renders any other inline element ({@code <g/>}, {@code <mrk/>},
 * {@code <ph>native</ph>}, {@code <x/>}, {@code <bx/>}, {@code <ex/>},
 * {@code <sc/>}, {@code <ec/>}, ...).
 *
 * <p>An element with content is replaced by its rendered content. An empty
 * element becomes the original data referenced by {@code dataRef} (2.x),
 * falling back to {@code equiv} (2.x) / {@code equiv-text} (1.2).
 */
public final class XliffGenericInlineElementStrategy extends XliffInlineElementStrategyAbstract {

	@Override
	public void append(Element element, StringBuilder out, int depth, XliffInlineRenderer renderer) {
		if (element.hasChildNodes()) {
			this.appendChildren(element, out, depth + 1, renderer);
		} else {
			out.append(this.placeholder(element, renderer));
		}
	}

	// Resolves an empty element's replacement text: dataRef, then equiv, then equiv-text.
	private String placeholder(Element element, XliffInlineRenderer renderer) {
		String data = this.originalData(element, "dataRef", renderer);
		if (!data.isEmpty()) {
			return data;
		}
		String equiv = element.getAttribute("equiv");
		return equiv.isEmpty() ? element.getAttribute("equiv-text") : equiv;
	}
}
