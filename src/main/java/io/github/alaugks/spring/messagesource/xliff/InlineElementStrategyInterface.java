// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import org.w3c.dom.Element;

/**
 * Contract for rendering a single inline element as text.
 */
interface InlineElementStrategyInterface {

	/**
	 * Appends this element's rendering to {@code out}.
	 *
	 * @param out     the buffer to append to.
	 * @param element the inline element to render.
	 * @param depth   the current recursion depth.
	 */
	void append(StringBuilder out, Element element, int depth);
}
