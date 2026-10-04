// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import org.w3c.dom.Element;

/**
 * Renders {@code <cp hex="..."/>} (2.x) as the referenced code point.
 */
class InlineElementCodePointElementStrategy extends InlineElementStrategyAbstract {

	@Override
	public void append(StringBuilder out, Element element, int depth) {
		out.appendCodePoint(Integer.parseInt(element.getAttribute("hex"), 16));
	}
}
