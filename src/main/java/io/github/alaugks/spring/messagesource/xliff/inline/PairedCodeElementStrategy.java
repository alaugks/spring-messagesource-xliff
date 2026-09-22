// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff.inline;

import org.w3c.dom.Element;

/**
 * Renders {@code <pc/>} (2.x), wrapping its rendered content in the original
 * data referenced by {@code dataRefStart}/{@code dataRefEnd}, falling back to
 * {@code equivStart}/{@code equivEnd} when no original data is referenced.
 */
public final class PairedCodeElementStrategy extends XliffInlineElementStrategyAbstract {

	@Override
	public void append(Element element, StringBuilder out, int depth) {
		out.append(this.boundary(element, "dataRefStart", "equivStart"));
		this.appendChildren(element, out, depth + 1);
		out.append(this.boundary(element, "dataRefEnd", "equivEnd"));
	}

	/**
	 * Resolves one pc boundary: dataRef attribute via <originalData>, falling back to the equiv attribute.
	 */
	private String boundary(Element element, String dataRefAttribute, String equivAttribute) {
		String data = this.originalData(element, dataRefAttribute);
		return !data.isEmpty() ? data : element.getAttribute(equivAttribute);
	}
}
