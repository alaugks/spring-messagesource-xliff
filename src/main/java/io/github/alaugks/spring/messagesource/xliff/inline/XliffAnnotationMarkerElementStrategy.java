// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff.inline;

import io.github.alaugks.spring.messagesource.xliff.XliffInlineRenderer;
import org.w3c.dom.Element;

/**
 * Renders {@code <sm>}/{@code <em>} (2.x) annotation markers, which contribute
 * no text.
 */
public final class XliffAnnotationMarkerElementStrategy extends XliffInlineElementStrategyAbstract {

	@Override
	public void append(Element element, StringBuilder out, int depth, XliffInlineRenderer renderer) {
		// Annotation markers contribute nothing.
	}
}
