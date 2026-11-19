/*
 * Copyright André Laugks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.alaugks.spring.messagesource.xliff;

import org.w3c.dom.Element;

/**
 * Chooses the {@link InlineElementStrategyAbstract} to render an inline element with, by
 * its local name.
 *
 * @author André Laugks
 */
final class InlineElementStrategyFactory {

	private static final InlineElementStrategyAbstract ANNOTATION_MARKER = new InlineElementAnnotationMarkerElementStrategy();

	private static final InlineElementStrategyAbstract CODE_POINT = new InlineElementCodePointElementStrategy();

	private static final InlineElementStrategyAbstract PAIRED_CODE = new InlineElementPairedCodeElementStrategy();

	private static final InlineElementStrategyAbstract GENERIC = new InlineElementGenericInlineElementStrategy();

	private InlineElementStrategyFactory() {
	}

	/**
	 * Picks the rendering strategy for an inline element by its local name.
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
