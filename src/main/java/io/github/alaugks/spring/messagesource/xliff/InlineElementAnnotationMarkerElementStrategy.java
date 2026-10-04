// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import org.w3c.dom.Element;

/**
 * Renders {@code <sm>}/{@code <em>} (2.x) annotation markers, which contribute
 * no text.
 */
class InlineElementAnnotationMarkerElementStrategy extends InlineElementStrategyAbstract {

	@Override
	public void append(StringBuilder out, Element element, int depth) {
		// Annotation markers contribute nothing.
	}
}
