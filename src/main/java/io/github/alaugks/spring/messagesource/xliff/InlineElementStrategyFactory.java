// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import org.w3c.dom.Element;

/**
 * Chooses the {@link InlineElementStrategyAbstract} to render an inline
 * element with, by its local name.
 */
class InlineElementStrategyFactory {

	private static final InlineElementStrategyAbstract ANNOTATION_MARKER = new InlineElementAnnotationMarkerElementStrategy();
	private static final InlineElementStrategyAbstract CODE_POINT = new InlineElementCodePointElementStrategy();
	private static final InlineElementStrategyAbstract PAIRED_CODE = new InlineElementPairedCodeElementStrategy();
	private static final InlineElementStrategyAbstract GENERIC = new InlineElementGenericInlineElementStrategy();

	private InlineElementStrategyFactory() {
	}

	/**
	 * Picks the rendering strategy for an inline element by its local name.
	 *
	 * @param element the inline element to render.
	 * @return the strategy responsible for {@code element}.
	 */
	static InlineElementStrategyAbstract strategyFor(Element element) {
		String name = XliffElementSupport.elementName(element);
		return switch (name) {
			case "sm", "em" -> ANNOTATION_MARKER;
			case "cp" -> CODE_POINT;
			case "pc" -> PAIRED_CODE;
			default -> GENERIC;
		};
	}
}
