// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff.inline;

import io.github.alaugks.spring.messagesource.xliff.XliffInlineRenderer;
import org.w3c.dom.Element;

/**
 * Renders {@code <cp hex="..."/>} (2.x) as the referenced code point.
 */
public final class XliffCodePointElementStrategy extends XliffInlineElementStrategyAbstract {

	@Override
	public void append(Element element, StringBuilder out, int depth, XliffInlineRenderer renderer) {
		try {
			int codePoint = Integer.parseInt(element.getAttribute("hex").trim(), 16);
			if (Character.isValidCodePoint(codePoint)) {
				out.appendCodePoint(codePoint);
			}
		} catch (NumberFormatException e) {
			// Not a hex number; skipped.
		}
	}
}
