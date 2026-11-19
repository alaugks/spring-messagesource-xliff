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
 * Renders any other inline element ({@code <g/>}, {@code <mrk/>},
 * {@code <ph>native</ph>}, {@code <x/>}, {@code <bx/>}, {@code <ex/>}, {@code <sc/>},
 * {@code <ec/>}, ...).
 *
 * <p>
 * An element with content is replaced by its rendered content. An empty element becomes
 * the original data referenced by {@code dataRef} (2.x), falling back to {@code equiv}
 * (2.x) / {@code equiv-text} (1.2).
 *
 * @author André Laugks
 */
class InlineElementGenericInlineElementStrategy extends InlineElementStrategyAbstract {

	@Override
	public void append(StringBuilder out, Element element, int depth) {
		if (element.hasChildNodes()) {
			this.appendChildren(element, out, depth + 1);
		}
		else {
			String data = this.originalData(element, "dataRef");
			if (!data.isEmpty()) {
				out.append(data);
				return;
			}

			String equiv = element.getAttribute("equiv");
			if (!equiv.isEmpty()) {
				out.append(equiv);
			}

			String equivText = element.getAttribute("equiv-text");
			if (!equivText.isEmpty()) {
				out.append(equivText);
			}
		}
	}

}
