// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff.inline;

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
public final class GenericInlineElementStrategy extends XliffInlineElementStrategyAbstract {

	@Override
	public void append(Element element, StringBuilder out, int depth) {
		if (element.hasChildNodes()) {
			this.appendChildren(element, out, depth + 1);
		} else {
			String data = this.originalData(element, "dataRef");
			if (!data.isEmpty()) {
				out.append(data);
				return;
			}
			String equiv = element.getAttribute("equiv");
			out.append(equiv.isEmpty() ? element.getAttribute("equiv-text") : equiv);
		}
	}
}
