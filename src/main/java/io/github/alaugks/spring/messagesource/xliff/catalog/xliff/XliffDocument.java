package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import org.w3c.dom.CharacterData;
import org.w3c.dom.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class XliffDocument {

    private final Element root;
    private List<String> translationUnitIdentifiers;
    private NodeList nodeList;

    public XliffDocument(Element root) {
        this.root = root;
    }

    public XliffDocument(Document document) {
        this.root = document.getDocumentElement();
    }

    public Set<TransUnit> getTransUnits(String transUnitName, List<String> translationUnitIdentifiers) {
        this.nodeList = this.root.getElementsByTagName(transUnitName);
        this.translationUnitIdentifiers = translationUnitIdentifiers;
        return this.getNodes();
    }

    public boolean isXliffDocument() {
        // Simple test: Filter if root element not <xliff>
        return root.getNodeName().equals("xliff");
    }

    public String getXliffVersion() {
        return this.getAttributeValue(
                root.getAttributes().getNamedItem("version")
        );
    }

    private String getAttributeValue(Node node) {
        if (node != null) {
            return node.getNodeValue();
        }
        return null;
    }

    private Set<TransUnit> getNodes() {
        Set<TransUnit> transUnits = new HashSet<>();

        for (int item = 0; item < nodeList.getLength(); item++) {
            Node translationNode = nodeList.item(item);

            /* Translation Node */
            Element node = (Element) translationNode;

            String code = this.getCode(node);
            if (code != null) {
                transUnits.add(
                        new TransUnit(
                                code,
                                this.getTargetValue(node)
                        )
                );
            }
        }

        return transUnits;
    }

    private String getCode(
            Element translationUnit
    ) {
        if (this.translationUnitIdentifiers != null) {
            for (String name : this.translationUnitIdentifiers) {
                String value = this.getAttributeValue(translationUnit, name);
                if (value != null) {
                    return value;
                }
            }
        }

        return null;
    }

    private String getTargetValue(Element translationNodeElement) {
        return this.getElementValue(translationNodeElement);
    }

    private String getElementValue(Element translationNodeElement) {
        return this.getCharacterDataFromElement(
                this.getFirstChild(
                        translationNodeElement.getElementsByTagName("target")
                )
        );
    }

    private String getCharacterDataFromElement(Node child) {
        if (child instanceof CharacterData) {
            if (child.getNextSibling() != null) {
                return child.getNextSibling().getTextContent().trim();
            }
            return ((CharacterData) child).getData().trim();
        }
        return null;
    }

    private Node getFirstChild(NodeList nodeList) {
        return nodeList.item(0).getFirstChild();
    }

    private String getAttributeValue(Node translationNode, String attributeName) {
        return this.getAttributeValue(
                translationNode.getAttributes().getNamedItem(attributeName)
        );
    }

    public static class TransUnit {
        private final String code;
        private final String targetValue;

        public TransUnit(String code, String targetValue) {
            this.code = code;
            this.targetValue = targetValue;
        }

        public String getCode() {
            return code;
        }

        public String getTargetValue() {
            return targetValue;
        }
    }

}
