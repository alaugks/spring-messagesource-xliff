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
 * Renders {@code <pc/>} (2.x), wrapping its rendered content in the original data
 * referenced by {@code dataRefStart}/{@code dataRefEnd}, falling back to
 * {@code equivStart}/{@code equivEnd} when no original data is referenced.
 *
 * @author André Laugks
 */
class InlineElementPairedCodeElementStrategy extends InlineElementStrategyAbstract {

	@Override
	public void append(StringBuilder out, Element element, int depth) {
		out.append(this.boundary(element, "dataRefStart", "equivStart"));
		this.appendChildren(element, out, depth + 1);
		out.append(this.boundary(element, "dataRefEnd", "equivEnd"));
	}

	// Resolves one pc boundary: dataRef attribute via <originalData>, falling back to the
	// equiv attribute.
	private String boundary(Element element, String dataRefAttribute, String equivAttribute) {
		String data = this.originalData(element, dataRefAttribute);

		if (!data.isEmpty()) {
			return data;
		}

		return element.getAttribute(equivAttribute);
	}

}
