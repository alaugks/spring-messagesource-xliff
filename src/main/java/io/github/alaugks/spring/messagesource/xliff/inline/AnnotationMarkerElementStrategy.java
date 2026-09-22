// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff.inline;

import org.w3c.dom.Element;

/**
 * Renders {@code <sm>}/{@code <em>} (2.x) annotation markers, which contribute
 * no text.
 */
public final class AnnotationMarkerElementStrategy extends XliffInlineElementStrategyAbstract {

	@Override
	public void append(Element element, StringBuilder out, int depth) {
		// Annotation markers contribute nothing.
	}
}
