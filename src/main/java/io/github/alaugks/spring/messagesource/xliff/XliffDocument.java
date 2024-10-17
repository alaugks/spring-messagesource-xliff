package io.github.alaugks.spring.messagesource.xliff;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class XliffDocument {

	private final Element root;

	public XliffDocument(Element root) {
		this.root = root;
	}

	public XliffDocument(Document document) {
		this.root = document.getDocumentElement();
	}

	public Map<String, String> getTransUnits(String transUnitName, List<String> transUnitIdentifiers) {
		if (this.isXliffDocument()) {
			return this.getNodes(
					this.root.getElementsByTagName(transUnitName),
					transUnitIdentifiers
			);
		}

		return new HashMap<>();
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

	private Map<String, String> getNodes(NodeList nodeList, List<String> transUnitIdentifiers) {
		Map<String, String> transUnitMap = new HashMap<>();

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

		return transUnitMap;
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
