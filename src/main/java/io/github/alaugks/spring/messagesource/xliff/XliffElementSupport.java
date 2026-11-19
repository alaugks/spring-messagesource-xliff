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

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * DOM helpers shared across the library's XLIFF parsing and inline rendering, both of
 * which need to identify elements by local name.
 *
 * @author André Laugks
 */
final class XliffElementSupport {

	private XliffElementSupport() {
	}

	/**
	 * Returns the node's local name, falling back to its node name when no
	 * namespace-aware local name is available.
	 * @param node the node to name.
	 * @return the local name, or the node name as fallback.
	 */
	static String elementName(Node node) {
		String localName = node.getLocalName();
		return (localName != null) ? localName : node.getNodeName();
	}

	/**
	 * Returns the first direct child element with the given local name.
	 * @param parent the element whose children are searched.
	 * @param localName the local name to match.
	 * @return the first matching child element, or {@code null} if none.
	 */
	static @Nullable Element firstChildElement(Element parent, String localName) {
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && localName.equals(elementName(child))) {
				return (Element) child;
			}
		}
		return null;
	}

}
