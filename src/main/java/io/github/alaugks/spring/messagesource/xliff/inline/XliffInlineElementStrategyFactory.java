// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff.inline;

import io.github.alaugks.spring.messagesource.xliff.XliffElementSupport;
import org.w3c.dom.Element;

/**
 * Chooses the {@link XliffInlineElementStrategyAbstract} to render an inline
 * element with, by its local name.
 */
final class XliffInlineElementStrategyFactory {

	private static final XliffInlineElementStrategyAbstract ANNOTATION_MARKER = new AnnotationMarkerElementStrategy();
	private static final XliffInlineElementStrategyAbstract CODE_POINT = new CodePointElementStrategy();
	private static final XliffInlineElementStrategyAbstract PAIRED_CODE = new PairedCodeElementStrategy();
	private static final XliffInlineElementStrategyAbstract GENERIC = new GenericInlineElementStrategy();

	private XliffInlineElementStrategyFactory() {
	}

	/**
	 * Picks the rendering strategy for an inline element by its local name.
	 *
	 * @param element the inline element to render.
	 * @return the strategy responsible for {@code element}.
	 */
	static XliffInlineElementStrategyAbstract strategyFor(Element element) {
		String name = XliffElementSupport.elementName(element);
		return switch (name) {
			case "sm", "em" -> ANNOTATION_MARKER;
			case "cp" -> CODE_POINT;
			case "pc" -> PAIRED_CODE;
			default -> GENERIC;
		};
	}
}
