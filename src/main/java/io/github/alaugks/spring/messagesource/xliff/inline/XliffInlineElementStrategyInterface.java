// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff.inline;

import org.w3c.dom.Element;

/**
 * Contract for rendering a single inline element as text.
 */
public interface XliffInlineElementStrategyInterface {

	/**
	 * Appends this element's rendering to {@code out}.
	 *
	 * @param element the inline element to render.
	 * @param out the buffer to append to.
	 * @param depth the current recursion depth.
	 */
	void append(Element element, StringBuilder out, int depth);
}
