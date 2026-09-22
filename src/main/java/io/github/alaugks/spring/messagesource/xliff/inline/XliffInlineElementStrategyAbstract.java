// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff.inline;

import io.github.alaugks.spring.messagesource.xliff.XliffInlineRenderer;
import org.w3c.dom.Element;

/**
 * Base for the per-element rendering strategies used by {@link XliffInlineRenderer}.
 *
 * <p>Provides the helpers shared by several strategies: recursing back into the
 * renderer's dispatch loop, and resolving original data referenced by a
 * {@code dataRef}/{@code dataRefStart}/{@code dataRefEnd} attribute via the
 * enclosing {@code <unit>}'s {@code <originalData>}.
 */
public abstract class XliffInlineElementStrategyAbstract {

	/**
	 * Appends this element's rendering to {@code out}.
	 *
	 * @param element the inline element to render.
	 * @param out the buffer to append to.
	 * @param depth the current recursion depth.
	 * @param renderer the renderer, used to recurse into child content.
	 */
	public abstract void append(Element element, StringBuilder out, int depth, XliffInlineRenderer renderer);

	// Delegates to the renderer's dispatch loop so subclasses can recurse into an element's children.
	protected final void appendChildren(Element parent, StringBuilder out, int depth, XliffInlineRenderer renderer) {
		renderer.appendChildren(parent, out, depth);
	}

	// Delegates to the renderer to resolve the original data referenced by attribute
	// (dataRef, dataRefStart or dataRefEnd), or "" if none.
	protected final String originalData(Element element, String attribute, XliffInlineRenderer renderer) {
		return renderer.originalData(element, attribute);
	}
}
