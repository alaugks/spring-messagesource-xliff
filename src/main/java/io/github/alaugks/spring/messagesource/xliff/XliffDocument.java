/*
 * Copyright 2023-2025 André Laugks <alaugks@gmail.com>
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public  class XliffDocument {

	private final Element root;

	public XliffDocument(Element root) {
		this.root = root;
	}

	public XliffDocument(Document document) {
		this.root = document.getDocumentElement();
	}

	public Map<String, String> getTransUnitsMap(String transUnitName, List<String> transUnitIdentifiers) {
		Map<String, String> transUnitMap = new HashMap<>();

		if (this.isXliffDocument()) {
			NodeList nodeList = this.root.getElementsByTagName(transUnitName);

			for (int item = 0; item < nodeList.getLength(); item++) {
				Element node = (Element) nodeList.item(item);
				Arrays.stream(transUnitIdentifiers.toArray())
						.map(attributeName -> this.getAttributeValue(
								node.getAttributes().getNamedItem(attributeName.toString())
						))
						.filter(code -> (code != null && !code.isEmpty()))
						.findFirst()
						.ifPresent(code -> transUnitMap.put(
								code,
								this.getCharacterDataFromElement(
										node.getElementsByTagName("target").item(0).getFirstChild()
								)
						));
			}
		}

		return transUnitMap;
	}

	public String getXliffVersion() {
		if (this.isXliffDocument()) {
			return this.getAttributeValue(
					root.getAttributes().getNamedItem("version")
			);
		}

		return null;
	}

	private boolean isXliffDocument() {
		// Simple test: Filter if root element <xliff>
		return root.getNodeName().equals("xliff");
	}

	private String getCharacterDataFromElement(Node child) {
		if (child.getNextSibling() != null) {
			return child.getNextSibling().getTextContent().trim();
		}
		return child.getNodeValue().trim();
	}

	private String getAttributeValue(Node node) {
		if (node != null) {
			return node.getNodeValue();
		}
		return null;
	}
}
